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
package com.taobao.itest.tddl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.taobao.itest.BaseTestCase;
import com.taobao.itest.annotation.ITestDataSet;
import com.taobao.itest.annotation.ITestSpringContext;

@ITestSpringContext({ "/tddl/tddl-rule-readwrite.xml" })
public class TddlTest extends BaseTestCase {
	/**
	 * 不分库不分表，原方式
	 */
	@Test
	@ITestDataSet(dsNames = { "db1" }, teardownOperation = "NONE")
	public void test01_tddl_不分库不分表() {
		List<?> a = jdbcTemplate1.queryForList("select * from user where id = '1'");
		assertThat(a, notNullValue());
		assertThat(a.size(), is(1));
	}

	/**
	 * 分库分表
	 */
	@Test
	@ITestDataSet(dsNames = { "root" }, teardownOperation = "NONE")
	public void test01_tddl_分库分表使用root配置() {
		List<?> a = jdbcTemplate2.queryForList("select * from user_0003 where id = '3'");
		assertThat(a, notNullValue());
		assertThat(a.size(), is(1));
	}
}
