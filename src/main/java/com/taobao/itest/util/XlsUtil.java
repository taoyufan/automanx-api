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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.excel.XlsDataSet;
import org.springframework.util.ResourceUtils;

import com.taobao.itest.util.converter.IntegerConvert;
import com.taobao.itest.util.converter.StringConvert;

public class XlsUtil {

	static {
		ConvertUtils.register(new IntegerConvert(), Integer.class);
		ConvertUtils.register(new DateConverter(), Date.class);
		ConvertUtils.register(new StringConvert(), String.class);
	}

	/**
	 * The Excel sheet in a row specified in the data read into the specified
	 * type Object
	 * 
	 * @param excelDir
	 *            excel file path, such as: abc.xls this file exists called to
	 *            the same level directory Document * @param sheetName excel
	 *            sheet names corresponding to
	 * @param line
	 *            the line number to read the first line is 0, in turn
	 *            increasing
	 * @param bean
	 *            excel row data object matches
	 * @Return excel sheet of data to populate the specified type of object in
	 *         the Object
	 */
	public static Object readForObject(String excelDir, String sheetName,
			int line, Object bean) {
		Map<String, List<Map<String, Object>>> allData = readData(excelDir);
		return readForObject(allData, sheetName, line, bean);

	}

	/**
	 * The Excel sheet in a row specified in the data read into the specified
	 * type Object
	 * 
	 * @param allData
	 *            excel all the data
	 * @see #readData (String) Document * @param sheetName excel sheet names
	 *      corresponding to
	 * @param line
	 *            the line number to read the first line is 0, in turn
	 *            increasing
	 * @param bean
	 *            excel row data object matches
	 * @return excel sheet of data to populate the specified type of object in
	 *         the Object
	 */
	public static Object readForObject(
			Map<String, List<Map<String, Object>>> allData, String sheetName,
			int line, Object bean) {
		List<Map<String, Object>> sheet = allData.get(sheetName);

		if (line < 0 || line > sheet.size()) {
			throw new IllegalArgumentException(
					"line number is out of excel length.");
		}

		return (Object) PopulateUtil.populate(bean, sheet.get(line));

	}

	/**
	 * The Excel sheet in a row specified in the data read into the specified
	 * type Object
	 * 
	 * @param excelDir
	 *            excel file path, such as: abc.xls this file exists called to
	 *            the same level directory Document * @param sheetName excel
	 *            sheet names corresponding to
	 * @param line
	 *            the line number to read the first line is 0, in turn
	 *            increasing
	 * @param clazz
	 *            excel row data type matching clazz
	 * @return excel sheet of data to populate the specified type of object in
	 *         the Object
	 */
	public static <T> T readForObject(String excelDir, String sheetName,
			int line, Class<T> clazz) {

		Map<String, List<Map<String, Object>>> allData = readData(excelDir);
		return readForObject(allData, sheetName, line, clazz);
	}

	/**
	 * The Excel sheet in a row specified in the data read into the specified
	 * type Object
	 * 
	 * @param allData
	 *            excel all the data
	 * @see # readData (String) Document * @param sheetName excel sheet names
	 *      corresponding to
	 * @param line
	 *            the line number to read the first line is 0, in turn
	 *            increasing
	 * @param clazz
	 *            excel row data type matching clazz
	 * @return excel sheet of data to populate the specified type of object in
	 *         the Object
	 */
	public static <T> T readForObject(
			Map<String, List<Map<String, Object>>> allData, String sheetName,
			int line, Class<T> clazz) {

		List<Map<String, Object>> sheet = allData.get(sheetName);

		if (line < 0 || line > sheet.size()) {
			throw new IllegalArgumentException(
					"line number is out of excel length.");
		}

		return PopulateUtil.populate(clazz, sheet.get(line));

	}

	/**
	 * Specified in the Excel sheet data read into the list specified type
	 * Object
	 * 
	 * @param excelDir
	 *            excel file path, such as: abc.xls this file exists called to
	 *            the same level directory Document * @param sheetName excel
	 *            sheet names corresponding to
	 * @param bean
	 *            excel row data object matches
	 * @return excel sheet data from the specified fill to a list of type Object
	 */
	public static List<Object> readForObjectList(String excelDir,
			String sheetName, Object bean) {
		Map<String, List<Map<String, Object>>> allData = readData(excelDir);
		return readForObjectList(allData, sheetName, bean);

	}

	/**
	 * Specified in the Excel sheet data read into the list specified type
	 * Object
	 * 
	 * @param allData
	 *            excel all the data
	 * @see # readData (String) Document * @param sheetName excel sheet names
	 *      corresponding to
	 * @param bean
	 *            excel row data object matches
	 * @return excel sheet data from the specified fill to a list of type Object
	 */
	public static List<Object> readForObjectList(
			Map<String, List<Map<String, Object>>> allData, String sheetName,
			Object bean) {
		try {
			List<Map<String, Object>> sheet = allData.get(sheetName);

			List<Object> res = new ArrayList<Object>();

			for (int i = 0; i < sheet.size(); i++) {
				res.add((Object) PopulateUtil.populate(
						BeanUtils.cloneBean(bean), sheet.get(i)));
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Specified in the Excel sheet data read into the list specified type
	 * Object
	 * 
	 * @param excelDir
	 *            excel file path, such as: abc.xls this file exists called to
	 *            the same level directory Document * @param sheetName excel
	 *            sheet names corresponding to
	 * @param clazz
	 *            excel row data matching the type of clazz
	 * @return excel sheet data from the specified fill to a list of type Object
	 */
	public static <T> List<T> readForObjectList(String excelDir,
			String sheetName, Class<T> clazz) {
		Map<String, List<Map<String, Object>>> allData = readData(excelDir);
		return readForObjectList(allData, sheetName, clazz);
	}

	/**
	 * Specified in the Excel sheet data read into the list specified type
	 * Object
	 * 
	 * @param allData
	 *            excel all the data
	 * @see # readData (String) Document * @param sheetName excel sheet names
	 *      corresponding to
	 * @param clazz
	 *            excel row data matching the type of clazz
	 * @return excel sheet data from the specified fill to a list of type Object
	 */
	public static <T> List<T> readForObjectList(
			Map<String, List<Map<String, Object>>> allData, String sheetName,
			Class<T> clazz) {
		List<Map<String, Object>> sheet = allData.get(sheetName);

		List<T> res = new ArrayList<T>();

		for (int i = 0; i < sheet.size(); i++) {
			try {
				res.add((T) PopulateUtil.populate(clazz, sheet.get(i)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * To read Excel data into a Map structure
	 * 
	 * @param excelDir
	 *            excel file path, such as: abc.xls this file exists called to
	 *            the same level directory
	 * 
	 * @return converted Map
	 */
	public static Map<String, List<Map<String, Object>>> readData(
			String execlDir) {

		Map<String, List<Map<String, Object>>> allData = new HashMap<String, List<Map<String, Object>>>();

		List<Map<String, Object>> sheet = null;

		String excelRealPath = getExcelRealPath(execlDir);
		IDataSet dataSet;
		try {
			dataSet = new XlsDataSet(ResourceUtils.getFile(excelRealPath));

			// traverse all sheet
			String[] allDataTable = dataSet.getTableNames();

			ITable dataTable = null;
			ITableMetaData meta = null;
			Column[] columns = null;

			Map<String, Object> row = null;
			String columnName = null;
			Object obj = null;

			// read every Sheet
			for (int d = 0; d < allDataTable.length; d++) {
				dataTable = dataSet.getTable(allDataTable[d]);
				meta = dataTable.getTableMetaData();
				columns = meta.getColumns();

				sheet = new ArrayList<Map<String, Object>>();
				// read every line
				for (int k = 0; k < dataTable.getRowCount(); k++) {
					row = Collections
							.synchronizedMap(new CamelCasingHashMap<String, Object>());
					;
					for (int i = 0; i < columns.length; i++) {
						columnName = columns[i].getColumnName();
						obj = dataTable.getValue(k, columnName);
						row.put(columnName, obj);
					}
					sheet.add(k, row);
				}

				allData.put(allDataTable[d], sheet);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return allData;

	}

	private static String getTestClassPackage() {
		int atLeastCallCount = 5;
		StackTraceElement stack[] = (new Throwable()).getStackTrace();

		/**
		 * Back call stack will know the caller 0：getTestClassName
		 * 1：getExcelRealPath 2: readData 3: readForObject 4: test class
		 * 
		 */
		if (stack.length < atLeastCallCount) {
			// Not less than 5 times the call, or take less than a call to the
			// test class
			return null;
		} else {
			StackTraceElement ste = stack[atLeastCallCount - 1];
			return ste.getClassName().substring(0,
					ste.getClassName().lastIndexOf('.'));
		}
	}

	/**
	 * Eventually converted into a unified type path: classpath :***, to the
	 * Spring to solve
	 * 
	 * @param execlDir
	 * @return
	 */
	private static String getExcelRealPath(String execlDir) {
		if (StringUtils.isBlank(execlDir)) {
			throw new IllegalArgumentException(
					"Excel file path must be clearly specified!");
		} else if (execlDir.startsWith("classpath:")) {
			return execlDir;
		} else if (execlDir.startsWith("/")) {
			return StringUtils.replaceOnce(execlDir, "/", "classpath:");
		} else {
			String testClass = getTestClassPackage();
			if (null == testClass) {
				// Call the test class not found
				throw new IllegalArgumentException(
						"There are some errors in method invode.Please check StackTrace.");
			}

			return StringUtils.join(new String[] { "classpath:",
					StringUtils.replace(testClass, ".", "/"), "/", execlDir });
		}
	}

}
