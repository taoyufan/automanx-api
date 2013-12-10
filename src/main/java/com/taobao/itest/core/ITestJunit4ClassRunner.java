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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import com.taobao.itest.annotation.Retry;
import com.taobao.itest.core.statements.RunAfterTestClass;
import com.taobao.itest.core.statements.RunAfterTestMethod;
import com.taobao.itest.core.statements.RunBeforeTestClass;
import com.taobao.itest.core.statements.RunBeforeTestMethod;

/**
 * 
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * @author <a href="mailto:yufan.yq@taobao.com">yufan</a>
 * 
 */
@SuppressWarnings("deprecation")
public class ITestJunit4ClassRunner<T> extends BlockJUnit4ClassRunner {
	private final TestContextManager testContextManager;
	private int retryTime = 1;
	private int now = 0;
	private Set<Class<? extends Throwable>> exceptions;

	public ITestJunit4ClassRunner(Class<?> klass) throws InitializationError {
		super(klass);
		testContextManager = new TestContextManager(getTestClass());
	}

	protected final TestContextManager getTestContextManager() {
		return this.testContextManager;
	}

	@Override
	protected Statement withBeforeClasses(Statement statement) {
		Statement junitBeforeClasses = super.withBeforeClasses(statement);
		return new RunBeforeTestClass(junitBeforeClasses, getTestContextManager());
	}

	@Override
	protected Statement withAfterClasses(Statement statement) {
		Statement junitAfterClasses = super.withAfterClasses(statement);
		return new RunAfterTestClass(junitAfterClasses, getTestContextManager());
	}

	@Override
	protected Object createTest() throws Exception {
		Object testInstance = super.createTest();
		getTestContextManager().prepareTestInstance(testInstance);
		return testInstance;
	}

	@Override
	protected Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
		Statement junitBefores = super.withBefores(frameworkMethod, testInstance, statement);
		return new RunBeforeTestMethod(junitBefores, testInstance, frameworkMethod.getMethod(), getTestContextManager());
	}

	@Override
	protected Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
		Statement junitAfters = super.withAfters(frameworkMethod, testInstance, statement);
		return new RunAfterTestMethod(junitAfters, testInstance, frameworkMethod.getMethod(), getTestContextManager());
	}

	/**
	 * @author yufan.yq
	 */

	// @Override
	// public void run(final RunNotifier notifier) {
	// notifier.addListener();
	// super.run(notifier);
	// }

	@Override
	protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
		Description description = describeChild(method);
		if (method.getAnnotation(Ignore.class) != null) {
			notifier.fireTestIgnored(description);
		} else if (shouldRetry(method)) {// 需要重试，则走重试的逻辑
			runLeafRetry(methodBlock(method), description, notifier);
		} else {// 不需要重试，则走原来的逻辑
			runLeaf(methodBlock(method), description, notifier);
		}
	}

	/**
	 * 是否需要重试，如果需要重试，会初始化retryTime和now
	 * 
	 * @param method
	 *            测试方法
	 * @return
	 */
	private boolean shouldRetry(final FrameworkMethod method) {
		Retry retry = null;
		// 类上有该注解，类上的所有方法都生效，子类会覆盖父类
		retry = getClassRetry(getTestClass().getJavaClass());
		// 方法上的注解会覆盖类上的
		Retry annotation = method.getAnnotation(Retry.class);
		if (annotation != null) {
			retry = annotation;
		}
		if (retry != null) {
			if (retry.value() > 0) {
				retryTime = retry.value();
			} else {// 如果用户将注解的value值设为小于1的数，默认定为1
				retryTime = 1;
			}
			now = 0;
			Class<? extends Throwable>[] exs = retry.exceptions();
			// 初始化exceptions
			exceptions = new HashSet<Class<? extends Throwable>>(exs.length);
			for (Class<? extends Throwable> e : exs) {
				exceptions.add(e);
			}
			return true;
		}
		return false;
	}

	/**
	 * 获取类上的Retry注解，子类会覆盖父类
	 * 
	 * @param mClass
	 * @return
	 */
	private Retry getClassRetry(Class<?> mClass) {
		if (mClass == null || mClass == Object.class) {
			return null;
		}
		Retry retry = null;
		Annotation[] annotations = mClass.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof Retry) {
				retry = (Retry) annotation;
				break;
			}
		}
		if (null == retry) {
			retry = getClassRetry(mClass.getSuperclass());
		}
		return retry;
	}

	/**
	 * 重试的话执行本方法
	 * 
	 * @author yufan.yq
	 */
	protected final void runLeafRetry(Statement statement, Description description, RunNotifier notifier) {
		EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
		eachNotifier.fireTestStarted();
		try {
			retryRun(statement);
		} catch (AssumptionViolatedException e) {
			eachNotifier.addFailedAssumption(e);
		} catch (Throwable e) {
			eachNotifier.addFailure(e);
		} finally {
			eachNotifier.fireTestFinished();
		}
	}

	/**
	 * 断言失败后重试的执行方法
	 * 
	 * @param statement
	 * @throws Throwable
	 */
	private void retryRun(Statement statement) throws Throwable {
		try {
			now++;// 当前执行次数递增
			statement.evaluate();
		} catch (Throwable e) {
			if (contains(e) && now < retryTime) {
				// 属于要重试的异常,还在重试范围内则重试，否则抛出断言异常
				retryRun(statement);
			} else {
				throw e;
			}
		}
	}

	private boolean contains(Throwable e) {
		if (e instanceof MultipleFailureException) {
			MultipleFailureException exception = (MultipleFailureException) e;
			List<Throwable> list = exception.getFailures();
			return contains(list.get(0).getClass());
		}
		return contains(e.getClass());
	}

	/**
	 * 递归判断是否存在某异常
	 * 
	 * @param mClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean contains(Class<? extends Throwable> mClass) {
		if (mClass.equals(Throwable.class)) {
			return false;
		}

		if (exceptions.contains(mClass)) {
			return true;
		} else {
			return contains((Class<? extends Throwable>) mClass.getSuperclass());
		}
	}
}
