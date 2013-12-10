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

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.taobao.itest.core.AbstractTestListener;
import com.taobao.itest.core.TestContext;
import com.taobao.itest.spring.context.SpringContextManager;

/**
 * Find fields and setter methods annotated by {@link javax.annotation.Resource}
 * and inject to spring application context
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 * 
 */
public class ITestResourceListener extends AbstractTestListener {
	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		injectBeansByName(testContext);
	}

	/**
	 * Injects spring beans into all fields that are annotated with
	 * {@link ITestSpringBean}.
	 * 
	 * @param testObject
	 *            The test instance, not null
	 */
	private void injectBeansByName(TestContext testContext) {
		Object testObject = testContext.getTestInstance();
		ApplicationContext applicationContext = SpringContextManager
				.getApplicationContext();

		SpringContextManager.findAnnotatedFieldsAndInjectBeanByName(
				Resource.class, testObject, applicationContext);
		SpringContextManager.findAnnotatedMethodsAndInjectBeanByName(
				Resource.class, testObject, applicationContext);

	}
}
