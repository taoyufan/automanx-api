package com.taobao.itest.dbunit;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;

import com.taobao.itest.dbunit.dataset.excel.XlsDataSet;
import com.taobao.itest.jdbc.SchemaDataSource;

public class DbUnitTools {

	private static final Log log = LogFactory.getLog(DbUnitTools.class);
	private static final String XML = "xml";

	private static final String XLS = "xls";

	/**
	 * Generate file with DbUnit DataSet format 生成与当前类同名的文件
	 * 
	 * @author yufan.yq
	 * @param dataSource
	 * @param querySqlsSplitBySemicolon
	 * @param fileType
	 *            xls or xml
	 */
	public static void generateDataSetFile(DataSource dataSource, String querySqlsSplitBySemicolon, String fileType) {
		@SuppressWarnings("restriction")
		Class<?> klass = sun.reflect.Reflection.getCallerClass(2);
		generateDataSetFile(klass, dataSource, querySqlsSplitBySemicolon, fileType);
	}

	/**
	 * Generate file with DbUnit DataSet format 生成与给定类同名的文件
	 * 
	 * @author yufan.yq
	 * @param klass
	 * @param dataSource
	 * @param querySqlsSplitBySemicolon
	 * @param fileType
	 */
	public static void generateDataSetFile(Class<?> klass, DataSource dataSource, String querySqlsSplitBySemicolon, String fileType) {
		if (!fileType.toLowerCase().equals(XLS) && !fileType.toLowerCase().equals(XML)) {
			log.error("只支持xml和xls两种格式");
			return;
		}
		StringBuffer destFilePath = new StringBuffer("src/test/java/");
		destFilePath.append(klass.getName().replace(".", "/")).append(".").append(fileType.toLowerCase());
		generateDataSetFile(destFilePath.toString(), dataSource, querySqlsSplitBySemicolon);
	}

	/**
	 * Generate file with DbUnit DataSet format
	 * 
	 * @param destFilePath
	 * @param dataSource
	 * @param querySqls
	 */
	public static void generateDataSetFile(String destFilePath, DataSource dataSource, String querySqlsSplitBySemicolon) {
		String[] querySqls = querySqlsSplitBySemicolon.split(";");
		File destFile = new File(destFilePath);
		log.info("生成xml或excel数据的filepath为:" + destFile.getAbsolutePath());
		DatabaseConnection databaseConnection = null;
		try {
			Connection connection = dataSource.getConnection();
			databaseConnection = getDatabaseConnection(dataSource, connection);
			DatabaseConfig config = databaseConnection.getConfig();
			if ("MySQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())) {
				config.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, new MySqlMetadataHandler());
			} else if ("Oracle".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())) {
				config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
			}
			QueryDataSet queryDataSet = new QueryDataSet(databaseConnection);
			for (int i = 0; i < querySqls.length; i++) {
				queryDataSet.addTable(getTableNameFromQuerySql(querySqls[i]), querySqls[i]);
			}
			String destFileName = destFile.getName();
			if (destFileName.endsWith(XLS)) {
				XlsDataSet.write(queryDataSet, new FileOutputStream(destFile));
			} else if (destFileName.endsWith(XML)) {
				FlatXmlDataSet.write(queryDataSet, new FileOutputStream(destFile));
			} else {
				throw new RuntimeException("destFile shoud be a xls or xml file!");
			}
			log.info("Dbunit Write Data To : " + destFile.getAbsolutePath());
		} catch (Exception e) {
			log.error("DbunitWriteDataToExcel failed");
			e.printStackTrace();
			throw new IllegalStateException("DbunitWriteDataToFile failed", e);
		} finally {
			try {
				if (null != databaseConnection) {
					databaseConnection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * generated dataset file for multi-datasource. sheet name will be a
	 * conjunction of "datasource name",a dot and "table name" .eg: db1.table1,
	 * db2.table2
	 * 
	 * @param destFilePath
	 *            the path where the file will be generated
	 * @param dsSql
	 *            map contains a datasource and the corresponding sql (sqls
	 *            seperated By Semicolon)
	 * @param dsSpringBeanNams
	 *            map contains a datasouce and the corresponding beanName
	 */
	public static void generateDataSetFile(String destFilePath, Map<DataSource, StringBuilder> dsSql, Map<DataSource, String> dsSpringBeanNams) {
		File destFile = new File(destFilePath);
		Set<DataSource> dss = dsSql.keySet();
		// traversal the map
		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		try {
			for (Iterator<DataSource> ds = dss.iterator(); ds.hasNext();) {
				DatabaseConnection databaseConnection = null;
				DataSource dataSource = (DataSource) ds.next();
				Connection connection = dataSource.getConnection();
				databaseConnection = getDatabaseConnection(dataSource, connection);
				DatabaseConfig config = databaseConnection.getConfig();
				if ("MySQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())) {
					config.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, new MySqlMetadataHandler());
				} else if ("Oracle".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())) {
					config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
				}
				String[] dsSqlStrs = dsSql.get(dataSource).toString().split(";");
				for (int i = 0; i < dsSqlStrs.length; i++) {
					QueryDataSet queryDataSet = new QueryDataSet(databaseConnection);
					queryDataSet.addTable(dsSpringBeanNams.get(dataSource) + "." + getTableNameFromQuerySql(dsSqlStrs[i]), dsSqlStrs[i]);// TODO
					dataSets.add(queryDataSet);
				}
			}
			// combine the dataset from different datasources
			IDataSet[] ids = new IDataSet[dss.size()];
			CompositeDataSet compositeDataSet = new CompositeDataSet((IDataSet[]) dataSets.toArray(ids));
			String destFileName = destFile.getName();
			if (destFileName.endsWith(".xls")) {
				XlsDataSet.write(compositeDataSet, new FileOutputStream(destFile));
			} else if (destFileName.endsWith(".xml")) {
				FlatXmlDataSet.write(compositeDataSet, new FileOutputStream(destFile));
			} else {
				throw new RuntimeException("destFile shoud be a xls or xml file!");
			}
		} catch (Exception e) {
			log.error("DbunitWriteDataToExcel failed");
			e.printStackTrace();
			throw new IllegalStateException("DbunitWriteDataToFile failed", e);
		}
		log.info("Dbunit Write Data To : " + destFile.getAbsolutePath());
	}

	private static String getTableNameFromQuerySql(String querySql) {
		String tableName = null;
		String[] array = querySql.split(" ");
		for (int i = 0; i < array.length; i++) {
			if ("From".equalsIgnoreCase(array[i].trim())) {
				tableName = array[i + 1].trim();
				break;
			}
		}
		return tableName;

	}

	private static DatabaseConnection getDatabaseConnection(DataSource dataSource, Connection connection) throws DatabaseUnitException, SQLException {
		String schemaName = getSchemaName(dataSource, connection);
		if (schemaName != null) {
			return new DatabaseConnection(connection, schemaName);
		} else {
			return new DatabaseConnection(connection);
		}

	}

	private static String getSchemaName(DataSource dataSource, Connection connection) throws SQLException {
		String schemaName = null;
		if (dataSource instanceof SchemaDataSource) {
			SchemaDataSource sds = (SchemaDataSource) dataSource;
			schemaName = sds.getSchemaName();
		}
		if (schemaName == null) {
			log.warn("No schemaName is specified, use username as shcemaName defaultly");
			schemaName = connection.getMetaData().getUserName();
		}
		return schemaName;
	}

}
