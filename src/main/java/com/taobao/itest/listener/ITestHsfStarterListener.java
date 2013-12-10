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

import java.lang.reflect.Method;

import com.taobao.itest.core.AbstractTestListener;
import com.taobao.itest.core.TestContext;
import com.taobao.itest.annotation.ITestHsfStarter;
import com.taobao.itest.util.AnnotationUtil;

public class ITestHsfStarterListener extends AbstractTestListener {
	@Override
	public void beforeTestClass(TestContext testContext) {
		Class<?> testClass = testContext.getTestClass();
		ITestHsfStarter itestHsfStarter = AnnotationUtil.findAnnotation(
				testClass, ITestHsfStarter.class);
		if (itestHsfStarter == null) {
			return;
		}
		if (!HsfStarterManager.started) {
			startHsf(testClass);
		}

	}

	private void startHsf(Class<?> testClass) {
		ITestHsfStarter hsfStarter = AnnotationUtil.findAnnotation(testClass,
				ITestHsfStarter.class);
		if (hsfStarter != null) {
			String path = hsfStarter.path();
			path = "".equals(path) ? null : path;
			String version = hsfStarter.version();
			version = "".equals(version) ? null : version;
			Class<?> hsfEasyStarterClass;
			try {
				hsfEasyStarterClass = Class
						.forName("com.taobao.hsf.hsfunit.HSFEasyStarter");
				Method startMethod = hsfEasyStarterClass.getMethod("start",
						String.class, String.class);
				startMethod.invoke(hsfEasyStarterClass.newInstance(), path,
						version);
				HsfStarterManager.started = true;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(
						"start hsf failed!" + e.getMessage(), e.getCause());
			}
			try {
				Thread.sleep(3000);// sleep for hsf started as a service
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}
