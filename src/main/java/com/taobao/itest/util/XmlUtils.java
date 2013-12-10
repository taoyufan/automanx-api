package com.taobao.itest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 不推荐使用，强烈建议使用dom4j原生的代码操作xml文件
 * 
 * @author yufan.yq
 * 
 */
@Deprecated
public class XmlUtils {
	private final static String FONT_FORMAT = "utf-8";
	protected static final Log logger = LogFactory.getLog(XmlUtils.class);

	/**
	 * 获取根节点
	 * 
	 * @param doc
	 * @return
	 */
	public static Element getRootElement(Document doc) {
		return doc.getRootElement();
	}

	/**
	 * 获取文档
	 * 
	 * @param filePath
	 * @return
	 */
	public static Document getDoc(String filePath) {
		if (null == filePath || filePath.trim().equals("")) {
			logger.error("路径为空");
			return null;
		}
		SAXReader saxReader = new SAXReader();
		Document document = null;
		File file = new File(filePath);
		if (file.exists()) {
			try {
				InputStreamReader ir = new InputStreamReader(
						new FileInputStream(file), FONT_FORMAT);
				document = saxReader.read(ir);
				ir.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			logger.error("Can't find the file : " + filePath);
		}
		return document;
	}

	/**
	 * 保存更新
	 * 
	 * @param doc
	 * @param filePath
	 */
	public static void saveDoc(Document doc, String filePath) {
		try {
			FileUtils.writeStringToFile(new File(filePath), doc.asXML(),
					FONT_FORMAT);
		} catch (IOException ex) {
			logger.debug(ex.getMessage());
		}
	}
}
