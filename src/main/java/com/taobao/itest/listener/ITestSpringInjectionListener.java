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

package com.taobao.itest.listener;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import com.taobao.itest.core.AbstractTestListener;
import com.taobao.itest.core.TestContext;
import com.taobao.itest.spring.context.SpringContextManager;

public class ITestSpringInjectionListener extends AbstractTestListener {

	@Override
	public void prepareTestInstance(final TestContext testContext)
			throws Exception {
		// System.out.println("ITestSpringInjectionListener");
		injectDependencies(testContext);
	}

	protected void injectDependencies(final TestContext testContext)
			throws Exception {
		if (SpringContextManager.getApplicationContext() == null) {
			return;
		}
		Object bean = testContext.getTestInstance();
		AutowireCapableBeanFactory beanFactory = SpringContextManager
				.getApplicationContext().getAutowireCapableBeanFactory();
		beanFactory.autowireBeanProperties(bean,
				AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		beanFactory.initializeBean(bean, testContext.getTestClass().getName());
	}

}
