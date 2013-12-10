package com.taobao.itest.dataprepare;

import org.junit.Test;

import com.taobao.itest.ITestSpringContextBaseCase;
import com.taobao.itest.annotation.ITestSpringContext;
import com.taobao.itest.annotation.ItestDataPrepare;

/**
 * 测试方法执行前后的动态执行sql
 * 
 * @author yufan.yq
 * 
 */
@ITestSpringContext({ "/applicationContext-test.xml" })
@ItestDataPrepare
public class ITestDataPrepareTest extends ITestSpringContextBaseCase {
	@Test
	public void testDataPrepare1() {
		System.out.println("----test1----");
	}

	@Test
	public void testDataPrepare2() {
		System.out.println("----test2----");
	}
}
