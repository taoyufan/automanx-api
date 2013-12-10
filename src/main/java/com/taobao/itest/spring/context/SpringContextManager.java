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

package com.taobao.itest.spring.context;

import static org.unitils.util.AnnotationUtils.getFieldsAnnotatedWith;
import static org.unitils.util.AnnotationUtils.getMethodsAnnotatedWith;
import static org.unitils.util.ReflectionUtils.getPropertyName;
import static org.unitils.util.ReflectionUtils.invokeMethod;
import static org.unitils.util.ReflectionUtils.isSetter;
import static org.unitils.util.ReflectionUtils.setFieldValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;

import com.taobao.hsf.hsfunit.util.ServiceUtil;

/**
 * 
 * 
 * @author <a href="mailto:yedu@taobao.com">yedu</a>
 * 
 * 
 */
public class SpringContextManager {

	private static final Map<String, ApplicationContext> contextKeyToContextMap = new ConcurrentHashMap<String, ApplicationContext>();
	private static ApplicationContext currentApplicationContext;

	public static ApplicationContext loadApplicationContext(SpringContextLoader contextLoader, String... locations) throws Exception {
		return contextLoader.loadContext(locations);
	}

	public static void put(String key, ApplicationContext context) {
		contextKeyToContextMap.put(key, context);
		currentApplicationContext = context;
	}

	public static ApplicationContext get(String key) {
		ApplicationContext context = contextKeyToContextMap.get(key);
		currentApplicationContext = context;
		return context;
	}

	public static ApplicationContext getApplicationContext() {
		return currentApplicationContext;
	}

	public static void findAnnotatedFieldsAndInjectBeanByName(Class<? extends Annotation> annotationType, Object object, ApplicationContext applicationContext) {
		Set<Field> fields = getFieldsAnnotatedWith(object.getClass(), annotationType);

		for (Field field : fields) {
			if (!hasBeanInjected(field, object)) {
				// applicationContext = determineApplicationContext(
				// applicationContext, field);
				setFieldValue(object, field, getBean(field.getName(), applicationContext));
			}
		}
	}

	private static boolean hasBeanInjected(Field field, Object object) {
		field.setAccessible(true);
		try {
			return field.get(object) != null;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// private static ApplicationContext determineApplicationContext(
	// ApplicationContext applicationContext, Field field) {
	// ITestSpringBean iTestSpringBeanAnnotation = field
	// .getAnnotation(ITestSpringBean.class);
	// if (iTestSpringBeanAnnotation != null) {
	// String key = iTestSpringBeanAnnotation.springContextKey();
	// if (key != null && key.length() > 0) {
	// applicationContext = contextKeyToContextMap.get(key);
	// }
	// }
	// return applicationContext;
	// }

	public static void findAnnotatedMethodsAndInjectBeanByName(Class<? extends Annotation> annotationType, Object object, ApplicationContext applicationContext) {
		Set<Method> methods = getMethodsAnnotatedWith(object.getClass(), annotationType);
		for (Method method : methods) {
			try {
				if (!isSetter(method)) {
					invokeMethod(object, method, getBean(method.getParameterTypes()[0], applicationContext));
				} else {
					invokeMethod(object, method, getBean(getPropertyName(method), applicationContext));
				}

			} catch (InvocationTargetException e) {
				throw new RuntimeException("Unable to assign the Spring bean value to method annotated with @" + annotationType.getSimpleName() + ". Method " + "has thrown an exception.", e.getCause());
			}
		}
	}

	public static void registerBeanDefinition(Field field, ApplicationContext applicationContext) {
		@SuppressWarnings("deprecation")
		RootBeanDefinition beanDefinition = new RootBeanDefinition(field.getType(), true);
		beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) (applicationContext.getAutowireCapableBeanFactory());
		defaultListableBeanFactory.registerBeanDefinition(field.getName(), beanDefinition);
	}

	private static Object getBean(String name, ApplicationContext applicationContext) {

		// return applicationContext.getBean(name);
		// modified by yufan hsf bean no need to wait config server load
		Object bean = applicationContext.getBean(name);
		if (Proxy.isProxyClass(bean.getClass())) {
			ServiceUtil.waitServiceReady(name);
			try {//所有hsf服务注入前sleep2s
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return bean;
	}

	private static Object getBean(Class<?> type, ApplicationContext applicationContext) {

		String[] names = applicationContext.getBeanNamesForType(type);
		if (names.length > 1) {
			throw new RuntimeException("Found more than one bean which type is " + type);
		}
		return getBean(applicationContext.getBeanNamesForType(type)[0], applicationContext);

	}
}
