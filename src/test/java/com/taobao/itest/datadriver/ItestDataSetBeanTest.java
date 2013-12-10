package com.taobao.itest.datadriver;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;

import com.taobao.itest.BaseTestCase;
import com.taobao.itest.User;
import com.taobao.itest.annotation.DataProvider;

/**
 * 本用例测试bean类型的数据驱动（也属于field）
 * 
 * @author yufan.yq
 * 
 */
public class ItestDataSetBeanTest extends BaseTestCase {
	@Resource
	public static DataSource db1;
	@Resource
	public static DataSource db2;

	public DataSource[][] test_bean = { { db1 }, { db2 } }; // bean必须声明为static

	// 直接使用spring的bean的话，会发现打出来的都是null，原因是在初始化测试方法时，bean还没有注入
	@Test
	@DataProvider
	public void test_bean(DataSource dataSource) {
		System.out.println(dataSource);
		System.out.println();
	}

	@Test
	// 使用@InjectLater这个注解可以让方法在执行时才去初始化参数，会发现打出来的不再是null
	@DataProvider(fieldName = "test_bean", injectLater = true)
	public void test_bean1(DataSource dataSource) {
		System.out.println(dataSource);
		System.out.println();
	}

	public Object[][] test_bean2 = { { db1, "yulong", new User(1, "yulong") }, { db2, "yuping", new User(2, "yuping") }, { null, "yufan", new User(3, "yufan") } };

	@Test
	// 只要测试数据里出现了bean，都可以使用@InjectLater这个注解
	@DataProvider(injectLater = true)
	public void test_bean2(DataSource db, String name, User user) {
		System.out.println("db=" + db);
		System.out.println("name=" + name);
		System.out.println(user);
		System.out.println();
	}
}
