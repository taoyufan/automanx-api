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

package com.taobao.itest.listener;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.dbunit.DatabaseUnitException;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.core.Constants;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.taobao.itest.annotation.ITestDataSet;
import com.taobao.itest.core.AbstractTestListener;
import com.taobao.itest.core.TestContext;
import com.taobao.itest.dbunit.dataset.excel.XlsDataSet;
import com.taobao.itest.dbunit.dataset.excel.XlsTableWrapper;
import com.taobao.itest.dbunit.operation.DeleteOperation;
import com.taobao.itest.dbunit.operation.RefreshOperation;
import com.taobao.itest.jdbc.SchemaDataSource;
import com.taobao.itest.spring.context.SpringContextManager;
import com.taobao.itest.tb.tddl.AppRule;
import com.taobao.itest.tb.tddl.RuleCalUtil;
import com.taobao.itest.util.AnnotationUtil;

/**
 * According to the test class or test method to do before the @ITestDataSet
 * DBUnit operations related to
 * 
 * @see ITestDataSet
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 */
public class ITestDataSetListener extends AbstractTestListener {
	private static final String XML = "xml";

	private static final String XLS = "xls";

	private static Pattern pattern = Pattern.compile("\\.");

	protected final Log log = LogFactory.getLog(getClass());
	private final Map<Method, List<DatasetConfig>> datasetConfigCache = Collections.synchronizedMap(new IdentityHashMap<Method, List<DatasetConfig>>());
	private final static Constants databaseOperations = new Constants(DatabaseOperation.class);

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		List<DatasetConfig> datasetConfigs = getDatasetConfigs(testContext);
		if (datasetConfigs == null || datasetConfigs.size() == 0)
			return;

		datasetConfigCache.put(testContext.getTestMethod(), datasetConfigs);

		for (DatasetConfig datasetConfig : datasetConfigs) {
			if (log.isInfoEnabled()) {
				log.info(format("Loading dataset from class path resource  '%s' using operation '%s' " + "with dataSourceName '%s'.", datasetConfig.getLocation(), datasetConfig.getSetupOperation(), datasetConfig.getDsName()));
			}
			datasetConfig.getDatabaseTester().onSetup();
		}
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		List<DatasetConfig> datasetConfigs = datasetConfigCache.get(testContext.getTestMethod());
		if (datasetConfigs == null || datasetConfigs.size() == 0)
			return;

		for (DatasetConfig datasetConfig : datasetConfigs) {
			if (log.isInfoEnabled()) {
				log.info(format("Tearing down dataset using operation '%s'", datasetConfig.getTeardownOperation()));
			}

			// modified by qixiu:put connection close in the finally
			// statement,because "onTearDown" may throw exception
			try {
				datasetConfig.getDatabaseTester().onTearDown();
			} catch (Exception e1) {
				throw e1;
			} finally {
				if (!datasetConfig.isTransactional()) {
					try {
						datasetConfig.getDatabaseTester().getConnection().getConnection().close();
					} catch (Exception e) {
					}
				}
			}
		}

		datasetConfigCache.remove(testContext.getTestMethod());
	}

	/**
	 * Supports two methods of multiple data sources can only use one
	 * 
	 * @param testContext
	 * @return
	 * @throws Exception
	 */
	List<DatasetConfig> getDatasetConfigs(TestContext testContext) throws Exception {
		ITestDataSet annotation = findAnnotation(testContext.getTestInstance().getClass(), testContext.getTestMethod());
		if (annotation == null)
			return null;

		String[] locations = determineLocations(testContext, annotation);
		String[] dsNames = determineDsNames(testContext, annotation);
		if (dsNames.length > 1 && locations.length != dsNames.length) {
			String errorMsg = format("dsNames number '%s' does'nt matchs the locations number '%s'.", dsNames.length, locations.length);
			log.error(errorMsg);
			throw new RuntimeException(errorMsg);
		}

		List<DatasetConfig> datasetConfigs = Collections.synchronizedList(new LinkedList<DatasetConfig>());

		// ========add by junliang, deal tddl begin========
		int ruleIndex = indexOfAppRule(dsNames);
		if (ruleIndex != -1) {
			AppRule rootRule = (AppRule) SpringContextManager.getApplicationContext().getBean(dsNames[ruleIndex]);
			String location = locations[ruleIndex];
			String fileType = location.substring(location.lastIndexOf(".") + 1);
			if (!XLS.equalsIgnoreCase(fileType)) {
				String errMsg = "Invalid file type [" + fileType + "], only XLS file supported if you want to use tddl features";
				throw new RuntimeException(errMsg);
			}
			locations = removeStringFromArray(locations, ruleIndex);
			dsNames = removeStringFromArray(dsNames, ruleIndex);
			HSSFWorkbook workbook = new HSSFWorkbook(new DefaultResourceLoader().getResource(location).getInputStream());
			Map<String, HSSFWorkbook> tddlMap = RuleCalUtil.calDataSet(rootRule, workbook, null);// null
																									// now
																									// means
																									// use
																									// local
																									// machine
																									// current
																									// time
			String autoGenFilePath = location.substring(0, location.lastIndexOf("/") + 1) + "autoGen/";
			Set<Entry<String, HSSFWorkbook>> entries = tddlMap.entrySet();
			String writeFilePath = null;
			for (Entry<String, HSSFWorkbook> entry : entries) {
				String autoGenLocation = autoGenFilePath + entry.getKey() + ".xls";
				writeFilePath = autoGenLocation.replaceAll("classpath:", "").replaceAll("[\\\\]", "/");
				writeFilePath = "target/test-classes" + writeFilePath;
				String directory = writeFilePath.substring(0, writeFilePath.lastIndexOf("/"));
				File dir = new File(directory);
				if (!dir.isDirectory()) {
					dir.mkdir();
				}
				File destFile = new File(writeFilePath);
				XlsDataSet.write(new XlsDataSet(entry.getValue()), new FileOutputStream(destFile));
				locations = addString2Array(locations, autoGenLocation);
				dsNames = addString2Array(dsNames, entry.getKey());
			}
		}
		// ========deal tddl end========

		for (int i = 0; i < locations.length; i++) {
			String location = locations[i];
			String fileType = location.substring(location.lastIndexOf(".") + 1);
			String dsName = dsNames.length == 1 ? dsNames[0] : dsNames[i];
			// build dataSet begin
			ReplacementDataSet dataSet;
			if (XLS.equalsIgnoreCase(fileType)) {
				XlsDataSet xlsDataSet = new XlsDataSet(new DefaultResourceLoader().getResource(location).getInputStream());
				// if(annotation.dsNames().length==0){//DataSource name maybe
				// defined in xls sheet
				String[] sheetNames = xlsDataSet.getTableNames();
				for (String sheetName : sheetNames) {
					String[] temp = pattern.split(sheetName);
					String tableName = sheetName;
					if (temp.length == 2) {
						// add by qixiu, remove sheets that has define the
						// dsnames,use different datasets
						sheetNames = (String[]) ArrayUtils.removeElement(sheetNames, sheetName);
						String dsNameTmp = temp[0];
						tableName = temp[1];
						dataSet = new ReplacementDataSet(new DefaultDataSet(new XlsTableWrapper(tableName, xlsDataSet.getTable(sheetName))));
						buildDataBaseConfig(testContext, annotation, datasetConfigs, location, dsNameTmp, dataSet);
					}
				}
				// add by qixiu, for normal sheets use one dataset
				int sheetCounts = sheetNames.length;
				ITable[] tables = new ITable[sheetCounts];
				for (int j = 0; j <= sheetCounts - 1; j++) {
					tables[j] = new XlsTableWrapper(sheetNames[j], xlsDataSet.getTable(sheetNames[j]));
				}
				dataSet = new ReplacementDataSet(new DefaultDataSet(tables));
				buildDataBaseConfig(testContext, annotation, datasetConfigs, location, dsName, dataSet);
				/*
				 * }else{ dataSet = new ReplacementDataSet(xlsDataSet);
				 * buildDataBaseConfig(testContext, annotation, datasetConfigs,
				 * location, dsName, dataSet); }
				 */

			} else if (XML.equalsIgnoreCase(fileType)) {
				dataSet = new ReplacementDataSet(new FlatXmlDataSet(new DefaultResourceLoader().getResource(location).getInputStream()));
				dataSet.addReplacementObject("[NULL]", null);
				buildDataBaseConfig(testContext, annotation, datasetConfigs, location, dsName, dataSet);
			} else {
				String errorMsg = format("Unsupported file type,file '%s' must be xls or xml.", location);
				log.error(errorMsg);
				throw new RuntimeException(errorMsg);
			}

			// build dataSet end
		}
		return datasetConfigs;
	}

	private int indexOfAppRule(String[] dsNames) {
		for (int i = 0; i < dsNames.length; i++) {
			Object ds = SpringContextManager.getApplicationContext().getBean(dsNames[i]);
			if (ds instanceof AppRule) {
				return i;
			}
		}
		return -1;
	}

	private String[] addString2Array(String[] array, String s) {
		String[] newArray = new String[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		newArray[array.length] = s;
		return newArray;
	}

	private String[] removeStringFromArray(String[] array, int index) {
		String[] newArray = new String[array.length - 1];
		int newArrayIndex = 0;
		for (int i = 0; i < array.length; i++) {
			if (i == index) {
				continue;
			}
			newArray[newArrayIndex] = array[i];
			newArrayIndex++;
		}
		return newArray;
	}

	private void buildDataBaseConfig(TestContext testContext, ITestDataSet annotation, List<DatasetConfig> datasetConfigs, String location, String dsName, ReplacementDataSet dataSet) throws DatabaseUnitException, SQLException {
		DataSource dataSource = (DataSource) SpringContextManager.getApplicationContext().getBean(dsName);
		Connection connection = DataSourceUtils.getConnection(dataSource);

		// build databaseTester start
		IDatabaseConnection Iconn = getDatabaseConnection(dataSource, connection);
		DatabaseConfig config = Iconn.getConfig();

		String dbType = connection.getMetaData().getDatabaseProductName();
		if ("MySQL".equalsIgnoreCase(dbType)) {
			config.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, new MySqlMetadataHandler());
		} else if ("Oracle".equalsIgnoreCase(dbType)) {
			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
		}

		Date dbNow = getDbCurrentTime(connection, dbType);
		addSysdateReplacement(dataSet, dbNow);
		addTimeStampReplacement(dataSet, dbNow);

		IDatabaseTester databaseTester = new DefaultDatabaseTester(Iconn);
		databaseTester.setDataSet(dataSet);
		String setUp = annotation.setupOperation();
		DatabaseOperation setUpOperation = "REFRESH".equals(setUp) ? new RefreshOperation() : (DatabaseOperation) databaseOperations.asObject(setUp);
		databaseTester.setSetUpOperation(setUpOperation);

		String teardown = annotation.teardownOperation();
		DatabaseOperation teardownOperation = "DELETE".equals(teardown) ? new DeleteOperation() : (DatabaseOperation) databaseOperations.asObject(teardown);
		databaseTester.setTearDownOperation(teardownOperation);
		// build databaseTester end

		boolean transactional = DataSourceUtils.isConnectionTransactional(connection, dataSource);
		DatasetConfig datasetConfig = new DatasetConfig(databaseTester, transactional).location(location).dsName(dsName).setupOperation(annotation.setupOperation()).teardownOperation(annotation.teardownOperation());

		datasetConfigs.add(datasetConfig);
	}

	private Date getDbCurrentTime(Connection connection, String dbType) throws SQLException {
		Date currentTime = null;
		String sql = null;
		if ("MySQL".equalsIgnoreCase(dbType)) {
			sql = "SELECT now()";
		} else if ("Oracle".equalsIgnoreCase(dbType)) {
			sql = "SELECT sysdate FROM dual";
		}

		if (sql != null) {
			ResultSet rs = connection.createStatement().executeQuery(sql);
			while (rs.next()) {

				currentTime = rs.getTimestamp(1);
			}
		} else {
			currentTime = new Date();
		}

		return currentTime;
	}

	private ITestDataSet findAnnotation(Class<?> testClass, Method testMethod) {
		ITestDataSet annotation = (ITestDataSet) AnnotationUtils.findAnnotation(testMethod, ITestDataSet.class);
		if (annotation == null) {

			annotation = (ITestDataSet) AnnotationUtil.findAnnotation(testClass, ITestDataSet.class);
		}
		return annotation;
	}

	private String[] determineLocations(TestContext testContext, ITestDataSet annotation) {
		Class<?> testClass = testContext.getTestInstance().getClass();
		String fileType = annotation.fileType();
		String[] value = annotation.value();
		String[] locations = annotation.locations();
		if (!ArrayUtils.isEmpty(value) && !ArrayUtils.isEmpty(locations)) {
			String msg = String.format("Test class [%s] has been configured with @ITestDataSetListener' 'value' [%s] and 'locations' [%s] attributes. Use one or the other, but not both.", testClass, ArrayUtils.toString(value), ArrayUtils.toString(locations));
			throw new RuntimeException(msg);
		} else if (!ArrayUtils.isEmpty(value)) {
			locations = value;
		}

		if (ArrayUtils.isEmpty(locations)) {// user undefined,using default
											// location
			locations = ResourceLocationProcessingUtil.generateDefaultLocations(testClass, "." + fileType);
		} else {// process to standard resource
			locations = ResourceLocationProcessingUtil.modifyLocations(testClass, locations);
		}
		return locations;
	}

	private String[] determineDsNames(TestContext testContext, ITestDataSet annotation) {
		String[] dsNames = annotation.dsNames();
		if (dsNames.length == 0) {// user undefined,look up default dataSource
			String[] defaultDsNames = SpringContextManager.getApplicationContext().getBeanNamesForType(SchemaDataSource.class);
			if (defaultDsNames.length < 1) {
				defaultDsNames = SpringContextManager.getApplicationContext().getBeanNamesForType(DataSource.class);
			}
			dsNames = new String[] { defaultDsNames[0] };
		}

		return dsNames;
	}

	private DatabaseConnection getDatabaseConnection(DataSource dataSource, Connection connection) throws DatabaseUnitException, SQLException {
		String schemaName = getSchemaName(dataSource, connection);
		if (schemaName != null) {
			return new DatabaseConnection(connection, schemaName) {
				public void close() throws SQLException {

				}
			};
		} else {
			return new DatabaseConnection(connection) {
				public void close() throws SQLException {

				}
			};
		}

	}

	private String getSchemaName(DataSource dataSource, Connection connection) throws SQLException {
		String schemaName = null;
		if (dataSource instanceof SchemaDataSource) {
			SchemaDataSource sds = (SchemaDataSource) dataSource;
			schemaName = sds.getSchemaName();
		}
		if (schemaName == null) {
			log.warn("No schemaName is specified, use db name as shcemaName defaultly");
			schemaName = connection.getCatalog();
		}
		return schemaName;
	}

	private void addSysdateReplacement(ReplacementDataSet dataSet, Date currentTime) {
		dataSet.addReplacementObject("sysdate", currentTime);
		long day = 3600 * 24 * 1000;
		for (int i = 1; i < 32; i++) {
			dataSet.addReplacementObject("sysdate-" + i, new java.util.Date(currentTime.getTime() - i * day));
			dataSet.addReplacementObject("sysdate+" + i, new java.util.Date(currentTime.getTime() + i * day));
		}
	}

	private void addTimeStampReplacement(ReplacementDataSet dataSet, Date currentTime) {
		dataSet.addReplacementObject("itest-timestamp", currentTime.getTime());
	}

	static class DatasetConfig {
		private IDatabaseTester databaseTester;
		private boolean transactional;

		private String location;
		private String dsName;
		private String setupOperation;
		private String teardownOperation;

		public DatasetConfig location(String location) {
			this.location = location;
			return this;
		}

		public DatasetConfig dsName(String dsName) {
			this.dsName = dsName;
			return this;
		}

		public DatasetConfig setupOperation(String setupOperation) {
			this.setupOperation = setupOperation;
			return this;
		}

		public DatasetConfig teardownOperation(String teardownOperation) {
			this.teardownOperation = teardownOperation;
			return this;
		}

		public DatasetConfig(IDatabaseTester databaseTester, boolean transactional) {
			this.databaseTester = databaseTester;
			this.transactional = transactional;
		}

		public IDatabaseTester getDatabaseTester() {
			return databaseTester;
		}

		public boolean isTransactional() {
			return transactional;
		}

		public String getLocation() {
			return location;
		}

		public String getDsName() {
			return dsName;
		}

		public String getSetupOperation() {
			return setupOperation;
		}

		public String getTeardownOperation() {
			return teardownOperation;
		}

	}

}
