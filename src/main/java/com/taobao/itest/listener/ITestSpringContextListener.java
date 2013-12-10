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

package com.taobao.itest.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.taobao.itest.annotation.ITestSpringContext;
import com.taobao.itest.core.AbstractTestListener;
import com.taobao.itest.core.TestContext;
import com.taobao.itest.spring.context.GenericXmlContextLoader;
import com.taobao.itest.spring.context.SpringContextLoader;
import com.taobao.itest.spring.context.SpringContextManager;
import com.taobao.itest.util.AnnotationUtil;

/**
 * 
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 * 
 */
public class ITestSpringContextListener extends AbstractTestListener {

	private static final Log logger = LogFactory
			.getLog(ITestSpringContextListener.class);

	/**
	 * modified by <a href="mailto:yufan.yq@taobao.com">yufan.yq</a>,
	 * 将spring的加载时机修改为beforeTestClass，
	 * 这样别的Listener可以在beforeTestClass时就获得Spring加载的内容, 另外删除了一些几乎不会使用的处理加载路径的代码
	 */
	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		Class<?> testClass = testContext.getTestClass();
		if (AnnotationUtil.findAnnotation(testClass, ITestSpringContext.class) == null) {
			return;
		}
		// spring configure file location
		String[] locations;
		// springContextManager key name
		String key;
		// The original processing logic
		locations = retrieveLocations(testClass);
		locations = ResourceLocationProcessingUtil.modifyLocations(testClass,
				locations);
		key = ArrayUtils.toString(locations);

		ApplicationContext applicationContext = SpringContextManager.get(key);
		if (applicationContext == null) {
			SpringContextLoader contextLoader = new GenericXmlContextLoader();
			try {
				applicationContext = SpringContextManager
						.loadApplicationContext(contextLoader, locations);
				logger.info("Spring 配置文件加载好了亲！");
			} catch (Exception e) {
				String err = "load Spring ApplicationContext faild ,locations= '"
						+ ArrayUtils.toString(locations)
						+ "' ,detail exception message is:" + e.getMessage();
				e.printStackTrace();
				logger.warn(err);
				throw new RuntimeException(err, e);
			}
			SpringContextManager.put(key, applicationContext);
		}
	}

	private String[] retrieveLocations(Class<?> clazz) {
		Class<ITestSpringContext> annotationType = ITestSpringContext.class;
		@SuppressWarnings("rawtypes")
		List<Class> classesAnnotationDeclared = AnnotationUtil
				.findClassesAnnotationDeclaredWith(clazz, annotationType);
		List<String> locationsList = new ArrayList<String>();
		for (Class<?> classAnnotationDeclared : classesAnnotationDeclared) {
			ITestSpringContext iTestSpringContext = classAnnotationDeclared
					.getAnnotation(annotationType);
			String[] value = iTestSpringContext.value();
			String[] locations = iTestSpringContext.locations();
			if (!ArrayUtils.isEmpty(value) && !ArrayUtils.isEmpty(locations)) {
				String msg = String
						.format("Test class [%s] has been configured with @ITestSpringContext' 'value' [%s] and 'locations' [%s] attributes. Use one or the other, but not both.",
								classAnnotationDeclared,
								ArrayUtils.toString(value),
								ArrayUtils.toString(locations));
				throw new RuntimeException(msg);
			} else if (!ArrayUtils.isEmpty(value)) {
				locations = value;
			}

			if (locations != null) {
				locationsList.addAll(0, Arrays.<String> asList(locations));
			}
			if (!iTestSpringContext.inheritLocations()) {
				break;
			}
		}
		return locationsList.toArray(new String[locationsList.size()]);
	}

}
