package com.taobao.itest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import junit.framework.AssertionFailedError;

/**
 * @author yufan.yq 可以标识在类或方法上，默认重试三次，<br/>
 *         方法会覆盖类上的值，子类会覆盖父类的值
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Retry {
	/**
	 * 重试次数
	 * 
	 * @return
	 */
	int value() default 3;

	/**
	 * 需要重试的异常集
	 */
	Class<? extends Throwable>[] exceptions() default { AssertionFailedError.class };
}
