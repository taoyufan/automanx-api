package com.taobao.itest.tb.tddl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableRule {

	private String[] dbIndexes;
	private String[] dbRules;
	private String[] tbRules;

	/**
	 * 格式必须是throughAllDB:[_0000-_0003]或resetEachDB:[_0000-_0003]，后缀长度可变
	 */
	private String tbSuffix;
	private String logicTableName;

	private static final String UNIQUE_DB = "UNIQUE_DB";// 表示不分库，配置如：UNIQUE_DB:dataSourceName
	private static final String UNIQUE_TB = "UNIQUE_TB";// 表示不分表，配置如：UNIQUE_TB:tableName

	public String[] getDbIndexes() {
		return dbIndexes;
	}

	public void setDbIndexes(String dbIndexes) {
		this.dbIndexes = dbIndexes.split(",");
		for (int i = 0; i < this.dbIndexes.length; i++) {
			this.dbIndexes[i] = this.dbIndexes[i].trim();
		}
	}

	public void setDbIndexes(String[] dbIndexes) {
		this.dbIndexes = dbIndexes;
		for (int i = 0; i < this.dbIndexes.length; i++) {
			this.dbIndexes[i] = this.dbIndexes[i].trim();
		}
	}

	public String[] getDbRules() {
		return dbRules;
	}

	public void setDbRules(String[] dbRules) {
		this.dbRules = dbRules;
		for (int i = 0; i < this.dbRules.length; i++) {
			this.dbRules[i] = this.dbRules[i].trim();
		}
	}

	public void setDbRules(String dbRules) {
		this.dbRules = new String[] { dbRules.trim() };
	}

	public String[] getTbRules() {
		return tbRules;
	}

	public void setTbRules(String[] tbRules) {
		this.tbRules = tbRules;
		for (int i = 0; i < this.tbRules.length; i++) {
			this.tbRules[i] = this.tbRules[i].trim();
		}
	}

	public void setTbRules(String tbRules) {
		this.tbRules = new String[] { tbRules.trim() };
	}

	public String getTbSuffix() {
		return tbSuffix;
	}

	public void setTbSuffix(String tbSuffix) {
		this.tbSuffix = tbSuffix;
	}

	public String getLogicTableName() {
		return logicTableName;
	}

	public void setLogicTableName(String logicTableName) {
		this.logicTableName = logicTableName;
	}

	public boolean isUniqueDB() {
		if (dbRules == null || dbRules.length == 0) {
			return true;
		}
		if (dbRules[0].trim().startsWith(UNIQUE_DB)) {
			return true;
		}
		return false;
	}

	public String getUniqueDB() {
		if (isUniqueDB()) {
			if (dbRules == null || dbRules.length == 0) {
				return dbIndexes[0];
			} else {
				String[] rule = dbRules[0].split(":");
				return rule[1].trim();
			}
		}

		return null;
	}

	public boolean isUniqueTB() {
		if (tbRules == null || tbRules.length == 0) {
			return true;
		}
		if (tbRules[0].trim().startsWith(UNIQUE_TB)) {
			return true;
		}
		return false;
	}

	public String getUniqueTB() {
		if (isUniqueTB()) {
			if (tbRules == null || tbRules.length == 0) {
				return logicTableName;
			} else {
				String[] rule = tbRules[0].split(":");
				return rule[1].trim();
			}
		}
		return null;
	}

	public Map<String, List<String>> getDbTableNames() {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> tables = new ArrayList<String>();
		if (isUniqueTB()) {// 不分表
			tables.add(getUniqueTB());
		} else {// 分表
			String exp = tbSuffix.substring(tbSuffix.indexOf("[") + 1,
					tbSuffix.indexOf("]"));
			String[] expEle = exp.split("-");
			String[] expNum = new String[] { expEle[0].replaceFirst("_", ""),
					expEle[1].replaceFirst("_", "") };
			for (int i = Integer.valueOf(expNum[0]); i <= Integer
					.valueOf(expNum[1]); i++) {
				tables.add(createTableName(expEle[0], i));
			}
		}

		if (!isUniqueDB() && !isUniqueTB()
				&& tbSuffix.startsWith("throughAllDB")) {
			// 分库分表throughAllDB
			int dbCount = dbIndexes.length;
			int tableCount = tables.size();
			int tablePerDb = tableCount / dbCount;
			for (int i = 0; i < dbIndexes.length; i++) {
				map.put(dbIndexes[i],
						tables.subList(i * tablePerDb, (i + 1) * tablePerDb));
			}
		} else if (!isUniqueDB() && !isUniqueTB()
				&& tbSuffix.startsWith("resetEachDB")) {
			// 分库分表resetEachDB
			for (String dbIndex : dbIndexes) {
				map.put(dbIndex, tables);
			}
		} else if (!isUniqueDB() && isUniqueTB()) {
			// 分库不分表
			for (String dbIndex : dbIndexes) {
				map.put(dbIndex, tables);
			}
		} else if (isUniqueDB()) {
			// 不分库分表
			// 不分库不分表
			map.put(getUniqueDB(), tables);
		}

		return map;
	}

	private String createTableName(String exp, int num) {
		String suffix = "_" + num;
		while (suffix.length() < exp.length()) {
			suffix = suffix.replaceFirst("_", "_0");
		}
		return logicTableName + suffix;
	}

	public static void main(String[] args) {
		TableRule rule = new TableRule();
		rule.setDbIndexes("db1,db2,db3,db4");
		//rule.setTbSuffix("throughAllDB:[_0000-_0007]");
		rule.setLogicTableName("bmw_users");
		rule.setDbRules("1UNIQUE_DB:dataSourceName");
		rule.setTbRules("1UNIQUE_TB:tableName");
		System.out.println(rule.getDbTableNames());
	}
}
