package com.taobao.itest.dbunit;

import org.junit.Test;

import com.taobao.itest.BaseTestCase;
import com.taobao.itest.annotation.ITestDataSet;
import com.taobao.itest.annotation.ITestDataSetBefore;

/**
 * @ITestDataSet注解直接加在类上，类中的所有方法都会生效，也可以加在方法上,使特定的方法生效，方法上的注解会覆盖类上的
 * @author yufan.yq
 * 
 */
// 默认执行前插入（存在则更新）执行后删除
// itest从默认位置（相同classpath下跟类同名的xls文件）用默认数据源（在Spring配置文件中定义的唯一DataSource）用默认数据管理策略完成数据加载和清理。标注于类对该类所有的测试方法有效。
@ITestDataSet
public class ITestDataSetTest extends BaseTestCase {
	/**
	 * 会使用类上添加的注解进行操作数据库
	 */
	@Test
	public void test_1() {
		System.out.println("执行前插入数据，执行后删除");
	}

	/**
	 * 会覆盖类上添加的注解进行操作数据库 . 需要准备数据的excel或xml文件可以自己指定一个或多个， 数据源也可指定，会分别对应
	 * ITestDataSetTest.xls会使用db1，ITestData_SetTest.xls会使用db2
	 * itest还提供用Excel的sheet名指定数据源,sheet名格式为dataSourceName.tableName.
	 * 自定义数据加载和清理策略<br>
	 * NONE：什么也不做 . UPDATE ：根据数据集内容更新表中数据. INSERT ：将数据集内容插入表，可能会违反主键唯一约束 .
	 * REFRESH ：如果表中存在数据集中对应的数据则更新，不存在则插入 . DELETE ：从表中删除数据集中对应的内容.
	 * ---------下面三条在公用数据库中慎用---------------------- . DELETE_ALL ：删除表中所有数据.
	 * TRUNCATE_TABLE：执行truncate table指令. CLEAN_INSERT ：先执行DELETE_ALL,再执行INSERT.
	 * 
	 * @ITestDataSet默认的策略是加载用”REFRESH“，清理用”DELETE“，通过指定@ITestDataSet的setupOperation和teardownOperation属性可以自定义策略。
	 */
	@ITestDataSet(locations = { "ITestDataSetTest.xls", "ITestData_SetTest.xls" }, dsNames = {
			"db1", "db2" }, teardownOperation = "None")
	@Test
	public void test_2() {
		System.out.println("执行前插入数据，执行后无操作");
	}

	// 支持无主键表:通常情况下,ITestDataSet根据数据库表主键完成插入和清理，有时候数据库表没有主键，在这种情况下使用ITestDataSet需要在excel里自定义主键。定义主键的方法很简单，在excel里将对应的字段名加下划线即可。
	// 支持sysdate及sysdate加减天操作:有时我们希望插入的时间是动态的，当前时间或前后几天，这种情况在excel里把时间对应的字段值设置为sysdate表示动态替换为当前时间，设置为sysdate+n,或sysdate-n表示按当前时间加减n天（n为正整数，且1<=n<=31）
	// 使用这个注解的方法会在数据插入前执行,需要在执行前处理数据可以写在这里
	@ITestDataSetBefore
	public void beforeDataSet() {
		System.out
				.println("使用这个注解@ITestDataSetBefore的方法会在数据插入前执行,"
						+ "需要在执行前处理数据可以写在这里，需要注意的是它会在每个方法执行前执行（在@Before之前），而不仅仅是@ITestDataSet之前");
	}

}
