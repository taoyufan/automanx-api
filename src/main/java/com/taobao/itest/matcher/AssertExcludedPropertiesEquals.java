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


import java.beans.PropertyDescriptor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.ArrayUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class AssertExcludedPropertiesEquals<T> extends BaseMatcher<T> {

	private Object actualObject;
	
	private String[] propertyNames ;
	
	
	public AssertExcludedPropertiesEquals(Object actualObject, String[] propertyNames) {
		super();
		this.actualObject = actualObject;
		this.propertyNames = propertyNames;
	}

	@Override
	public boolean matches(Object expected) {
		assertPropertiesEqualsIgnoreOthers(expected, actualObject, propertyNames);
		return true ;
	}

	@Override
	public void describeTo(Description arg0) {

	}

	@Factory
	public static <T> Matcher<T> assertExcludedPropertiesEquals(Object actualObject, 
			String... propertyName) {
		
		return new AssertExcludedPropertiesEquals<T>(actualObject,propertyName);
	}
	
	/**
	 * assertPropertiesEqualsIgnoreOthers 
	 * Mainly used to compare the two bean, 
	 * in addition to ignoring the property, other various attributes are equal
	 * 
	 *
	 * @param expect
	 * @param actual
	 * @param ignoredProperties   ignoring property,
	 *
	 * 
	 */
	public static void assertPropertiesEqualsIgnoreOthers(Object expect, Object actual,
							String... ignoredProperties){
		if(!ArrayUtils.isEmpty(ignoredProperties)){
			PropertyDescriptor[] props = BeanUtilsBean.getInstance()
			.getPropertyUtils().getPropertyDescriptors(expect);
			String	propertyNames="";
			for(int i=0;i<props.length;i++){
				/*
				 * The current bean in the property not for the "class", 
				 * nor is ignoredProperties values, this time to put in the comparison
				 */
				if(!props[i].getName().equals("class")
						&& !ArrayUtils.contains(ignoredProperties, props[i].getName())){
					propertyNames+=props[i].getName()+",";
				}
			}
			/*	According to include comparison  */
			AssertPropertiesEquals.assertPropertiesEquals(expect, actual,
					propertyNames.substring(0, propertyNames.length()-1).split(","));
		}else{
			/*
			 * This parameter is empty, the situation has in the upper control,
			 * directly through the reflection verified. 
			 * But to prevent each other's direct calls, and still retain
			 */
			AssertPropertiesEquals.assertPropertiesEquals(expect, actual);
		}
	}
}
