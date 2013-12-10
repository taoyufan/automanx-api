package com.taobao.itest.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * Luo Feng style handle field names with the underscore style compatible with
 * the java class of database fields in the automatic mapping
 * 
 * @author yedu
 * 
 */
@SuppressWarnings("unchecked")
public class CamelCasingHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = 1L;

	public boolean containsKey(Object key) {
		return super.containsKey(toCamelCasing(key.toString()));
	}

	public V get(Object key) {
		return super.get(toCamelCasing(key.toString()));
	}

	/**
	 * If the first letter capitalized, then all to lowercase
	 */
	public V put(K key, V value) {
		String tmp = key.toString();
		if (Character.isUpperCase(tmp.charAt(0))) {
			tmp = tmp.toLowerCase();
		}
		return super.put((K) toCamelCasing(tmp), value);
	}

	public void putAll(@SuppressWarnings("rawtypes") Map m) {
		Iterator<?> iter = m.keySet().iterator();
		while (iter.hasNext()) {
			K key = (K) iter.next();
			V value = (V) m.get(key);
			this.put(key, value);
		}
	}

	public V remove(Object key) {
		return super.remove(toCamelCasing(key.toString()));
	}

	/**
	 * 将下划线连接的字符串替换为驼峰风格,方便JavaBean拷贝
	 * <p/>
	 * <h2>Example:</h2>
	 * <code>toCamelCasing("pic_path")</code> will return picPath
	 * 
	 * @param s
	 * @return
	 */
	public static String toCamelCasing(String s) {
		if (s == null) {
			return s;
		}

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < s.length() - 1; i++) {
			char ch = s.charAt(i);
			if (ch != '_') {
				buffer.append(ch);
			} else {
				char nextChar = s.charAt(i + 1);
				if (nextChar != '_') {
					if (buffer.toString().length() < 2) {
						buffer.append(Character.toLowerCase(nextChar));
					} else {
						buffer.append(Character.toUpperCase(nextChar));
					}
					i++;
				}
			}
		}
		char lastChar = s.charAt(s.length() - 1);
		if (lastChar != '_') {
			buffer.append(lastChar);
		}

		return buffer.toString();
	}

}
