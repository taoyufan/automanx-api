/*
 * Copyright 2011 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.taobao.itest.spring.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;

/**
 * Automatically print call log 自动打印调用入参和结果 由于“Service
 * Testing”的接口一般是JavaBean对象，通常包含N多的字段
 * ，测试时需要对相关字段赋值。有时（尤其是用例运行第一次失败时）需要知道调用的确切入参是什么
 * ，以检查构造测试数据是否正确，需要知道调用返回的确切结果是什么，以检查断言语句是否正确。
 * 
 * itest提供的LogInterceptor可以通过配置自动打印出调用入参和结果。LogInterceptor采用Spring
 * Aop的思路实现，其配置示例如下<br>
 * <code>
 * <bean id="logInterceptor" class="com.taobao.testsupport.spring.aop.LogInterceptor">
 *  <property name="logInvokeParams" value="true" />
 *  <property name="logInvokeResult" value="true" /> 
 * </bean> 
 *  <bean class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator">
 * 		<property name="beanNames"> 
 * 			<value>guestManager</value>
 * 		</property> 
 * 		<property name="interceptorNames">
 * 			 <list><value>logInterceptor</value></list>
 * 		</property> 
 * </bean> 
 * </code>通过上面的配置为guestManager这个Bean注入了自动打印调用入参和结果功能。
 * 
 * package com.taobao.test.sample;
 * 
 * public class GuestManagerTest extends BaseCase {
 * 
 * @Resource protected GuestManager guestManager;
 * @Test public void testGetGusetsByName () { Guest guest =
 *       populate(Guest.class); List <Guest> guests =
 *       guestManager.getGuests(guest); } }<br>
 *       执行上面的测试将会打印出类似下面的日志
 * 
 *       GuestManagerTest.testGetGusets(33) |GuestManagerImpl.getGuests invoked
 *       with params: param 1:
 *       com.taobao.testsupport.sample.model.Guest@1d47b2b[ id=<null>
 *       trackid=0a63263401ac269422217a09cc3 lastNick=James Gosling ]
 *       GuestManagerTest.testGetGusets(33) |GuestManagerImpl.getGuests invoked
 *       result is: [com.taobao.testsupport.sample.model.Guest@1700391[ id=88001
 *       trackid=0a63263401ac269422217a09cc3 lastNick=James Gosling ]]
 *       注意LogInterceptor的功能开启需要在log4j里设置com.taobao.itest日志级别为debug
 * 
 *       <logger name="com.taobao.itest"> <level value="DEBUG" /> </logger>
 * @author yedu
 * 
 */
public class LogInterceptor implements MethodInterceptor {
	private final Log log = LogFactory.getLog(getClass());
	private String methodNameExpression;// To print the log of the method name
										// regular expression
	private boolean logInvokeParams = false;// Default does not print into the
											// reference
	private boolean logInvokeResult = true;// Default Call the result the
											// printer

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object object = invocation.getThis();
		Method method = invocation.getMethod();
		Object[] args = invocation.getArguments();

		if (!log.isDebugEnabled() || !matches(method.getName()))
			return invocation.proceed();

		StackTraceElement[] stes = new Throwable().getStackTrace();
		StackTraceElement testSte = null;
		for (int i = 0; i < stes.length; i++) {
			if (stes[i].getClassName().endsWith("Test")) {
				testSte = stes[i];
				break;
			}
		}

		if (testSte == null)
			return invocation.proceed();

		boolean isTestMethod = testSte.getMethodName().startsWith("test");
		if (testSte == null || !isTestMethod)
			return invocation.proceed();
		String callerClassSimpleName = testSte.getClassName().substring(
				testSte.getClassName().lastIndexOf(".") + 1);
		if (!callerClassSimpleName.endsWith("Test"))
			return invocation.proceed();
		String callerMethodName = testSte.getMethodName();
		int lineNumber = testSte.getLineNumber();
		String callerInfo = callerClassSimpleName
				+ "."
				+ callerMethodName
				+ (lineNumber >= 0 ? "(" + lineNumber + ")"
						: "(Unknown Source)");
		String invokeClassSimpleName = ClassUtils.getShortClassName(object
				.getClass());
		String invokeInfo = invokeClassSimpleName + "." + method.getName();
		String baseInfo = callerInfo + "  |" + invokeInfo + " ";
		if (logInvokeParams) {
			log.debug(baseInfo + " invoked with params:");
			for (int i = 0; i < args.length; i++) {
				log.debug("param " + (i + 1) + ": ");
				println(args[i]);
			}
		}
		Object result = null;
		try {
			result = invocation.proceed();
		} catch (Exception e) {
			log.debug(baseInfo + " throw exception: ");
			log.debug(e.getMessage());
			throw e;
		}
		if (logInvokeResult) {
			log.debug(baseInfo + " invoked result is: ");
			println(result);
		}

		return result;
	}

	private boolean matches(String methodName) {
		if (methodNameExpression == null)
			return true;// By default all methods
		return Pattern.compile(methodNameExpression).matcher(methodName)
				.matches();
	}

	private void println(Object obj) {
		if (obj == null)
			return;
		if (obj.getClass().isPrimitive()) {
			log.debug(obj);
		} else if (obj instanceof Collection) {
			List<Object> list = new ArrayList<Object>();
			for (Object object : (Collection<?>) obj) {
				list.add(stringBuilder(object));
			}
			log.debug(list);
		} else {
			log.debug(stringBuilder(obj));
		}
	}

	private String stringBuilder(Object obj) {
		if (null == obj) {
			return null;
		}
		if (ReflectionUtils.findMethod(obj.getClass(), "toString") != null) {
			return obj.toString();
		} else {
			return ToStringBuilder.reflectionToString(obj,
					ToStringStyle.MULTI_LINE_STYLE);
		}
	}

	public String getMethodNameExpression() {
		return methodNameExpression;
	}

	public void setMethodNameExpression(String methodNameExpression) {
		this.methodNameExpression = methodNameExpression;
	}

	public boolean isLogInvokeParams() {
		return logInvokeParams;
	}

	public void setLogInvokeParams(boolean logInvokeParams) {
		this.logInvokeParams = logInvokeParams;
	}

	public boolean isLogInvokeResult() {
		return logInvokeResult;
	}

	public void setLogInvokeResult(boolean logInvokeResult) {
		this.logInvokeResult = logInvokeResult;
	}

}
