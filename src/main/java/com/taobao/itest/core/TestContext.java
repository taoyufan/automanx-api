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
package com.taobao.itest.core;

import java.lang.reflect.Method;

import org.junit.runners.model.TestClass;

/**
 * 
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 * 
 */
public class TestContext {
	private final Class<?> testClass;

	private Object testInstance;

	private Method testMethod;

	private TestClass junitTestClass;
	private Throwable testException;

	public TestContext(Class<?> testClass, TestClass junitTestClass) {
		this.testClass = testClass;
		this.junitTestClass = junitTestClass;
	}

	public void updateState(Object testInstance, Method testMethod,
			Throwable testException) {
		this.testInstance = testInstance;
		this.testMethod = testMethod;
		this.testException = testException;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public Object getTestInstance() {
		return testInstance;
	}

	public Method getTestMethod() {
		return testMethod;
	}

	public TestClass getJunitTestClass() {
		return junitTestClass;
	}

	public Throwable getTestException() {
		return testException;
	}
}
