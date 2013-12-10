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
package com.taobao.itest.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 */
public class PropertiesUtil {
	/**
	 * The properties file key-value information into a Map, if the key contains
	 * a underline into the next hump style change
	 * 
	 * @param resourceName
	 * @param characterSet
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> loadProperties(String resourceName) {
		return loadProperties(resourceName, "UTF-8");
	}

	/**
	 * The properties file key-value information into a Map, if the key contains
	 * a underline into the next hump style change
	 * 
	 * @param resourceName
	 * @param characterSet
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> loadProperties(String resourceName,
			String characterSet) {
		Properties properties;
		Map<String, String> paramsMap = Collections
				.synchronizedMap(new CamelCasingHashMap<String, String>());
		try {
			properties = PropertiesLoaderUtils.loadAllProperties(resourceName);

			for (Enumeration<?> keys = properties.keys(); keys
					.hasMoreElements();) {
				String key = (String) keys.nextElement();
				paramsMap.put(
						key,
						getValue(properties.getProperty(key).trim(),
								characterSet));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return paramsMap;
	}

	private static String getValue(String s, String characterSet)
			throws UnsupportedEncodingException {
		if (characterSet != null) {
			return new String(s.getBytes("ISO-8859-1"), characterSet);
		} else {
			return s;
		}
	}

}
