package com.taobao.itest.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class CsvUtil {
	private final static String FONT_FORMAT = "utf-8";

	public static void main(String[] args) throws IOException {
		String[][] strings = getParasFromCsv("src/test/java/com/taobao/itest/aiyaya/test/1.csv", "testCsv");
		for (int i = 0; i < strings.length; i++) {
			System.out.println(Arrays.toString(strings[i]));
		}
	}

	public static String[][] getParasFromCsv(String filePath, String methodName) {
		try {
			return getParasFromCsv(filePath, methodName, FONT_FORMAT);
		} catch (IOException e) {
			throw new RuntimeException("No parameter values available for method: " + methodName + e.getMessage() + "请检查csv文件是否存在，路径：" + filePath + "或格式是否正确");
		}
	}

	/**
	 * 通过csv文件获取方法参数值
	 * 
	 * @param filePath
	 * @param methodName
	 * @param encode
	 * @return
	 * @throws IOException
	 */
	public static String[][] getParasFromCsv(String filePath, String methodName, String encode) throws IOException {
		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), encode));
		List<String[]> result = new ArrayList<String[]>();
		String[] nextLine = reader.readNext();
		String csvName = nextLine[0].trim();
		if (isMethod(csvName)) {
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine[0].trim().equals(methodName)) {
					String[] newLine = new String[nextLine.length - 1];
					System.arraycopy(nextLine, 1, newLine, 0, newLine.length);
					result.add(newLine);
				}
			}
		} else {
			while ((nextLine = reader.readNext()) != null) {
				result.add(nextLine);
			}
		}
		if (null != reader) {
			reader.close();
		}
		String[][] strings = listToArray(result);
		if (null == strings || strings.length == 0) {
			throw new RuntimeException("No parameter values available for method: " + methodName + "请检查csv文件是否存在，路径：" + filePath + "或格式是否正确");
		}
		return strings;
	}

	private static boolean isMethod(String csvName) {
		String methodName = "methodName";
		if (csvName.equalsIgnoreCase(methodName)) {
			return true;
		}
		int i = csvName.charAt(0);
		if (i == 65279) {// 65279为utf-8文件头
			return csvName.substring(1).equalsIgnoreCase(methodName);
		}
		return false;
	}

	private static String[][] listToArray(List<String[]> result) {
		if (null == result || result.isEmpty()) {
			return null;
		}
		int size = result.size();
		String[][] strings = new String[size][];
		for (int i = 0; i < size; i++) {
			strings[i] = result.get(i);
		}
		return strings;
	}
}
