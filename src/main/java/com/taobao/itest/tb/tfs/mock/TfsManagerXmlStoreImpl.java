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
package com.taobao.itest.tb.tfs.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.taobao.common.tfs.TfsManager;

/**
 * TFS的XML Store的实现，主要用于测试
 * 
 * @author leijuan
 */
public class TfsManagerXmlStoreImpl implements TfsManager {
	/**
	 * store中的文件列表
	 */
	private Map<String, TfsFile> tfsStore = new HashMap<String, TfsFile>();

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
			List<Element> fileElements = doc.getRootElement().elements("file");
			for (Element fileElement : fileElements) {
				TfsFile tfsFile = new TfsFile();
				tfsFile.setKey(fileElement.attributeValue("key"));
				tfsFile.setHref(fileElement.attributeValue("href"));
				tfsFile.setSuffix(fileElement.attributeValue("suffix"));
				tfsFile.setDesc(fileElement.attributeValue("desc"));
				if (StringUtils.isNotEmpty(tfsFile.getHref())) {
					tfsFile.setRawContent(IOUtils.toByteArray(this.getClass().getResourceAsStream(tfsFile.getHref())));
				} else {
					tfsFile.setRawContent(fileElement.getTextTrim().getBytes());
				}
				tfsStore.put(tfsFile.getKey(), tfsFile);
			}
		}

	}

	/**
	 * 是否可用
	 * 
	 * @return 可用标识
	 */
	public boolean isEnable() {
		return true;
	}

	/**
	 * 改变master ip地址
	 */
	public int setMasterIP(String ipaddr) {
		return 0;
	}

	/**
	 * 得到当前主设备的IP
	 * 
	 * @return master ip
	 */
	public String getMasterIP() {
		return "127.0.0.1";
	}

	/**
	 * 生成一个新的tfs文件名，tfs会根据prefix来确定文件名，也就是说prefix是一样的，生成的文件名也一样
	 * 
	 * @param suffix
	 *            后缀名
	 * @return tfs file name
	 */
	public String newTfsFileName(String suffix) {
		return "MOCK-" + RandomStringUtils.random(8);
	}

	/**
	 * 读取一个tfs的文件到本地文件
	 * 
	 * @param tfsFileName
	 *            tfs file name
	 * @param suffix
	 *            suffix
	 * @param localFileName
	 *            local file name
	 * @return 成功标识
	 */
	public boolean fetchFile(String tfsFileName, String suffix, String localFileName) {
		if (tfsStore.containsKey(tfsFileName) && tfsStore.get(tfsFileName).isSuffixSame(suffix)) {
			try {
				FileOutputStream fileInputOutput = new FileOutputStream(new File(localFileName));
				IOUtils.write(tfsStore.get(tfsFileName).getRawContent(), fileInputOutput);
				IOUtils.closeQuietly(fileInputOutput);
				return true;
			} catch (Exception ignore) {
				return false;
			}

		}
		return false;
	}

	/**
	 * 保存一个文件
	 * 
	 * @param localFileName
	 *            local file name
	 * @param tfsFileName
	 *            tfs file name
	 * @param suffix
	 *            后缀名
	 * @return 文件名
	 */
	public String saveFile(String localFileName, String tfsFileName, String suffix) {
		try {
			return saveFile(IOUtils.toByteArray(new FileInputStream(localFileName)), tfsFileName, suffix);
		} catch (Exception ignore) {
			return null;
		}
	}

	/**
	 * 保存一个byte[]到TFS
	 * 
	 * @param data
	 *            要保存的内容
	 * @param tfsFileName
	 *            指定的tfs文件名
	 * @param suffix
	 *            指定的后缀
	 * @return 保存成功的TFS文件名
	 */
	public String saveFile(byte[] data, String tfsFileName, String suffix) {
		TfsFile tfsFile = new TfsFile();
		tfsFile.setKey(tfsFileName);
		tfsFile.setSuffix(suffix);
		tfsFile.setRawContent(data);
		tfsStore.put(tfsFile.getKey(), tfsFile);
		return tfsFile.getKey();
	}

	/**
	 * 删除一个文件
	 * 
	 * @param tfsFileName
	 *            tfs file name
	 * @param suffix
	 *            后缀名
	 * @return 删除文件
	 */
	public boolean unlinkFile(String tfsFileName, String suffix) {
		tfsStore.remove(tfsFileName);
		return true;
	}

	/**
	 * 读取一个tfs的文件到output
	 * 
	 * @param tfsFileName
	 *            tfs file name
	 * @param suffix
	 *            后缀名
	 * @param output
	 *            output stream
	 * @return 是否成功
	 */
	public boolean fetchFile(String tfsFileName, String suffix, OutputStream output) {
		if (tfsStore.containsKey(tfsFileName) && tfsStore.get(tfsFileName).isSuffixSame(suffix)) {
			try {
				output.write(tfsStore.get(tfsFileName).getRawContent());
				return true;
			} catch (Exception ignore) {
				return false;
			}
		}
		return false;
	}

	/**
	 * 保存一个不重复文件
	 * 
	 * @param localFileName
	 *            local file name
	 * @param tfsFileName
	 *            tfs file name
	 * @param suffix
	 *            后缀名
	 * @return file name
	 */
	public String saveUniqueFile(String localFileName, String tfsFileName, String suffix) {
		File localFile = new File(localFileName);
		if (!localFile.exists()) {
			return saveFile(localFileName, tfsFileName, suffix);
		}
		return null;
	}

	/**
	 * 保存一个不重复的文件(byte[])到TFS
	 * 
	 * @param data
	 *            要保存的内容
	 * @param tfsFileName
	 *            指定的tfs文件名
	 * @param suffix
	 *            指定的后缀
	 * @return 保存成功的TFS文件名
	 */
	public String saveUniqueFile(byte[] data, String tfsFileName, String suffix) {
		try {
			File tempFile = File.createTempFile(RandomStringUtils.random(6), suffix);
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			fetchFile(tfsFileName, suffix, outputStream);
			IOUtils.closeQuietly(outputStream);
			return tempFile.getAbsolutePath();
		} catch (Exception ignore) {
			return null;
		}
	}

	/**
	 * 删除一个不重复文件
	 * 
	 * @param tfsFileName
	 *            tfs file name
	 * @param suffix
	 *            后缀名
	 * @return 删除不重复的文件
	 */
	public int unlinkUniqueFile(String tfsFileName, String suffix) {
		tfsStore.remove(tfsFileName);
		return 0;
	}

	/**
	 * 临时隐藏一个文件
	 * 
	 * @param fileName
	 *            file
	 * @param suffix
	 *            suffix
	 * @param option
	 *            1 隐藏 0 恢复
	 * @return 成功标识
	 */
	public boolean hideFile(String fileName, String suffix, int option) {
		// 空实现
		return true;
	}

	/**
	 * TFS file object
	 * 
	 * @author leijuan
	 */
	private class TfsFile {
		/**
		 * tfs key
		 */
		private String key;
		/**
		 * TFS 文件的后缀名
		 */
		private String suffix;
		/**
		 * 链接文件
		 */
		private String href;
		/**
		 * 文件描述
		 */
		private String desc;
		/**
		 * 原始内容
		 */
		private byte[] rawContent;

		/**
		 * 获取 tfs key
		 * 
		 * @return tfs key
		 */
		public String getKey() {
			return key;
		}

		/**
		 * 设置 tfs key
		 * 
		 * @param key
		 *            tfs key
		 */
		public void setKey(String key) {
			this.key = key;
		}

		/**
		 * 获取 TFS 文件的后缀名
		 * 
		 * @return TFS 文件的后缀名
		 */
		@SuppressWarnings("unused")
		public String getSuffix() {
			return suffix;
		}

		/**
		 * 设置 TFS 文件的后缀名
		 * 
		 * @param suffix
		 *            TFS 文件的后缀名
		 */
		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		/**
		 * 获取 原始内容
		 * 
		 * @return 原始内容
		 */
		public byte[] getRawContent() {
			return rawContent;
		}

		/**
		 * 设置 原始内容
		 * 
		 * @param rawContent
		 *            原始内容
		 */
		public void setRawContent(byte[] rawContent) {
			this.rawContent = rawContent;
		}

		/**
		 * 判断后缀名是否一致
		 * 
		 * @param suffix
		 *            后缀名
		 * @return 一致标识
		 */
		public boolean isSuffixSame(String suffix) {
			return suffix != null ? suffix.equals(this.suffix) : this.suffix == null;
		}
	}

}
