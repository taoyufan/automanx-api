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
package com.taobao.itest.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnnotationUtil {

	@SuppressWarnings("rawtypes")
	public static List<Class> findClassesAnnotationDeclaredWith(Class<?> clazz,
			Class<? extends Annotation> annotationType) {
		List<Class> classesAnnotationDeclared = new ArrayList<Class>();
		Class<?> classAnnotationDeclared = findClassAnnotationDeclaredWith(
				clazz, annotationType);
		while (classAnnotationDeclared != null) {
			classesAnnotationDeclared.add(classAnnotationDeclared);
			classAnnotationDeclared = findClassAnnotationDeclaredWith(
					classAnnotationDeclared.getSuperclass(), annotationType);
		}
		return classesAnnotationDeclared;
	}

	public static <A extends Annotation> A findAnnotation(Class<?> clazz,
			Class<A> annotationType) {
		A annotation = clazz.getAnnotation(annotationType);
		if (annotation != null) {
			return annotation;
		}
		for (Class<?> ifc : clazz.getInterfaces()) {
			annotation = findAnnotation(ifc, annotationType);
			if (annotation != null) {
				return annotation;
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if (superClass == null || superClass == Object.class) {
			return null;
		}
		return findAnnotation(superClass, annotationType);
	}

	public static Class<?> findClassAnnotationDeclaredWith(Class<?> clazz,
			Class<? extends Annotation> annotationType) {
		if (clazz == null || clazz.equals(Object.class)) {
			return null;
		}
		return (isAnnotationDeclaredWith(clazz, annotationType)) ? clazz
				: findClassAnnotationDeclaredWith(clazz.getSuperclass(),
						annotationType);
	}

	public static boolean isAnnotationDeclaredWith(Class<?> clazz,
			Class<? extends Annotation> annotationType) {
		boolean annotationDeclared = false;
		for (Annotation annotation : Arrays.asList(clazz
				.getDeclaredAnnotations())) {
			if (annotation.annotationType().equals(annotationType)) {
				annotationDeclared = true;
				break;
			}
		}
		return annotationDeclared;
	}

}
