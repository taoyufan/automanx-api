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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.ComparisonFailure;

public class AssertPropertiesEquals<T> extends BaseMatcher<T> {

	private Object actualObject;
	
	private String[] propertyNames ;
	
	
	public AssertPropertiesEquals(Object actualObject, String[] propertyNames) {
		super();
		this.actualObject = actualObject;
		this.propertyNames = propertyNames;
	}

	@Override
	public boolean matches(Object expected) {
		assertPropertiesEquals(expected, actualObject, propertyNames);
		return true ;
	}

	@Override
	public void describeTo(Description arg0) {

	}

	@Factory
	public static <T> Matcher<T> assertPropertiesEquals(Object actualObject, String... propertyName) {
		
		return new AssertPropertiesEquals<T>(actualObject,propertyName);
	}
	
	/**
	 * assertPropertiesEquals 
	 * Mainly used to compare two objects of the specified attribute value is equal,
	 * to support complex type of attribute comparison
	 * 
	 * <li>Eg: Person class contains a type of property Address: 
	 * 			address, Address type contains a String property: street.<br>
	 *     Then compare two Person objects are equal when the street can be written：<br>
	 *     AssertUtil.assertPropertiesEquals(person1,person2,"address.street")<br>
	 *     Written about the time when the form<br>
	 *     AssertUtil.assertPropertiesEquals(person1,person2,"address")<br>
	 *     Can compare the address of each property is equal to
	 * 
	 * @param expect  Expect the object
	 * @param actual  actual the object
	 * @param propertyNames  Need to compare the property values,
	 *  			support for complex types, you can enter multiple comma-separated string, 
	 *  			you can also enter the array
	 * 
	 */
	public static void assertPropertiesEquals(Object expect, Object actual,String... propertyNames){
		Assert.assertNotNull("bean is null"+expect, expect);
		Assert.assertNotNull("bean is null"+actual, actual);
		//Assert.assertFalse("to compare the property is empty", ArrayUtils.isEmpty(propertyNames));
		Object valueExpect=null;
		Object valueActual=null;
		List<String> errorMessages=new ArrayList<String>();
		/*
		 * If the attribute name is empty, then compare the two object
		 * the current value of all the fields.
		 * This parameter is empty,
		 * the situation has in the upper control and verified directly through reflection
		 */
		if ( ArrayUtils.isEmpty(propertyNames)) {
			PropertyDescriptor[] props = BeanUtilsBean.getInstance()
			.getPropertyUtils().getPropertyDescriptors(expect);
			propertyNames=new String[props.length-1];
			int j=0;
			for(int i=0;i<props.length;i++){
				if(!props[i].getName().equals("class")){
					propertyNames[j]=props[i].getName();
					j++;
				}
			}
		}
		//Cycle to compare different properties
			for (int i = 0; i < propertyNames.length; i++) {
				try {
					
					valueExpect=BeanUtilsBean.getInstance().getPropertyUtils().getProperty(expect, propertyNames[i]);
					valueActual=BeanUtilsBean.getInstance().getPropertyUtils().getProperty(actual, propertyNames[i]);
					if(valueExpect==null||valueActual==null)
						Assert.assertEquals(valueExpect, valueActual);
					else if(valueExpect.getClass().getName().startsWith("java.lang")||valueExpect instanceof Date){
						Assert.assertEquals(valueExpect, valueActual);
					/*
					 * If taken out of the object is still a complex structure, 
					 * then the recursive call, then the property name set to null, 
					 * compare the value of all the attributes of the object
					 */
					}else {
						assertPropertiesEquals(valueExpect,valueActual);
					}
				} catch (Exception e){
					throw new RuntimeException("error property"+propertyNames[i],e.fillInStackTrace());
				} catch (AssertionError err){
					errorMessages.add("\nexpect property："+propertyNames[i]+" value is ："+valueExpect+"，but actual value is："+valueActual);
					if(StringUtils.isNotBlank(err.getMessage())){
						errorMessages.add("\ncaused by："+err.getMessage());
					}
				}
			}
			if(errorMessages.size()>0){
				throw new ComparisonFailure(errorMessages.toString(),"","");
			}
	}
}
