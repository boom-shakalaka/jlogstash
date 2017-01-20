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
package com.dtstack.logstash.assembly.disruptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dtstack.logstash.exception.ExceptionUtil;
import com.lmax.disruptor.ExceptionHandler;
/**
 * 
 * Reason: TODO ADD REASON(可选) Date: 2017年01月20日 下午09:25:18
 * Company:www.dtstack.com
 * 
 * @author sishu.yss
 *
 */
public class MapEventExceptionHandler implements ExceptionHandler<MapEvent> {
	
	private  static Logger logger = LoggerFactory.getLogger(MapEventExceptionHandler.class);

	@Override
	public void handleEventException(Throwable ex, long sequence,
			MapEvent event) {
		// TODO Auto-generated method stub
		logger.error("{}:handleEventException:{}",event.getEvent(),ExceptionUtil.getErrorMessage(ex));
	}

	@Override
	public void handleOnStartException(Throwable ex) {
		// TODO Auto-generated method stub
		logger.error("handleOnStartException:{}",ExceptionUtil.getErrorMessage(ex));
	}

	@Override
	public void handleOnShutdownException(Throwable ex) {
		// TODO Auto-generated method stub
		logger.error("handleOnShutdownException:{}",ExceptionUtil.getErrorMessage(ex));
	}  
}  