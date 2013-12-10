package com.taobao.itest.dbunit;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;

import com.taobao.itest.BaseTestCase;
import com.taobao.itest.dataprepare.ITestDataPrepareTest;

/**
 * automan-helper提供了三种生成测试文件的方法
 * 
 * @author yufan.yq
 * 
 */
public class DbUnitToolTest extends BaseTestCase {
	@Resource
	DataSource db1;

	/**
	 * 分别指定路径、数据源、sql
	 */
	@Test
	public void test_用工具生成测试文件1() {
		DbUnitTools.generateDataSetFile(
				"src/test/java/com/taobao/itest/dbunit/DbUnitToolTest.xls",
				db1, "select * from user where id > 10");
	}

	/**
	 * 分别指定数据源、sql、文件类型，会自动在同目录下生成所需要的测试文件
	 */
	@Test
	public void test_用工具生成测试文件2() {
		DbUnitTools.generateDataSetFile(db1,
				"select * from user where id > 10", "xml");
	}

	/**
	 * 分别指定测试类、数据源、sql、文件类型，会自动在测试类同目录下生成所需要的测试文件
	 */
	@Test
	public void test_用工具生成测试文件3() {
		DbUnitTools.generateDataSetFile(ITestDataPrepareTest.class, db1,
				"select * from user where id > 10", "xls");
	}
}
