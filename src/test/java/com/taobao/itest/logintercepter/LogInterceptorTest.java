package com.taobao.itest.logintercepter;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;

import com.taobao.itest.ITestSpringContextBaseCase;
import com.taobao.itest.annotation.ITestSpringContext;

/**
 * 需要在bean里面进行特殊配置的，见/transaction/applicationContext-test.xml，会自动打印入参的值的，
 * log4j的日志级别要为debug
 * 
 * @author yufan.yq
 * 
 */
@ITestSpringContext({ "/transaction/applicationContext-test.xml" })
public class LogInterceptorTest extends ITestSpringContextBaseCase {
	@Resource
	DataSource db1;

	@Test
	public void testLogInterceptor() {
		System.out.println(db1);
	}
}
