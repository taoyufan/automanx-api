package com.taobao.itest.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yufan.yq
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ItestDataPrepare {
	/**
	 * 数据准备的xml存在的路径，默认为与类同名同路径 <br>
	 * <?xml version="1.0" encoding="UTF-8"?> <root package =
	 * "com.taobao.aifly.testcase.totest.auctionmall.detail.projects.tbskip_3"
	 * class="TestExample3"> <DataPrepare env="daily"> <BeforeClass> <parameter
	 * name="NewUser">insert into userinfo value (?,?)</parameter> <parameter
	 * name="NewRole">insert into roleinfo value (?,?)</parameter>
	 * </BeforeClass> <AfterClass> <parameter name="deleteUser">delete from
	 * userinfo where id =1001</parameter> <parameter name="deleteRole">delete
	 * from roleinfo where id =1001</parameter> </AfterClass> <Before>
	 * <parameter name="insertRole">insert into roleinfo value (?,?)</parameter>
	 * </Before> <After> <parameter name="deleteRole">delete from roleinfo where
	 * id =1001</parameter> <parameter name="deleteRole">delete from roleinfo
	 * where id =1001</parameter> </After> </DataPrepare> </root>
	 * 
	 * @return
	 */
	String location() default "";

	/**
	 * 数据源名称，默认为spring bean配置的第一个
	 * 
	 * @return
	 */
	String dsName() default "";
}
