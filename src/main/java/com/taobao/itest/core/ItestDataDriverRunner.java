package com.taobao.itest.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import com.taobao.itest.annotation.DataProvider;
import com.taobao.itest.annotation.ITestCaseLevel;
import com.taobao.itest.util.DataPrepareUtils;

/**
 * @author yufan.yq
 * 
 * 
 * <br/>
 *         为参数化的itest新写的runner，兼容之前的TC
 *         当需要使用时在TC类前写@RunWith(ItestDataDriverRunner.class)<br/>
 *         example：<br/>
 *         public class TestWithParam extends ITestDataDriverBaseCase{<br/>
 * 
 *         public Object [][] testRunner = new Object[][]{<br/>
 *         {10,21L,new MyParamObj("yufan1",1,2)},<br/>
 *         {11,21L,new MyParamObj("yufan2",4,2)},<br/>
 *         {1,21L,new MyParamObj("yufan3",4,2)},<br/>
 *         {1,21L,new MyParamObj("yufan4",4,2)}<br/>
 *         };<br/>
 * 
 * @Test<br/>
 * @DataProvider<br/> public void testRunner(int i,long j,MyParamObj k){<br/>
 *                    System.out.println(i);<br/>
 *                    System.out.println(j);<br/>
 *                    System.out.println(k);<br/>
 *                    Assert.assertEquals("yufan2", k.foo);<br/>
 *                    }
 * 
 * @Test<br/> public void test_没有参数的(){<br/>
 *            System.out.println("没有参数也可以的");<br/>
 *            }<br/>
 *            }<br/>
 * 
 *            <p>
 *            其中MyParamObj为一普通对象<br/>
 *            对应的参数public Object [][] testRunner：<br/>
 *            <li>必须为public
 *            <li>名字应和@DataProvider注解中fieldName值相同，注解值默认和方法名相同
 * 
 * 
 */
@SuppressWarnings("rawtypes")
public class ItestDataDriverRunner extends ITestJunit4ClassRunner {
	private List<FrameworkMethod> children;

	@SuppressWarnings("unchecked")
	public ItestDataDriverRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected String testName(FrameworkMethod method) {
		return ((method instanceof FrameworkMethodWithParameters || method instanceof FrameworkMethodWithParametersLater) ? method.toString() : super.testName(method));
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		if (children == null) {
			children = new ArrayList<FrameworkMethod>();
			TestClass testClass = getTestClass();
			// add by yufan.yq 2013.1.4
			Set<String> testSet = null;
			String keludeTest = System.getProperty("AutomanXTest");
			if (null != keludeTest) {
				String[] testNames = keludeTest.split("#");
				testSet = new HashSet<String>(testNames.length);
				for (int i = 0; i < testNames.length; i++) {
					testSet.add(testNames[i]);
				}
			}
			
			Set<String> caseLevelSet = null;
			String itestCaseLevel = System.getProperty("ITestCaseLevel");
			if (null != itestCaseLevel) {
				String[] caseLevels = itestCaseLevel.split("#");
				testSet = new HashSet<String>(caseLevels.length);
				for (int i = 0; i < caseLevels.length; i++) {
					testSet.add(caseLevels[i]);
				}
			}
			
			for (FrameworkMethod method : testClass.getAnnotatedMethods(Test.class)) {
				// add by yufan.yq 2013.1.4
				if (null != testSet && !testSet.contains(method.getName())) {
					continue;// 如果包含该参数，则包含改名字的方法才执行
				}
				ITestCaseLevel level = method.getAnnotation(ITestCaseLevel.class);
				if(null!=level && null!=caseLevelSet && !caseLevelSet.contains(level.value())){
					continue;
				}
				
				if (method.getMethod().getParameterTypes().length == 0) {
					// 标准的 JUnit TC
					children.add(method);
					continue;
				} else {
					// 参数化TC
					List<? extends FrameworkMethod> parameterizedTestMethods;
					parameterizedTestMethods = computeParameterizedTestMethods(method.getMethod());
					children.addAll(parameterizedTestMethods);
				}
			}
		}
		return children;
	}

	private List<FrameworkMethod> computeParameterizedTestMethods(Method method) {
		List<FrameworkMethod> result = new ArrayList<FrameworkMethod>();
		if( null != method.getAnnotation(Ignore.class)){
			//add by yufan 2013.11.28 如果是ignore的就不去计算参数了
			return result;
		}
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[][] generatedParams = null;
		generatedParams = DataPrepareUtils.getParams(getTestClass(), method);
		if (null == generatedParams || generatedParams.length == 0) {
			throw new RuntimeException("No parameter values available for method: " + method);
		}
		DataProvider later = method.getAnnotation(DataProvider.class);
		if (!later.injectLater()) {
			for (int i = 0; i < generatedParams.length; i++) {
				Object[] preParams = generatedParams[i];
				Object[] usedParams = new Object[parameterTypes.length];
				System.arraycopy(preParams, 0, usedParams, 0, Math.min(preParams.length, usedParams.length));
				result.add(new FrameworkMethodWithParameters(method, i + 1, usedParams));
			}
		} else {
			for (int i = 0; i < generatedParams.length; i++) {
				result.add(new FrameworkMethodWithParametersLater(method, i + 1, getTestClass()));
			}
		}
		return result;
	}

	@Override
	protected void validateTestMethods(List<Throwable> errors) {
		validatePublicVoidMethods(Test.class, false, errors);
	}

	private void validatePublicVoidMethods(Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors) {
		List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
		for (FrameworkMethod eachTestMethod : methods)
			eachTestMethod.validatePublicVoid(isStatic, errors);
	}
}
