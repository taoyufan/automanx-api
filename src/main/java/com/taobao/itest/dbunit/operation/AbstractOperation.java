package com.taobao.itest.dbunit.operation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.util.QualifiedTableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Manuel Laflamme
 * @since Jan 17, 2004
 * @version $Revision: 953 $
 */
public abstract class AbstractOperation extends DatabaseOperation {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(AbstractOperation.class);

	protected String getQualifiedName(String prefix, String name,
			IDatabaseConnection connection) {
		if (logger.isDebugEnabled()) {
			logger.debug(
					"getQualifiedName(prefix={}, name={}, connection={}) - start",
					new Object[] { prefix, name, connection });
		}

		String escapePattern = (String) connection.getConfig().getProperty(
				DatabaseConfig.PROPERTY_ESCAPE_PATTERN);
		QualifiedTableName qualifiedTbleName = new QualifiedTableName(name,
				prefix, escapePattern);
		return qualifiedTbleName.getQualifiedName();
	}

	/**
	 * Returns the metadata to use in this operation. It is retrieved from the
	 * database connection using the information from the physical database
	 * table.
	 * 
	 * @param connection
	 *            the database connection
	 * @param metaData
	 *            the XML table metadata
	 */
	static ITableMetaData getOperationMetaData(IDatabaseConnection connection,
			ITableMetaData metaData) throws DatabaseUnitException, SQLException {
		logger.debug(
				"getOperationMetaData(connection={}, metaData={}) - start",
				connection, metaData);

		IDataSet databaseDataSet = connection.createDataSet();
		String tableName = metaData.getTableName();

		ITableMetaData tableMetaData = databaseDataSet
				.getTableMetaData(tableName);
		Column[] columns = metaData.getColumns();
		// if user defined primary keys ,use them. edited by yedu
		Column[] primaryKeys = metaData.getPrimaryKeys();
		List<Column> columnList = new ArrayList<Column>();
		List<Column> primarykeyList = new ArrayList<Column>();
		for (int j = 0; j < columns.length; j++) {
			String columnName = columns[j].getColumnName();
			// Check if column exists in database
			// method "getColumnIndex()" throws NoSuchColumnsException when
			// columns have not been found
			int dbColIndex = tableMetaData.getColumnIndex(columnName);
			// If we get here the column exists in the database
			Column dbColumn = tableMetaData.getColumns()[dbColIndex];
			columnList.add(dbColumn);

			if (!ArrayUtils.isEmpty(primaryKeys)
					&& ArrayUtils.contains(primaryKeys, columns[j])) {
				primarykeyList.add(dbColumn);
			}

		}

		primaryKeys = tableMetaData.getPrimaryKeys();
		if (primarykeyList.size() > 0) {
			primaryKeys = primarykeyList.toArray(new Column[primarykeyList
					.size()]);
		}

		return new DefaultTableMetaData(tableMetaData.getTableName(),
				(Column[]) columnList.toArray(new Column[0]), primaryKeys);
	}
}
