package com.taobao.itest.hsf;

import org.junit.Test;

import com.taobao.itest.ITestSpringContextBaseCase;
import com.taobao.itest.annotation.ITestHsfStarter;

/**
 * 这里只演示如何启动hsf，真正使用时，需要加载hsf的bean，配置见/hsf/applicationContext-test.xml
 * 
 * @author yufan.yq
 * 
 */
// @ITestSpringContext({ "/hsf/applicationContext-test.xml" })
@ITestHsfStarter(path = "/home/admin/hsf", version = "1.4.9.3")
public class HsfTest extends ITestSpringContextBaseCase {
	@Test
	public void testHsfStarter() {
		System.out.println("hsf 已经启动");
	}
}
