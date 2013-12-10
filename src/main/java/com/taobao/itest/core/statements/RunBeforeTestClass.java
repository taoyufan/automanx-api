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

import org.junit.runners.model.Statement;

import com.taobao.itest.core.TestContext;
import com.taobao.itest.core.TestContextManager;

/**
 * Statement which runs
 * {@link com.taobao.itest.core.TestListener#beforeTestClass(TestContext)} then
 * {@link org.junit.BeforeClass} . Required JUnit 4.5+
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 */
public class RunBeforeTestClass extends Statement {

	private final Statement next;

	private final TestContextManager testContextManager;

	public RunBeforeTestClass(Statement next,
			TestContextManager testContextManager) {
		this.next = next;
		this.testContextManager = testContextManager;
	}

	@Override
	public void evaluate() throws Throwable {
		this.testContextManager.beforeTestClass();
		this.next.evaluate();
	}

}
