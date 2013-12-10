package com.taobao.itest.tb.tddl;

import java.util.HashMap;
import java.util.Map;

public class ShardRule {

	private Map<String, TableRule> tableRules = new HashMap<String, TableRule>();
	private String defaultDB;// used to get database current time when Date
								// Field replace is necessary

	public Map<String, TableRule> getTableRules() {
		return tableRules;
	}

	public void setTableRules(Map<String, TableRule> tableRules) {
		this.tableRules = tableRules;
	}

	public String getDefaultDB() {
		return defaultDB;
	}

	public void setDefaultDB(String defaultDB) {
		this.defaultDB = defaultDB;
	}

}
