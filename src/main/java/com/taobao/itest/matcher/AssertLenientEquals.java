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

public class AssertLenientEquals<T> extends BaseMatcher<T> {

	private Object actual;
	
	public AssertLenientEquals(Object actual) {
		super();
		this.actual = actual;
	}

	@Override
	public boolean matches(Object excepted) {
		try {
			ReflectionAssert.assertLenientEquals(excepted, actual);
		} catch (AssertionFailedError e) {
			throw new java.lang.AssertionError(e.getMessage());
		}
		return true;
	}

	@Override
	public void describeTo(Description arg0) {
		/**
		 * if ReflectionAssert is error ,the following message will not be printed,
		 * but throw AssertionError message instead. 
		 */
	}

	@Factory
    public static <T> Matcher<T> assertLenientEquals(Object actual) {
		
        return new AssertLenientEquals<T>(actual);
    }
	
	public Object getActual() {
		return actual;
	}

	public void setActual(Object actual) {
		this.actual = actual;
	}
	 
	
}
