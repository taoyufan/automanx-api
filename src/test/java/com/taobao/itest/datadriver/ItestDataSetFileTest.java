package com.taobao.itest.datadriver;

import org.junit.Test;

import com.taobao.itest.BaseTestCase;
import com.taobao.itest.annotation.DataProvider;
import com.taobao.itest.util.DataDriverTool;

/**
 * 本用例测试数据驱动，和注解实现执行sql
 * 
 * @author yufan.yq
 * 
 */
public class ItestDataSetFileTest extends BaseTestCase {

	public static void main(String[] args) {
		DataDriverTool.generateXml("com.taobao.itest.datadriver");
	}

	@Test
	@DataProvider(location = "ItestDataSetFileTest.xml")
	public void test_file(int id, long tel, String message) {
		System.out.println("=====test_file=====");
		System.out.println("id=" + id);
		System.out.println("tel=" + tel);
		System.out.println("message=" + message);
		System.out.println();
	}

	@Test
	@DataProvider(location = "test_file2.csv")
	public void test_file2(int id, long tel, String message) {
		System.out.println("=====test_file2=====");
		System.out.println("id=" + id);
		System.out.println("tel=" + tel);
		System.out.println("message=" + message);
		System.out.println();
	}

	@Test
	@DataProvider(location = "ItestDataSetFileTest.csv")
	public void test_file3(int id, String name) {
		System.out.println("=====test_file3=====");
		System.out.println("id=" + id);
		System.out.println("name=" + name);
		System.out.println();
	}

	@Test
	@DataProvider(location = "ItestDataSetFileTest.csv")
	public void test_file4(int id, long tel, String name) {
		System.out.println("=====test_file4=====");
		System.out.println("id=" + id);
		System.out.println("name=" + name);
		System.out.println();
	}

}
