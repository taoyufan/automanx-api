/*
* Copyright 2011 Alibaba Group Holding Limited.
* All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package com.taobao.itest.tb.swapsample;

import javax.annotation.Resource;

import org.junit.Test;

import com.taobao.itest.ITestSpringContextBaseCase;
import com.taobao.itest.annotation.ITestSpringContext;
import com.taobao.itest.jdbc.SchemaDataSource;

/**
 * change different spring config file use maven need set maven-surefire-plugin
 * 					<systemProperties>
 * 						<property>
 * 							<name>locations</name>
 * 							<value>${runEnv}</value>
 * 						</property>
 * 					</systemProperties>
 * 
 * @author yuanhua
 *  change with -Dlocations="/swap/datasource2.xml"
 */
@ITestSpringContext("/swap/datasource1.xml") 
public class BaseCase extends ITestSpringContextBaseCase{

	@Resource
	protected static SchemaDataSource db;
	
	@Test
	public void test() {}
	
	
}