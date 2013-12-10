package com.taobao.itest;

import java.sql.SQLException;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.taobao.itest.annotation.ITestSpringContext;

//会自动的加载spring配置文件
@ITestSpringContext({ "/applicationContext-test.xml" })
public class BaseTestCase extends ITestSpringContextBaseCase {
	// 使用@Resource注解可以实例化bean
	@Resource
	protected static SimpleJdbcTemplate jdbcTemplate1;
	@Resource
	protected static SimpleJdbcTemplate jdbcTemplate2;

	@Test
	@Ignore
	public void test() throws SQLException {

	}
}
