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

package com.taobao.itest.matcher;

import junit.framework.AssertionFailedError;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

public class AssertReflectionEquals<T> extends BaseMatcher<T> {

	private Object actual ;
	
	private ReflectionComparatorMode[] modes ;
	
	public AssertReflectionEquals(Object actual,
			ReflectionComparatorMode[] modes) {
		super();
		this.actual = actual;
		this.modes = modes;
	}

	@Override
	public boolean matches(Object expected) {
		try {
			ReflectionAssert.assertReflectionEquals(expected, actual,modes);
		} catch (AssertionFailedError e) {
			throw new java.lang.AssertionError(e.getMessage());
		}
		return true;
	}

	@Override
	public void describeTo(Description arg0) {
		
	}
	
	@Factory
	public static <T> Matcher<T> assertReflectionEquals(Object actual,ReflectionComparatorMode... modes) {
		
		return new AssertReflectionEquals<T>(actual,modes);
	}

}
