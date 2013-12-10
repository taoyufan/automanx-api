package com.taobao.itest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runners.model.TestClass;

import com.taobao.itest.annotation.DataProvider;
import com.taobao.itest.listener.ResourceLocationProcessingUtil;

public class DataPrepareUtils {
	protected static final Log logger = LogFactory
			.getLog(DataPrepareUtils.class);
	private final static String TESTCASE = "testcase";
	private final static String METHOD = "method";
	private final static String VALUE = "value";
	private final static String ENV_VALUE;
	private final static String ENV = "env";
	private final static Set<String> caseLevels = new HashSet<String>();
	private final static String DATA_PREPARE = "DataPrepare";
	private final static String CASE_LEVEL = "case_level";
	static {
		String env = System.getProperty(ENV);
		if (null == env) {
			env = "daily";
		}
		ENV_VALUE = env;
		String caseLevelStr = System.getProperty(CASE_LEVEL);
		if(null != caseLevelStr){
			String[] ss = caseLevelStr.split("#");
			for (String string : ss) {
				caseLevels.add(string);
			}
		}
	}
	private static boolean isCaseLevel(String caseLevel){
		if(null == caseLevel || caseLevel.trim().equals("") || caseLevels.isEmpty()){
			return true;
		}
		return caseLevels.contains(caseLevel);
	}
	/**
	 * 获取数据初始化方面的SQL语句，如BeforeClass,AfterClass,Before,After
	 * 
	 * @return Map<String,List<Object>>
	 */
	public static Map<String, List<Object>> getPrepareDatas(String path) {

		Map<String, List<Object>> resultMap = new HashMap<String, List<Object>>();
		List<Object> list = null;
		Element root = getRoot(path);
		if (null != root) {
			for (Iterator<?> i = root.elementIterator(DATA_PREPARE); i
					.hasNext();) {
				Element dataPrepare = (Element) i.next();
				if (!dataPrepare.attributeValue(ENV)
						.equalsIgnoreCase(ENV_VALUE)) {
					continue;
				}
				@SuppressWarnings("unchecked")
				List<Element> datas = (List<Element>) dataPrepare.elements();

				for (int j = 0, m = datas.size(); j < m; j++) {
					list = new ArrayList<Object>();
					Element record = datas.get(j);
					@SuppressWarnings("unchecked")
					List<Element> paras = record.elements();
					for (int k = 0; k < paras.size(); k++) {
						list.add(paras.get(k).getText());
					}
					resultMap.put(datas.get(j).getName(), list);
				}
			}
		} else {
			logger.error("getPrepareDatas获取结果为空！");
		}
		return resultMap;
	}

	/**
	 * 获取Xml的根节点Root
	 * 
	 * @return Element
	 */
	private static Element getRoot(String filePath) {
		if (null == filePath || filePath.trim().equals("")) {
			logger.error("路径为空");
			return null;
		}
		SAXReader saxReader = new SAXReader();
		Document document = null;
		File file = new File(filePath);
		if (file.exists()) {
			try {
				InputStreamReader ir = new InputStreamReader(
						new FileInputStream(file), "UTF-8");
				document = saxReader.read(ir);
				ir.close();
				return document.getRootElement();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			logger.error("Can't find the file : " + filePath);
		}
		return null;
	}

	/**
	 * 
	 * @param testClass
	 * @param method
	 * @return
	 */
	public static Object[][] getParams(TestClass testClass, Method method) {
		DataProvider provider = method.getAnnotation(DataProvider.class);
		if (null == provider) {
			throw new RuntimeException(
					"No parameter values available for method: " + method
							+ " 如果要使用数据驱动，请为测试方法添加注解@DataProvider！！");
		}
		if (!provider.location().equals("")) {// 处理文件
			return getParamsFromFile(testClass, method, provider);
		}
		String fieldName = null;
		if (provider.fieldName().equals("")) {
			fieldName = method.getName();
		} else {
			fieldName = provider.fieldName();
		}
		return getParasFromFieldName(testClass, method, fieldName);
	}

	/**
	 * 对于文件准备的以下类型，支持自动转换
	 * <ul>
	 * <li>java.lang.BigDecimal (no default value)</li>
	 * <li>java.lang.BigInteger (no default value)</li>
	 * <li>boolean and java.lang.Boolean (default to false)</li>
	 * <li>byte and java.lang.Byte (default to zero)</li>
	 * <li>char and java.lang.Character (default to a space)</li>
	 * <li>java.lang.Class (no default value)</li>
	 * <li>double and java.lang.Double (default to zero)</li>
	 * <li>float and java.lang.Float (default to zero)</li>
	 * <li>int and java.lang.Integer (default to zero)</li>
	 * <li>long and java.lang.Long (default to zero)</li>
	 * <li>short and java.lang.Short (default to zero)</li>
	 * <li>java.lang.String (default to null)</li>
	 * <li>java.io.File (no default value)</li>
	 * <li>java.net.URL (no default value)</li>
	 * <li>java.sql.Date (no default value)</li>
	 * <li>java.sql.Time (no default value)</li>
	 * <li>java.sql.Timestamp (no default value)</li>
	 * </ul>
	 * 
	 * @param testClass
	 * @param method
	 * @param provider
	 * @return
	 */
	private static Object[][] getParamsFromFile(TestClass testClass,
			Method method, DataProvider provider) {
		if (!provider.fieldName().equals(""))
			logger.warn("用例\"" + method.getName()
					+ "\"选择了文件数据驱动的形式，fieldName提供的数据将被忽略");
		String filePath = ResourceLocationProcessingUtil.modifyLocations(
				testClass.getJavaClass(), provider.location())[0].replace(
				"classpath:", "target/test-classes");
		String[][] params = null;
		if (filePath.endsWith("xml")) {
			params = getParasFromXML(filePath, method.getName());
		} else if (filePath.endsWith("csv")) {
			params = CsvUtil.getParasFromCsv(filePath, method.getName());
		} else {
			throw new RuntimeException(
					"No parameter values available for method: " + method
							+ ",请使用合法的文件路径，csv或者xml");
		}
		return parseStrings(params, method);
	}

	/**
	 * 将String二维数组，根据方法的参数类型转换为相应的Object二维数组
	 * 
	 * @param params
	 * @param method
	 * @return
	 */
	private static Object[][] parseStrings(String[][] params, Method method) {
		if (null == params || params.length == 0) {
			return null;
		}
		Object[][] objects = new Object[params.length][];
		Class<?>[] parameterTypes = method.getParameterTypes();
		Converter[] parameterConverters = getConverters(parameterTypes);
		for (int i = 0; i < params.length; i++) {
			objects[i] = parseStrings(params[i], parameterTypes,
					parameterConverters);
		}
		return objects;
	}

	/**
	 * 将String数组，根据方法的参数类型集合、相应的转换器集合，转换为相应的Object数组
	 * 
	 * @param strings
	 * @param parameterTypes
	 * @param converters
	 * @return
	 */
	private static Object[] parseStrings(String[] strings,
			Class<?>[] parameterTypes, Converter[] converters) {
		int len = Math.min(strings.length, converters.length);
		Object[] result = new Object[len];
		for (int i = 0; i < len; i++) {
			result[i] = converters[i].convert(parameterTypes[i], strings[i]);
		}
		return result;
	}

	/**
	 * 根据Class类型集合得到相应的转换器
	 * 
	 * @param parameterTypes
	 * @return
	 */
	private static Converter[] getConverters(Class<?>[] parameterTypes) {
		int len = parameterTypes.length;
		Converter[] parameterConverters = new Converter[len];
		for (int i = 0; i < len; i++) {
			parameterConverters[i] = ConvertUtils.lookup(parameterTypes[i]);
		}
		return parameterConverters;
	}

	/**
	 * 根据xml文件生成相应的String二维数组数据
	 * 
	 * @param path
	 * @param methodName
	 * @return
	 */
	private static String[][] getParasFromXML(String path, String methodName) {
		Element root = getRoot(path);
		if (null != root) {
			for (Iterator<?> i = root.elementIterator(TESTCASE); i.hasNext();) {
				Element testcase = (Element) i.next();
				if (!testcase.attributeValue(ENV)
						.equalsIgnoreCase(ENV_VALUE)) {
					continue;
				}
				if (!methodName.equals(testcase.attributeValue(METHOD))) {
					continue;
				}
				@SuppressWarnings("unchecked")
				List<Element> records = (List<Element>) testcase.elements();
				int m = records.size();
				String[][] parentList = new String[m][];
				String[] list = null;
				int j = 0;
				for (int t = 0; j < m; j++, t++) {
					Element record = records.get(t);
					if(!isCaseLevel(record.attributeValue(CASE_LEVEL))){
						j--;
						m--;
						continue;
					}
					String run = record.attributeValue("run");
					if (null != run && run.equalsIgnoreCase("false")) {
						j--;
						m--;
						continue;
					}
					@SuppressWarnings("unchecked")
					List<Element> paras = record.elements();
					int n = paras.size();
					list = new String[n];
					for (int k = 0; k < n; k++) {
						Element parameter = paras.get(k);
						String value = parameter.attributeValue(VALUE);
						if (null == value) {
							@SuppressWarnings("unchecked")
							List<Element> elements = parameter.elements();
							if (null == elements || elements.isEmpty()) {
								value = parameter.getText();
							} else {
								Element cdata = elements.get(0);
								if (cdata instanceof CDATA) {
									value = ((CDATA) cdata).getText();
								}
							}
						}
						list[k] = value;
					}
					parentList[j] = list;
				}
				if (j == parentList.length) {
					return parentList;
				}
				String[][] parentList2 = new String[j][];
				System.arraycopy(parentList, 0, parentList2, 0, j);
				return parentList2;
			}
		}
		throw new RuntimeException("No parameter values available for method: "
				+ methodName + "请检查xml文件是否存在，路径：" + path + "或格式是否正确");

	}

	/**
	 * 通过属性获取参数值
	 * 
	 * @param ftc
	 * @param method
	 * @param fieldName
	 * @return
	 */
	private static Object[][] getParasFromFieldName(final TestClass ftc,
			Method method, String fieldName) {
		Object fTarget = null;
		try {
			fTarget = new ReflectiveCallable() {
				@Override
				protected Object runReflectiveCall() throws Throwable {
					return ftc.getOnlyConstructor().newInstance();
				}
			}.run();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Field targetField = null;
		try {
			targetField = ftc.getJavaClass().getField(fieldName);
		} catch (SecurityException e) {
			logger.error(e.getMessage());
		} catch (NoSuchFieldException e) {
			logger.error(e.getMessage());
		}
		if (null == targetField) {
			throw new RuntimeException(
					"No parameter values available for method: "
							+ method
							+ "请注意：数据准备的field是声明必须是public，@DataProvider(fieldName='')值为对应的fieldName"
							+ "如果不写，默认为和方法名同名的fieldName");
		}
		Object[][] params = null;
		try {
			targetField.setAccessible(true);
			params = (Object[][]) targetField.get(fTarget);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		}
		return params;
	}
}
