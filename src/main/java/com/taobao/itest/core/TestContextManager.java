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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.junit.runners.model.TestClass;
import org.springframework.beans.BeanUtils;

import com.taobao.itest.util.AnnotationUtil;

/**
 * 
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 * 
 */
public class TestContextManager {
	private final TestContext testContext;

	private final List<TestListener> testListeners = new ArrayList<TestListener>();

	public TestContextManager(TestClass testClass) {
		this.testContext = new TestContext(testClass.getJavaClass(), testClass);
		registerTestListeners(retrieveTestListeners(testClass.getJavaClass()));
	}

	public void registerTestListeners(TestListener... TestListeners) {
		for (TestListener listener : TestListeners) {
			this.testListeners.add(listener);
		}
	}

	private TestListener[] retrieveTestListeners(Class<?> clazz) {
		Class<TestListeners> annotationType = TestListeners.class;
		@SuppressWarnings("rawtypes")
		List<Class> classesAnnotationDeclared = AnnotationUtil
				.findClassesAnnotationDeclaredWith(clazz, annotationType);
		List<Class<? extends TestListener>> classesList = new ArrayList<Class<? extends TestListener>>();
		for (Class<?> classAnnotationDeclared : classesAnnotationDeclared) {
			TestListeners testListeners = classAnnotationDeclared
					.getAnnotation(annotationType);
			Class<? extends TestListener>[] valueListenerClasses = testListeners
					.value();
			Class<? extends TestListener>[] listenerClasses = testListeners
					.listeners();
			if (!ArrayUtils.isEmpty(valueListenerClasses)
					&& !ArrayUtils.isEmpty(listenerClasses)) {
				String msg = String
						.format("Test class [%s] has been configured with @TestListeners' 'value' [%s] and 'listeners' [%s] attributes. Use one or the other, but not both.",
								classAnnotationDeclared,
								ArrayUtils.toString(valueListenerClasses),
								ArrayUtils.toString(listenerClasses));
				throw new RuntimeException(msg);
			} else if (!ArrayUtils.isEmpty(valueListenerClasses)) {
				listenerClasses = valueListenerClasses;
			}

			if (listenerClasses != null) {
				classesList
						.addAll(0,
								Arrays.<Class<? extends TestListener>> asList(listenerClasses));
			}
			if (!testListeners.inheritListeners()) {
				break;
			}
		}

		List<TestListener> listeners = new ArrayList<TestListener>(
				classesList.size());
		for (Class<? extends TestListener> listenerClass : classesList) {
			listeners.add((TestListener) BeanUtils
					.instantiateClass(listenerClass));
		}
		return listeners.toArray(new TestListener[listeners.size()]);
	}

	public void prepareTestInstance(Object testInstance) throws Exception {

		getTestContext().updateState(testInstance, null, null);

		for (TestListener testListener : getTestListeners()) {
			testListener.prepareTestInstance(getTestContext());
		}
	}

	public void beforeTestClass() throws Exception {
		getTestContext().updateState(null, null, null);

		for (TestListener testListener : getTestListeners()) {
			testListener.beforeTestClass(getTestContext());
		}
	}

	public void beforeTestMethod(Object testInstance, Method testMethod)
			throws Exception {
		getTestContext().updateState(testInstance, testMethod, null);

		for (TestListener testListener : getTestListeners()) {
			testListener.beforeTestMethod(getTestContext());
		}
	}

	public void afterTestMethod(Object testInstance, Method testMethod,
			Throwable exception) throws Exception {
		getTestContext().updateState(testInstance, testMethod, exception);
		for (TestListener testListener : getReversedTestListeners()) {
			testListener.afterTestMethod(getTestContext());
		}
	}

	public void afterTestClass() throws Exception {
		getTestContext().updateState(null, null, null);
		for (TestListener testListener : getReversedTestListeners()) {
			testListener.afterTestClass(getTestContext());
		}
	}

	private List<TestListener> getReversedTestListeners() {
		List<TestListener> listenersReversed = new ArrayList<TestListener>(
				getTestListeners());
		Collections.reverse(listenersReversed);
		return listenersReversed;
	}

	public TestContext getTestContext() {
		return testContext;
	}

	public List<TestListener> getTestListeners() {
		return Collections.unmodifiableList(testListeners);
	}

}
