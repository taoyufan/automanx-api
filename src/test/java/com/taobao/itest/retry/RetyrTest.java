package com.taobao.itest.retry;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.taobao.itest.ITestSpringContextBaseCase;
import com.taobao.itest.annotation.Retry;

public class RetyrTest extends ITestSpringContextBaseCase {
	int j;

	@Before
	public void bb() {
		System.out.println("before");
	}

	@Retry(10)
	@Test
	public void test_01() {
		System.out.println("-----test_01-------");
		Assert.assertEquals(++j, 5);

	}

}
