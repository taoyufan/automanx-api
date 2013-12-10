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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

public class DateConverter implements Converter {
	private static String resource = "envParams";
	private static String defaultDateTimePattern = null;
	private static String defaultDatePattern = null;

	public static synchronized String getDatePattern() {
		Locale locale = LocaleContextHolder.getLocale();
		try {
			defaultDatePattern = ResourceBundle.getBundle(resource, locale)
					.getString("datePattern");
		} catch (MissingResourceException mse) {
			defaultDatePattern = "yyyy-MM-dd";
		}

		return defaultDatePattern;
	}

	public static synchronized String getDateTimePattern() {
		Locale locale = LocaleContextHolder.getLocale();
		try {
			defaultDateTimePattern = ResourceBundle.getBundle(resource, locale)
					.getString("dateTimePattern");
		} catch (MissingResourceException mse) {
			defaultDateTimePattern = "yyyy-MM-dd HH:mm:ss";
		}

		return defaultDateTimePattern;
	}

	@Override
	public Object convert(@SuppressWarnings("rawtypes") Class type, Object value) {
		if (value == null) {
			return null;
		} else if (type == Timestamp.class) {
			return convertToDate(type, value);
		} else if (type == Date.class) {
			return convertToDate(type, value);
		} else if (type == String.class) {
			return convertToString(type, value);
		} else {
			return value;
		}

		// throw new ConversionException("Could not convert "
		// + value.getClass().getName() + " to " + type.getName());
	}

	protected Object convertToDate(@SuppressWarnings("rawtypes") Class type,
			Object value) {
		if (value instanceof String) {
			try {
				if (StringUtils.isEmpty(value.toString())) {
					return null;
				}
				String pattern = getDatePattern();
				if (value.toString().contains(":")) {
					pattern = getDateTimePattern();
				}
				DateFormat df = new SimpleDateFormat(pattern);
				Date date = df.parse((String) value);
				if (type.equals(Timestamp.class)) {
					return new Timestamp(date.getTime());
				}
				return date;
			} catch (Exception pe) {
				pe.printStackTrace();
				throw new ConversionException("Error converting String to Date");
			}
		} else if (value instanceof Date) {
			return value;
		} else if (value instanceof Long) {
			return getGmtDate((Long) value);
		}

		throw new ConversionException("Could not convert "
				+ value.getClass().getName() + " to " + type.getName());
	}

	public Object convertToString(@SuppressWarnings("rawtypes") Class type,
			Object value) {

		if (value instanceof Date) {
			DateFormat df = new SimpleDateFormat(getDatePattern());
			if (value instanceof Timestamp) {
				df = new SimpleDateFormat(getDateTimePattern());
			}

			try {
				return df.format(value);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ConversionException("Error converting Date to String");
			}
		} else {
			return value.toString();
		}
	}

	public static String dateToStr(Date date, String pattern) {
		if (pattern == null) {
			pattern = "yyyy-MM-dd HH:mm:ss.SSS";
		}
		DateFormat ymdhmsFormat = new SimpleDateFormat(pattern);

		return ymdhmsFormat.format(date);
	}

	public static Date strToDate(String str, String pattern)
			throws ParseException {
		if (pattern == null) {
			pattern = "yyyy-MM-dd HH:mm:ss.SSS";
		}
		DateFormat ymdhmsFormat = new SimpleDateFormat(pattern);
		return ymdhmsFormat.parse(str);
	}

	public static Date getToday() {
		Calendar ca = Calendar.getInstance();
		return ca.getTime();
	}

	public static Date mkDate(int year, int month, int date) {
		Calendar ca = Calendar.getInstance();
		ca.set(year, month - 1, date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		sdf.format(ca.getTime());
		return ca.getTime();
	}

	/**
	 * get GMT Time
	 * 
	 * @param calendar
	 * @return
	 */
	public Date getGmtDate(Long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int offset = calendar.get(Calendar.ZONE_OFFSET) / 3600000
				+ calendar.get(Calendar.DST_OFFSET) / 3600000;
		calendar.add(Calendar.HOUR, -offset);
		Date date = calendar.getTime();

		return date;
	}

}
