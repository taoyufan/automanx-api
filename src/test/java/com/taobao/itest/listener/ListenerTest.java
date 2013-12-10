package com.taobao.itest.listener;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.taobao.itest.core.ItestDataDriverRunner;
import com.taobao.itest.core.TestListeners;

/**
 * Itest 最核心的部分是Listener机制，通过本用例可以方便的看到各个方法的执行顺序,便于测试同学自己扩展
 * 
 * @author yufan.yq
 * 
 */
@RunWith(ItestDataDriverRunner.class)
@TestListeners({ ITestListener.class })
public class ListenerTest {
	@BeforeClass
	public static void setUp() {
		System.out.println("BeforeClass");
		System.out.println();
	}

	@Before
	public void before() {
		System.out.println("before");
		System.out.println();
	}

	@Test
	public void myTest1() {
		System.out.println("test1");
		System.out.println();
	}

	@Test
	public void myTest2() {
		System.out.println("test2");
		System.out.println();
	}

	@After
	public void after() {
		System.out.println("after");
	}

	@AfterClass
	public static void tearDown() {
		System.out.println("AfterClass");
	}
}
