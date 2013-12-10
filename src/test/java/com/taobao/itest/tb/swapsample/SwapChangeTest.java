package com.taobao.itest.tb.swapsample;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 校验itest自己封装的datasource是否可用
 * 
 * @author yufan.yq
 * 
 */
public class SwapChangeTest extends BaseCase {

	@Test
	public void test_get_db2_datasource() throws Exception {
		System.out.println(db.getUrl());
		Assert.assertNotNull(db);
	}

}
