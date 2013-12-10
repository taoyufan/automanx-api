package com.taobao.itest.util;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.FatalBeanException;

public class PopulateUtil {

	/**
	 * Value will be copied to the bean properties
	 * 
	 * @param bean
	 * @return
	 */
	public static Object populate(Object bean, Map<?, ?> properties) {
		try {
			BeanUtils.populate(bean, properties);
		} catch (Exception e) {
			throw new FatalBeanException("Could not copy properties to target",
					e);
		}
		return bean;
	}

	/**
	 * Will map the value of copy to the bean, to support a nested JavaBean
	 * 
	 * @param type
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public static <T> T populate(Class<T> type, Map<?, ?> map) {
		Object bean = null;
		try {
			bean = type.newInstance();
			Iterator<?> it = map.entrySet().iterator();
			Map<Object, ?> tmp = new HashMap<Object, Object>();
			boolean hasSub = false;
			while (it.hasNext()) {
				Entry<?, ?> entry = (Entry<?, ?>) it.next();
				Object key = entry.getKey();

				if (key.toString().contains(".")) {
					hasSub = true;
					tmp.put(key.toString().substring(0,
							key.toString().indexOf(".")), null);
				} else {
					tmp.put(key, null);
				}

			}
			// If you include the child objects
			if (hasSub) {
				PropertyDescriptor[] props = BeanUtilsBean.getInstance()
						.getPropertyUtils().getPropertyDescriptors(bean);
				for (int i = 0; i < props.length; i++) {
					Class<?> subType = props[i].getPropertyType();
					if (!subType.getName().startsWith("java.")
							&& !subType.getName().startsWith("[Ljava.")
							&& !subType.isPrimitive()) {
						try {
							if (tmp.containsKey(props[i].getName())) {
								props[i].getWriteMethod().invoke(bean,
										subType.newInstance());
							}
						} catch (Exception e) {

						}
					}
				}
			}
			BeanUtils.populate(bean, map);
		} catch (Exception e) {
			throw new FatalBeanException("Could not copy properties to target",
					e);
		}
		return (T) bean;
	}

}
