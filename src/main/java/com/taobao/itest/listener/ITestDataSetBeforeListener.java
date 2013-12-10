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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;

import com.taobao.itest.annotation.ITestDataSetBefore;
import com.taobao.itest.core.AbstractTestListener;
import com.taobao.itest.core.TestContext;

/**
 * 
 * 
 * @see ITestDataSetBefore
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 */
public class ITestDataSetBeforeListener extends AbstractTestListener {
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		List<FrameworkMethod> itestDataSetBefores = testContext.getJunitTestClass().getAnnotatedMethods(ITestDataSetBefore.class);
		// add by yufan 如果有异常，抛出，而不是吞掉
		List<Throwable> errors = new ArrayList<Throwable>();
		for (FrameworkMethod itestDataSetBefore : itestDataSetBefores) {
			try {
				itestDataSetBefore.invokeExplosively(testContext.getTestInstance());
			} catch (Throwable e) {
				e.printStackTrace();
				// add by yufan
				errors.add(e);
			}
		}
		// add by yufan
		if (!errors.isEmpty()) {
			throw new MultipleFailureException(errors);

		}
	}
}
