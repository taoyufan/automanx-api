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
package com.taobao.itest.util.converter;

import java.math.BigDecimal;

import org.apache.commons.beanutils.Converter;

public class StringConvert implements Converter {

	@SuppressWarnings("rawtypes")
	public Object convert(Class type, Object value) {
		if (type == String.class) {
			if (value instanceof BigDecimal) {
				String tmp = value.toString();
				if (tmp.indexOf(".") != -1
						&& ((BigDecimal) value).doubleValue()
								- ((BigDecimal) value).intValue() == 0) {
					return String.valueOf(((BigDecimal) value).intValue());
				}
			}
			return (null != value) ? value.toString() : value;
		}
		return value;
	}

}
