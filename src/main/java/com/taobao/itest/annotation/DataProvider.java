package com.taobao.itest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * location为文件路径 fieldName值为对应的field的Name 如果不写，默认为和方法名同名的fieldName
 * 
 * @author yufan.yq
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DataProvider {
	String fieldName() default "";

	/**
	 * 对于文件准备的以下类型，支持自动转换
	 * <ul>
	 * <li>java.lang.BigDecimal (no default value)</li>
	 * <li>java.lang.BigInteger (no default value)</li>
	 * <li>boolean and java.lang.Boolean (default to false)</li>
	 * <li>byte and java.lang.Byte (default to zero)</li>
	 * <li>char and java.lang.Character (default to a space)</li>
	 * <li>java.lang.Class (no default value)</li>
	 * <li>double and java.lang.Double (default to zero)</li>
	 * <li>float and java.lang.Float (default to zero)</li>
	 * <li>int and java.lang.Integer (default to zero)</li>
	 * <li>long and java.lang.Long (default to zero)</li>
	 * <li>short and java.lang.Short (default to zero)</li>
	 * <li>java.lang.String (default to null)</li>
	 * <li>java.io.File (no default value)</li>
	 * <li>java.net.URL (no default value)</li>
	 * <li>java.sql.Date (no default value)</li>
	 * <li>java.sql.Time (no default value)</li>
	 * <li>java.sql.Timestamp (no default value)</li>
	 * </ul>
	 */
	String location() default "";

	boolean injectLater() default false;
}
