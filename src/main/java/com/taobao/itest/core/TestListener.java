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

/**
 * Run test as this order: Initialize test instance->
 * {@link #beforeTestClass(TestContext)}->{@link org.junit.BeforeClass} ->
 * {@link #prepareTestInstance(TestContext)} ->
 * {@link #beforeTestMethod(TestContext)} ->{@link org.junit.Before} ->
 * {@link org.junit.Test} ->{@link org.junit.After}->
 * {@link #afterTestMethod(TestContext)} ->{@link org.junit.AfterClass}->
 * {@link #afterTestClass(TestContext)}
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 * 
 */
public interface TestListener {

	void prepareTestInstance(TestContext testContext) throws Exception;

	void beforeTestClass(TestContext testContext) throws Exception;

	void beforeTestMethod(TestContext testContext) throws Exception;

	void afterTestMethod(TestContext testContext) throws Exception;

	void afterTestClass(TestContext testContext) throws Exception;

}
