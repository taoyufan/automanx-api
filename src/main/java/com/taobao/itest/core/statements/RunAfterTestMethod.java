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
package com.taobao.itest.core.statements;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import com.taobao.itest.core.TestContext;
import com.taobao.itest.core.TestContextManager;

/**
 * Statement which runs {@link org.junit.After} then
 * {@link com.taobao.itest.core.TestListener#afterTestMetod(TestContext)}.
 * Required JUnit 4.5+
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 */
@SuppressWarnings("deprecation")
public class RunAfterTestMethod extends Statement {

	private final Statement next;

	private final Object testInstance;

	private final Method testMethod;

	private final TestContextManager testContextManager;

	public RunAfterTestMethod(Statement next, Object testInstance, Method testMethod, TestContextManager testContextManager) {
		this.next = next;
		this.testInstance = testInstance;
		this.testMethod = testMethod;
		this.testContextManager = testContextManager;
	}

	@Override
	public void evaluate() throws Throwable {
		Throwable testException = null;
		List<Throwable> errors = new ArrayList<Throwable>();
		try {
			this.next.evaluate();
		} catch (Throwable e) {
			testException = e;
			errors.add(e);
		}

		try {
			this.testContextManager.afterTestMethod(this.testInstance, this.testMethod, testException);
		} catch (Exception e) {
			errors.add(e);
		}

		if (errors.isEmpty()) {
			return;
		}
		throw new MultipleFailureException(errors);
	}
}
