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
package com.taobao.itest.tb.tair.mock;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.taobao.common.tair.DataEntry;
import com.taobao.common.tair.Result;
import com.taobao.common.tair.ResultCode;
import com.taobao.common.tair.TairManager;

/**
 * tair manager的xml store 实现，主要用于单元测试，模拟Tair的功能
 * 
 * @author leijuan
 */
public class TairManagerXmlStoreImpl implements TairManager {
	/**
	 * tair store
	 */
	private Map<Integer, Map<Object, Object>> tairStore = new HashMap<Integer, Map<Object, Object>>();

	/**
	 * 设置xml文件列表，多个文件逗号分隔，所有的文件都是从classpath中加载
	 * 
	 * @param files
	 *            文件列表
	 * @throws Exception
	 *             exception
	 */
	@SuppressWarnings({ "unchecked" })
	public void setXmlStoreFiles(String files) throws Exception {
		SAXReader reader = new SAXReader();
		for (String filePath : files.split("[,:]")) {
			Document doc = reader.read(this.getClass().getResourceAsStream(filePath.trim()));
			List<Element> entryElements = doc.getRootElement().elements("entry");
			for (Element entryElement : entryElements) {
				int namespace = Integer.valueOf(entryElement.attributeValue("namespace"));
				String key = entryElement.attributeValue("key");
				String content = entryElement.getTextTrim();
				if (entryElement.attributeValue("href") != null) {
					content = IOUtils.toString(this.getClass().getResourceAsStream(entryElement.attributeValue("href")));
				}
				String language = entryElement.attributeValue("language");
				if ("groovy".equalsIgnoreCase(language)) {
					getNameSpaceStore(namespace).put(key, evalGroovy(content));
				} else {
					getNameSpaceStore(namespace).put(key, content);
				}
			}
		}

	}

	/**
	 * get name space store
	 * 
	 * @param namespace
	 *            name space id
	 * @return name space store
	 */
	private Map<Object, Object> getNameSpaceStore(int namespace) {
		Map<Object, Object> namepaceStore = tairStore.get(namespace);
		if (namepaceStore == null) {
			namepaceStore = new HashMap<Object, Object>();
			tairStore.put(namespace, namepaceStore);
		}
		return namepaceStore;
	}

	/**
	 * 获取数据
	 * 
	 * @param namespace
	 *            数据所在的namespace
	 * @param key
	 *            要获取的数据的key
	 * @return result data entry
	 */
	public Result<DataEntry> get(int namespace, Object key) {
		if (getNameSpaceStore(namespace).containsKey(key)) {
			return new Result<DataEntry>(ResultCode.SUCCESS, new DataEntry(key, getNameSpaceStore(namespace).get(key)));
		} else {
			return new Result<DataEntry>(ResultCode.DATANOTEXSITS);
		}
	}

	/**
	 * 批量获取数据
	 * 
	 * @param namespace
	 *            数据所在的namespace
	 * @param keys
	 *            要获取的数据的key列表
	 * @return 如果成功，返回的数据对象为一个Map<Key, Value>
	 */
	public Result<List<DataEntry>> mget(int namespace, List<Object> keys) {
		List<DataEntry> entries = new ArrayList<DataEntry>();
		for (Object key : keys) {
			entries.add(get(namespace, key).getValue());
		}
		return new Result<List<DataEntry>>(ResultCode.SUCCESS, entries);
	}

	/**
	 * 设置数据，如果数据已经存在，则覆盖，如果不存在，则新增 如果是新增，则有效时间为0，即不失效 如果是更新，则不检查版本，强制更新
	 * 
	 * @param namespace
	 *            数据所在的namespace
	 * @param key
	 *            key
	 * @param value
	 *            value
	 * @return result code
	 */
	public ResultCode put(int namespace, Object key, Serializable value) {
		getNameSpaceStore(namespace).put(key, value);
		return ResultCode.SUCCESS;
	}

	/**
	 * 设置数据，如果数据已经存在，则覆盖，如果不存在，则新增
	 * 
	 * @param namespace
	 *            数据所在的namespace
	 * @param key
	 *            数据的key
	 * @param value
	 *            数据的value
	 * @param version
	 *            数据的版本，如果和系统中数据的版本不一致，则更新失败
	 * @return result code
	 */
	public ResultCode put(int namespace, Object key, Serializable value, int version) {
		return put(namespace, key, value);
	}

	/**
	 * 设置数据，如果数据已经存在，则覆盖，如果不存在，则新增
	 * 
	 * @param namespace
	 *            数据所在的namespace
	 * @param key
	 *            数据的key
	 * @param value
	 *            数据的value
	 * @param version
	 *            数据的版本，如果和系统中数据的版本不一致，则更新失败
	 * @param expireTime
	 *            数据的有效时间，单位为秒
	 * @return result code
	 */
	public ResultCode put(int namespace, Object key, Serializable value, int version, int expireTime) {
		return put(namespace, key, value, version);
	}

	/**
	 * 删除key对应的数据
	 * 
	 * @param namespace
	 *            数据所在的namespace
	 * @param key
	 *            数据的key
	 * @return result code
	 */
	public ResultCode delete(int namespace, Object key) {
		getNameSpaceStore(namespace).remove(key);
		return ResultCode.SUCCESS;
	}

	/**
	 * 失效数据，该方法将失效由失效服务器配置的多个实例中当前group下的数据
	 * 
	 * @param namespace
	 *            数据所在的namespace
	 * @param key
	 *            要失效的key
	 * @return result code
	 */
	public ResultCode invalid(int namespace, Object key) {
		return delete(namespace, key);
	}

	/**
	 * 批量失效数据，该方法将失效由失效服务器配置的多个实例中当前group下的数据
	 * 
	 * @param namespace
	 *            数据所在的namespace
	 * @param keys
	 *            要失效的key列表
	 * @return result code
	 */
	public ResultCode minvalid(int namespace, List<? extends Object> keys) {
		for (Object key : keys) {
			invalid(namespace, key);
		}
		return ResultCode.SUCCESS;
	}

	/**
	 * 批量删除，如果全部删除成功，返回成功，否则返回失败
	 * 
	 * @param namespace
	 *            数据所在的namespace
	 * @param keys
	 *            要删除数据的key列表
	 * @return result code
	 */
	public ResultCode mdelete(int namespace, List<Object> keys) {
		for (Object key : keys) {
			delete(namespace, key);
		}
		return ResultCode.SUCCESS;
	}

	/**
	 * 将key对应的数据加上value，如果key对应的数据不存在，则新增，并将值设置为defaultValue
	 * 如果key对应的数据不是int型，则返回失败
	 * 
	 * @param namespace
	 *            数据所在的namspace
	 * @param key
	 *            数据的key
	 * @param value
	 *            要加的值
	 * @param defaultValue
	 *            不存在时的默认值
	 * @return 更新后的值
	 */
	public Result<Integer> incr(int namespace, Object key, int value, int defaultValue) {
		int newValue = defaultValue;
		Map<Object, Object> nameSpaceStore = getNameSpaceStore(namespace);
		if (nameSpaceStore.get(key) == null) {
			nameSpaceStore.put(key, newValue);
		} else {
			Integer temp = (Integer) nameSpaceStore.get(key);
			newValue = temp + value;
			nameSpaceStore.put(key, newValue);
		}
		return new Result<Integer>(ResultCode.SUCCESS, newValue);
	}

	/**
	 * 将key对应的数据减去value，如果key对应的数据不存在，则新增，并将值设置为defaultValue
	 * 如果key对应的数据不是int型，则返回失败
	 * 
	 * @param namespace
	 *            数据所在的namspace
	 * @param key
	 *            数据的key
	 * @param value
	 *            要减去的值
	 * @param defaultValue
	 *            不存在时的默认值
	 * @return 更新后的值
	 */
	public Result<Integer> decr(int namespace, Object key, int value, int defaultValue) {
		return incr(namespace, key, (0 - value), defaultValue);
	}

	/**
	 * 获取客户端的版本
	 */
	public String getVersion() {
		return "1.0.0-Mock";
	}

	/**
	 * 执行Groovy代码
	 * 
	 * @param groovyCode
	 *            groovy代码
	 * @return 执行结果
	 */
	private Object evalGroovy(String groovyCode) {
		Binding binding = new Binding();
		GroovyShell shell = new GroovyShell(binding);
		return shell.evaluate(groovyCode);
	}
}
