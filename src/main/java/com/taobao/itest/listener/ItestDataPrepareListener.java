package com.taobao.itest.listener;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.itest.annotation.ItestDataPrepare;
import com.taobao.itest.core.AbstractTestListener;
import com.taobao.itest.core.TestContext;
import com.taobao.itest.jdbc.SchemaDataSource;
import com.taobao.itest.spring.context.SpringContextManager;
import com.taobao.itest.util.AnnotationUtil;
import com.taobao.itest.util.DataPrepareUtils;

/**
 * @author yufan.yq
 * 
 */
public class ItestDataPrepareListener extends AbstractTestListener {
	protected final Log log = LogFactory.getLog(getClass());
	private Map<String, List<Object>> sqlMap;
	private String path;
	private ItestDataPrepare annotation;
	private DataSource dataSource;
	private Connection cnn;

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		init(testContext);
		if (annotation != null && null != dataSource && null != sqlMap) {
			generateSql(sqlMap.get("BeforeClass"));
		}
	}

	/**
	 * 初始化数据源、路径、执行的sql的map、数据驱的map
	 * 
	 * @param testContext
	 */
	private void init(TestContext testContext) {
		annotation = (ItestDataPrepare) AnnotationUtil.findAnnotation(
				testContext.getTestClass(), ItestDataPrepare.class);
		if (null != annotation) {
			dataSource = determineDataSource(testContext, annotation);
			path = determineLocation(testContext, annotation);// 相对路径
			sqlMap = DataPrepareUtils.getPrepareDatas(path);
		}
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		if (annotation != null) {
			if (null != dataSource && null != sqlMap) {
				generateSql(sqlMap.get("AfterClass"));
			}
			annotation = null;
			sqlMap = null;
			if (null != cnn) {
				cnn.close();
				cnn = null;
			}
			dataSource = null;
		}
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		if (annotation != null && null != dataSource && null != sqlMap) {
			generateSql(sqlMap.get("Before"));
		}
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		if (annotation != null && null != dataSource && null != sqlMap) {
			generateSql(sqlMap.get("After"));
		}
	}

	/**
	 * 执行sql
	 * 
	 * @param sqlList
	 */
	private void generateSql(List<Object> sqlList) {
		if (null == sqlList) {
			return;
		}
		try {
			if (null == cnn) {
				cnn = dataSource.getConnection();
			}
			for (Object sql : sqlList) {
				cnn.prepareStatement(sql.toString()).execute();
				log.info("执行sql:" + sql.toString());
			}
		} catch (SQLException e) {
			log.error("执行sql失败" + e.getMessage());
		}
	}

	/**
	 * 获取配置文件所在位置
	 * 
	 * @param testContext
	 * @param annotation
	 * @return
	 */
	private static String determineLocation(TestContext testContext,
			ItestDataPrepare annotation) {
		Class<?> testClass = testContext.getTestClass();
		String location = annotation.location();
		if (location.equals("")) {// user undefined,using default // location
			location = ResourceLocationProcessingUtil.generateDefaultLocations(
					testClass, ".xml")[0];
		} else {// process to standard resource
			location = ResourceLocationProcessingUtil.modifyLocations(
					testClass, location)[0];
		}
		return location.replace("classpath:", "target/test-classes");
	}

	/**
	 * 获取数据源
	 * 
	 * @param testContext
	 * @param annotation
	 * @return
	 */
	private DataSource determineDataSource(TestContext testContext,
			ItestDataPrepare annotation) {
		String dsName = annotation.dsName();
		if (dsName.equals("")) {// user undefined,look up default dataSource
			if (null == SpringContextManager.getApplicationContext()) {
				return null;
			}
			String[] defaultDsNames = SpringContextManager
					.getApplicationContext().getBeanNamesForType(
							SchemaDataSource.class);
			if (defaultDsNames.length < 1) {
				defaultDsNames = SpringContextManager.getApplicationContext()
						.getBeanNamesForType(DataSource.class);
			}
			dsName = defaultDsNames[0];
		}
		return (DataSource) SpringContextManager.getApplicationContext()
				.getBean(dsName);
	}

}
