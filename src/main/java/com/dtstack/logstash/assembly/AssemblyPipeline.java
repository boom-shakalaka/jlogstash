/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.logstash.assembly;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dtstack.logstash.assembly.pthread.FilterThread;
import com.dtstack.logstash.assembly.pthread.InputThread;
import com.dtstack.logstash.assembly.pthread.OutputThread;
import com.dtstack.logstash.assembly.qlist.InputQueueList;
import com.dtstack.logstash.assembly.qlist.OutPutQueueList;
import com.dtstack.logstash.configs.YamlConfig;
import com.dtstack.logstash.exception.ExceptionUtil;
import com.dtstack.logstash.factory.InputFactory;
import com.dtstack.logstash.factory.InstanceFactory;
import com.dtstack.logstash.inputs.BaseInput;
import com.dtstack.logstash.outputs.BaseOutput;
import com.google.common.collect.Lists;
import  com.dtstack.logstash.classloader.JarClassLoader;

/**
 * 
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:25:11
 * Company: www.dtstack.com
 * @author sishu.yss
 *
 */
public class AssemblyPipeline {
	
	private static Logger logger = LoggerFactory.getLogger(AssemblyPipeline.class);
			
	private InputQueueList initInputQueueList;
	
	private OutPutQueueList initOutputQueueList;

	private List<BaseInput> baseInputs;
	
	private List<BaseOutput> allBaseOutputs = Lists.newCopyOnWriteArrayList();
	
	private JarClassLoader JarClassLoader = new JarClassLoader();
	

	/**
	 * 组装管道
	 * @param cmdLine
	 * @return 
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void assemblyPipeline(CommandLine cmdLine) throws IOException{
		try{
			logger.debug("load config start ...");
			Map configs = new YamlConfig().parse(cmdLine.getOptionValue("f"));
			logger.debug(configs.toString());
			logger.debug("load plugin...");
			InstanceFactory.setClassCloaders(JarClassLoader.loadJar());
			logger.debug("initInputQueueList start ...");
			initInputQueueList=InputQueueList.getInputQueueListInstance(CmdLineParams.getFilterWork(cmdLine), CmdLineParams.getInputQueueSize(cmdLine));
			if(initInputQueueList==null||initInputQueueList.getQueueList().size()==0){
				logger.error("init inputQueueList is error");
				System.exit(1);
			}
			List<Map> inputs = (List<Map>) configs.get("inputs");
			if(inputs==null||inputs.size()==0){
				logger.error("input plugin is not empty");
				System.exit(1);
			}
			logger.debug("initOutputQueueList start ...");
			initOutputQueueList = OutPutQueueList.getOutPutQueueListInstance(CmdLineParams.getOutputWork(cmdLine), CmdLineParams.getOutputQueueSize(cmdLine));
			if(initOutputQueueList==null||initOutputQueueList.getQueueList().size()==0){
				logger.error("init outputQueueList is error");
				System.exit(1);
			}	
			List<Map> outputs = (List<Map>) configs.get("outputs");
			if(outputs==null||outputs.size()==0){
				logger.error("output plugin is not empty");
				System.exit(1);
			}
		    List<Map> filters = (List<Map>) configs.get("filters");
			logger.debug("init input plugin start ...");
			baseInputs =InputFactory.getBatchInstance(inputs,initInputQueueList);
			initInputQueueList.startElectionIdleQueue();
			initOutputQueueList.startElectionIdleQueue();
			if(CmdLineParams.isQueueSizeLog(cmdLine)){
				initInputQueueList.startLogQueueSize();
				initOutputQueueList.startLogQueueSize();	
			}
			logger.debug("input thread start ...");
			InputThread.initInputThread(baseInputs);
			logger.debug("filter thread start ...");
			FilterThread.initFilterThread(filters,initInputQueueList,initOutputQueueList);
			logger.debug("output thread start ...");
			OutputThread.initOutPutThread(outputs,initOutputQueueList,allBaseOutputs);
    		//add shutdownhook
    		ShutDownHook shutDownHook = new ShutDownHook(initInputQueueList,initOutputQueueList,baseInputs,allBaseOutputs);
    		shutDownHook.addShutDownHook();
		}catch(Exception t){
			logger.error("assemblyPipeline is error:{}",ExceptionUtil.getErrorMessage(t));
			System.exit(1);
		}
	}
}