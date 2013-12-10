package com.taobao.itest.tb.tddl;

import java.util.HashMap;
import java.util.Map;

public class AppRule {

	private Map<String, ShardRule> rootMap = new HashMap<String, ShardRule>();

	public Map<String, ShardRule> getRootMap() {
		return rootMap;
	}

	public void setRootMap(Map<String, ShardRule> rootMap) {
		this.rootMap = rootMap;
	}
}
