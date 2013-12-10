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
package com.taobao.itest;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.taobao.itest.core.ItestDataDriverRunner;
import com.taobao.itest.core.TestListeners;
import com.taobao.itest.listener.ITestDataSetBeforeListener;
import com.taobao.itest.listener.ITestDataSetListener;
import com.taobao.itest.listener.ITestHsfStarterListener;
import com.taobao.itest.listener.ITestResourceListener;
import com.taobao.itest.listener.ITestSpringContextListener;
import com.taobao.itest.listener.ITestSpringInjectionListener;
import com.taobao.itest.listener.ItestDataPrepareListener;
import com.taobao.itest.util.DateConverter;

@RunWith(ItestDataDriverRunner.class)
@TestListeners({ ITestHsfStarterListener.class,
		ITestSpringContextListener.class, ITestSpringInjectionListener.class,
		ITestResourceListener.class, ITestDataSetBeforeListener.class,
		ITestDataSetListener.class, ItestDataPrepareListener.class })
public class ITestSpringContextBaseCase implements ApplicationContextAware {
	static {
		ConvertUtils.register(new DateConverter(), Timestamp.class);
		ConvertUtils.register(new DateConverter(), Date.class);
		ConvertUtils.register(new DateConverter(), String.class);
	}
	protected final Log logger = LogFactory.getLog(getClass());

	protected ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
