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

import org.unitils.reflectionassert.ReflectionComparatorMode;

public class Matchers extends org.hamcrest.Matchers {
	
	/**
	 * Assertion verification by Unitils two Object reflection
	 * @param <T>
	 * @param actual actual object value
	 * @param modes Comparison of the relaxed mode of reflection:</br>
	 * 	&nbsp;&nbsp;&nbsp;&nbsp;ReflectionComparatorMode.IGNORE_DEFAULTS,</br>
	 *  &nbsp;&nbsp;&nbsp;&nbsp;ReflectionComparatorMode.LENIENT_DATES,</br>
	 * 	&nbsp;&nbsp;&nbsp;&nbsp;ReflectionComparatorMode.LENIENT_ORDER.</br>
	 *  &nbsp;&nbsp;&nbsp;&nbsp; see: {@link ReflectionComparatorMode}
	 * @return A match to the mather.see: {@link org.hamcrest.Matcher}
	 * @author guangyuan
	 * @since 0.1-SNAPSHOT
	 */
	public static <T> org.hamcrest.Matcher<T> reflectionEquals(Object actual, ReflectionComparatorMode... modes) {
		return com.taobao.itest.matcher.AssertReflectionEquals.assertReflectionEquals(actual,modes);
	}
	
	/**
	 * Liberal assertion verification by the two reflex Unitils Object,
	 *  using LENIENT_ORDER, IGNORE_DEFAULTS two loose way.
	 * see:{@link ReflectionComparatorMode}
	 * @param <T> 
	 * @param actual  actual object value
	 * @return A match to the mather.see: {@link org.hamcrest.Matcher}
	 * @author guangyuan
	 * @since 0.1-SNAPSHOT
	 */
	public static <T> org.hamcrest.Matcher<T> reflectionLenientEquals(Object actual) {
		return com.taobao.itest.matcher.AssertLenientEquals.assertLenientEquals(actual);
	}

	/**
	 * Assertion two objects in the specified attributes are equal. 
	 * If the specified attribute comparison,
	 * will assert that two objects reflective properties of objects in each
	 * @param <T>
	 * @param actualObject actual object value
	 * @param propertyNames The property name to be compare
	 * @return A match to the mather.see: {@link org.hamcrest.Matcher}
	 * @author guangyuan
	 * @since 0.1-SNAPSHOT
	 */
	public static <T> org.hamcrest.Matcher<T> propertiesEquals(Object actualObject, String... propertyNames) {
		if(null == propertyNames || propertyNames.length == 0){
			//using reflection assert if no property will be compared
			return com.taobao.itest.matcher.AssertReflectionEquals.assertReflectionEquals(actualObject);
		}
		return com.taobao.itest.matcher.AssertPropertiesEquals.assertPropertiesEquals(actualObject, propertyNames);
	}
	
	/**
	 * Assertion two objects in the exclusion of the remaining property after the specified attributes are equal.
	 *  If the specified property excluded comparison, 
	 *  will assert that two objects reflective properties of objects in each.
	 * @param <T> 
	 * @param actualObject actual object value
	 * @param propertyName The property name to be compare
	 * @return A match to the mather.see: {@link org.hamcrest.Matcher}
	 * @author guangyuan
	 * @since 0.1-SNAPSHOT
	 */
	public static <T> org.hamcrest.Matcher<T> excludedPropertiesEquals(Object actualObject, String... propertyName) {
		if(null == propertyName || propertyName.length == 0){
			//using reflection assert if no exclude property will be compared
			return com.taobao.itest.matcher.AssertReflectionEquals.assertReflectionEquals(actualObject);
		}
		return com.taobao.itest.matcher.AssertExcludedPropertiesEquals.assertExcludedPropertiesEquals(actualObject, propertyName);
	}
}
