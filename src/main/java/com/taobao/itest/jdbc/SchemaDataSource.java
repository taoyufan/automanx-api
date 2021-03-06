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

package com.taobao.itest.jdbc;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * Increase schemaName property to support DBUnit operation
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 */
public class SchemaDataSource extends BasicDataSource {

	private String schemaName;

	public SchemaDataSource() {

	}

	public SchemaDataSource(String driverClassName, String url,
			String username, String password, String schemaName) {
		super();
		this.schemaName = schemaName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

}
