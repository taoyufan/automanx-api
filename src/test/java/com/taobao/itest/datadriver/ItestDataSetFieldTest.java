package com.taobao.itest.datadriver;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.itest.BaseTestCase;
import com.taobao.itest.User;
import com.taobao.itest.annotation.DataProvider;

/**
 * 本用例测试数据驱动，和注解实现执行sql
 * 
 * @author yufan.yq
 * 
 */
public class ItestDataSetFieldTest extends BaseTestCase {
	// @Resource
	// static DataSource db1;
	//
	// @BeforeClass
	// public static void setUp() {
	// System.out.println(db1);
	// db1 = (DataSource) SpringContextManager.getApplicationContext()
	// .getBean("db1");
	// System.out.println(db1);
	// System.out.println();
	// }

	public Integer[][] test = { { 277 }, { 21 } };

	@Test
	@DataProvider(fieldName = "test")
	public void test_field(int id) {
		// System.out.println(db1);
		// ParameterizedBeanPropertyRowMapper<User> mapper = new
		// ParameterizedBeanPropertyRowMapper<User>();
		// mapper.setMappedClass(User.class);
		// User userPrepared = (User) jdbcTemplate1.queryForObject(
		// "select * from user where id = ?", mapper, id);
		System.out.println(id);
		System.out.println();
	}

	public Object[][] test_field2 = { { 1, "yulong", new User(1, "yulong") }, { 2, "yuping", new User(2, "yuping") }, { 3, "yufan", new User(3, "yufan") } };

	@Test
	@DataProvider
	public void test_field2(int id, String name, User user) {
		System.out.println("id=" + id);
		System.out.println("name=" + name);
		System.out.println(user);
		System.out.println();
	}

	@Test
	public void test1() {
		System.out.println("test1");
		System.out.println();
	}

	@BeforeClass
	public static void bc() {
		System.out.println("@BeforeClass");
		System.out.println();
	}

	@AfterClass
	public static void ac() {
		System.out.println("@@AfterClass");
		System.out.println();
	}

	@After
	public void am() {
		System.out.println("@@After");
		System.out.println();
	}

	@Before
	public void bm() {
		System.out.println("@Before");
		System.out.println();
	}
}
