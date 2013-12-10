package com.taobao.itest.junit;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CalculatorTest {
	long time;
	static int index = 0;
	Calculator calculator;

	@BeforeClass
	public static void startTest() {
		System.out.println("测试开始执行了");
	}

	@Before
	public void setUp() {
		System.out.printf("第%d个用例开始执行\n", ++index);
		time = System.currentTimeMillis();
		calculator = new Calculator();
	}

	@Test
	public void testPlus() throws InterruptedException {
		Assert.assertEquals(calculator.plus(1, 2), 3);
		Thread.sleep(100);
	}

	@After
	public void tearDown() {
		calculator = null;
		time = System.currentTimeMillis() - time;
		System.out.printf("第%d个用例执行了%d毫秒\n", index, time);
	}

	@AfterClass
	public static void endTest() {
		System.out.println("测试结束了");
	}

	@Test
	public void testMinus() throws InterruptedException {
		Assert.assertEquals(calculator.minus(1, 2), -1);
		Thread.sleep(200);
	}

	@Test
	public void testMultiply() throws InterruptedException {
		Assert.assertEquals(calculator.multiply(1, 2), 2);
		Thread.sleep(300);
	}

	@Test
	public void testDivide() throws InterruptedException {
		Assert.assertEquals(calculator.divide(1, 2), 0.5);
		Thread.sleep(400);
	}

}

// 待测类
class Calculator {
	int plus(int x, int y) {
		return x + y;
	}

	int minus(int x, int y) {
		return x - y;
	}

	int multiply(int x, int y) {
		return x * y;
	}

	double divide(int x, int y) {
		return ((double) x) / y;
	}
}
