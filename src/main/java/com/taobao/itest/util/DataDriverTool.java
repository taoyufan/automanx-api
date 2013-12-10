package com.taobao.itest.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.junit.Test;

/**
 * 工具方法，帮助生成数据驱动测试文件。支持如下类型的格式
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
 * @author yufan.yq
 * 
 */
public class DataDriverTool {
	protected static final Log logger = LogFactory.getLog(DataPrepareUtils.class);
	/**
	 * 支持转换的列表
	 */
	private static Set<String> prams = new HashSet<String>(25);
	static {
		prams.add("java.lang.BigDecimal");
		prams.add("java.lang.BigInteger");
		prams.add("boolean");
		prams.add("java.lang.Boolean");
		prams.add("byte");
		prams.add("java.lang.Byte");
		prams.add("char");
		prams.add("java.lang.Character");
		prams.add("java.lang.Class");
		prams.add("double");
		prams.add("java.lang.Double");
		prams.add("float");
		prams.add("java.lang.Float");
		prams.add("int");
		prams.add("java.lang.Integer");
		prams.add("long");
		prams.add("java.lang.Long");
		prams.add("short");
		prams.add("java.lang.Short");
		prams.add("java.lang.String");
		prams.add("java.io.File");
		prams.add("java.net.URL");
		prams.add("java.sql.Date");
		prams.add("java.sql.Time");
		prams.add("java.sql.Timestamp");
	}
	private final static String BASE_PATH = "src/test/resources/";
	private final static String XML = ".xml";
	private final static String ROOT = "root";
	private final static String PACKAGE = "package";
	private final static String CLASS = "class";
	private final static String TESTCASE = "testcase";
	private final static String METHOD = "method";
	private final static String ENV = "env";
	private final static String DAILY = "daily";
	private final static String RECORD = "record ";
	private final static String BLOCK = "block";
	private final static String CASE_LEVEL = "case_level";

	private final static String RUN = "run";
	private final static String PARAMETER = "parameter ";
	private final static String ORDERID = "orderid";
	private final static String NAME = "name";
	private final static String VALUE = "value";

	// 注释
	private final static String COMMENT1 = "使用XML做数据驱动支持如下类型的自动转换：java.lang.Boolean, char, java.lang.Long, java.sql.Timestamp, java.lang.Float, long, float, short, java.lang.Double, byte, java.lang.Character, java.lang.Class, java.sql.Time, boolean, java.lang.BigDecimal, java.lang.BigInteger, int, java.lang.Integer, java.lang.String, java.lang.Short, double, java.sql.Date, java.net.URL, java.io.File, java.lang.Byte。";
	private final static String COMMENT2 = "每个record节点可能有一个属性run，标示改数据是否生效，为true时会执行，否则不执行，默认为true";
	private final static String COMMENT3 = "如果测试数据比较复杂，可以考虑使用CDATA解决：<parameter orderid=\"3\" name=\"message\"><![CDATA[ <div id=\"cool\">我是div</div> ]]></parameter>";

	/**
	 * 根据测试类所在包名生成数据驱动文件<br/>
	 * 会循环扫描子包生成<br/>
	 * 默认路径为src/test/resources/包名.replace(".", "/")/类名.xml
	 * 
	 * @param packageName包名
	 */
	public static void generateXml(String packageName) {
		generateXml(packageName, true);
	}

	/**
	 * 根据测试类所在包名生成数据驱动文件<br/>
	 * 默认路径为src/test/resources/包名.replace(".", "/")/类名.xml
	 * 
	 * @param dirPath
	 *            测试类所在父文件夹
	 * @param recursive
	 *            是否循环迭代
	 */
	public static void generateXml(String packageName, boolean recursive) {
		if (StringUtils.isEmpty(packageName)) {
			logger.warn("测试类所在包名不能为空");
			return;
		}
		Set<Class<?>> classes = getClasses(packageName, recursive);
		generateXml(classes);
	}

	/**
	 * 根据测试类集合生成数据驱动文件<br/>
	 * 默认路径为src/test/resources/包名.replace(".", "/")/类名.xml
	 * 
	 * @param classes
	 *            测试类集合
	 */
	public static void generateXml(Set<Class<?>> classes) {
		if (null == classes || classes.isEmpty()) {
			logger.warn("测试类集合不能为空");
			return;
		}
		for (Class<?> clazz : classes) {
			generateXml(clazz);
		}
	}

	/**
	 * 根据测试类生成数据驱动文件<br/>
	 * 默认路径为src/test/resources/包名.replace(".", "/")/类名.xml
	 * 
	 * @param mClass
	 *            测试类
	 */
	public static void generateXml(Class<?> mClass) {
		String filePath = BASE_PATH + mClass.getName().replace(".", "/") + XML;
		generateXml(mClass, filePath);
	}

	/**
	 * 根据测试类和测试路径生成数据驱动文件<br/>
	 * 
	 * @param mClass
	 * @param filePath
	 */
	public static void generateXml(Class<?> mClass, String filePath) {
		if (!filePath.toLowerCase().endsWith(XML)) {
			throw new RuntimeException("只支持xml格式的文件");
		}
		Package pac = mClass.getPackage();
		String packageName = "";
		if (null != pac) {
			packageName = pac.getName();
		}
		Document doc = DocumentHelper.createDocument();
		doc.addComment(COMMENT1);
		doc.addComment(COMMENT2);
		doc.addComment(COMMENT3);
		// root element
		Element root = doc.addElement(ROOT).addAttribute(PACKAGE, packageName).addAttribute(CLASS, mClass.getSimpleName());
		Method[] methods = mClass.getDeclaredMethods();
		// 是否需要生成测试文件
		boolean gene = false;
		for (Method method : methods) {
			if (method.getAnnotation(Test.class) == null) {
				continue;
			}
			String methodName = method.getName();
			logger.info("正在为方法" + methodName + "生成测试数据模板");
			if (!method.getReturnType().toString().equals("void") || method.getModifiers() != Modifier.PUBLIC) {
				throw new RuntimeException("测试方法必须声明为public void，请检查方法 ：" + methodName);
			}
			// 程序走到这里说明已经是一个合法的automanx测试用例
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 0) {// 普通测试方法，不用生成数据
				continue;
			}
			checkParameters(parameterTypes);
			// 程序走到这里说明已经是一个合法的数据驱动测试用例，需要生成测试数据
			gene = true;
			Element testCase = root.addElement(TESTCASE).addAttribute(METHOD, methodName).addAttribute(ENV, DAILY);
			addRecord(testCase, parameterTypes, "P0", "1", "true");
			addRecord(testCase, parameterTypes, "P0", "2", "false");
		}
		if (gene) {// 需要生成数据准备文件
			saveDoc(doc, filePath);
		} else {
			logger.warn("该类不包含有数据驱动的方法！类名：" + mClass.getName());
		}
	}

	private static void saveDoc(Document doc, String filePath) {
		if (null == doc) {
			logger.warn("Document为null");
			return;
		}
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			File file = new File(filePath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
			writer.write(doc);
			writer.flush();
			writer.close();
			logger.info("数据驱动文件生成成功，路径：" + file.getAbsolutePath());
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		}
	}

	/**
	 * 为测试用例增加数据
	 * 
	 * @param testCase
	 * @param parameterTypes
	 * @param index
	 *            第几个
	 * @param run
	 *            是否执行
	 * 
	 */
	private static void addRecord(Element testCase, Class<?>[] parameterTypes, String caseLevel, String index, String run) {
		Element record = testCase.addElement(RECORD).addAttribute(BLOCK, index).addAttribute(CASE_LEVEL, caseLevel).addAttribute(RUN, run);
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> pClass = parameterTypes[i];
			record.addElement(PARAMETER).addAttribute(ORDERID, String.valueOf(i + 1)).addAttribute(NAME, pClass.getSimpleName()).addAttribute(VALUE, "");
		}

	}

	/**
	 * 检查是否包含不支持的类型
	 * 
	 * @param parameterTypes
	 */
	private static void checkParameters(Class<?>[] parameterTypes) {
		for (Class<?> clazz : parameterTypes) {
			if (!prams.contains(clazz.getName())) {
				// 出现了不支持的类型
				throw new RuntimeException("测试方法中包含了不支持的类型 ：" + clazz.getName() + COMMENT1);
			}
		}
	}

	/**
	 * 从包package中获取所有的Class <br/>
	 * 借鉴：http://www.cnblogs.com/phoebus0501/archive/2011/03/13/1982841.html
	 * 
	 * @param pack
	 * @param recursive
	 *            是否循环迭代
	 * @return
	 */
	private static Set<Class<?>> getClasses(String pack, boolean recursive) {
		// 创建一个class类的集合
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// 获取包的名字 并进行替换
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		// 定义一个枚举的集合 并进行循环来处理这个目录下的things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			// 循环迭代下去
			while (dirs.hasMoreElements()) {
				// 获取下一个元素
				URL url = dirs.nextElement();
				// 得到协议的名称
				String protocol = url.getProtocol();
				// 如果是以文件的形式保存在服务器上
				if ("file".equals(protocol)) {
					System.err.println("file类型的扫描");
					// 获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
				} else if ("jar".equals(protocol)) {
					// 如果是jar包文件
					// 定义一个JarFile
					System.err.println("jar类型的扫描");
					findAndAddClassesInPackageByJar(recursive, classes, packageName, url);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classes;
	}

	/**
	 * 以包的形式获取所有classes
	 * 
	 * @param recursive
	 * @param classes
	 * @param packageName
	 * @param url
	 */
	private static void findAndAddClassesInPackageByJar(boolean recursive, Set<Class<?>> classes, String packageName, URL url) {
		String packageDirName = packageName.replace('.', '/');
		try {
			// 获取jar
			JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
			// 从此jar包 得到一个枚举类
			Enumeration<JarEntry> entries = jar.entries();
			// 同样的进行循环迭代
			while (entries.hasMoreElements()) {
				// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				// 如果是以/开头的
				if (name.charAt(0) == '/') {
					// 获取后面的字符串
					name = name.substring(1);
				}
				// 如果前半部分和定义的包名相同
				if (name.startsWith(packageDirName)) {
					int idx = name.lastIndexOf('/');
					// 如果以"/"结尾 是一个包
					if (idx != -1) {
						// 获取包名 把"/"替换成"."
						packageName = name.substring(0, idx).replace('/', '.');
					}
					// 如果可以迭代下去 并且是一个包
					if ((idx != -1) || recursive) {
						// 如果是一个.class文件 而且不是目录
						if (name.endsWith(".class") && !entry.isDirectory()) {
							// 去掉后面的".class" 获取真正的类名
							String className = name.substring(packageName.length() + 1, name.length() - 6);
							try {
								// 添加到classes，使用classLoader的好处是不会执行静态块
								classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
							} catch (ClassNotFoundException e) {
								logger.error("添加用户自定义视图类错误 找不到此类的.class文件");
								e.printStackTrace();
							}
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("在扫描用户定义类时从jar包获取文件出错");
			e.printStackTrace();
		}
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			logger.warn("用户定义包名 " + packageName + " 下没有任何文件");
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
			} else {
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = file.getName().substring(0, file.getName().length() - 6);
				try {
					// 添加到集合中去
					classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					logger.error("添加用户自定义视图类错误 找不到此类的.class文件");
					e.printStackTrace();
				}
			}
		}
	}
}
