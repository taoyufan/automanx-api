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

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

/**
 * TFS manager xml store 实现的测试类
 *
 * @author leijuan
 */
@SuppressWarnings({"ReuseOfLocalVariable"})
public class TfsManagerXmlStoreImplTest extends TestCase {
    /**
     * tfs manager xml store
     */
    private TfsManagerXmlStoreImpl tfsManagerXmlStore;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tfsManagerXmlStore = new TfsManagerXmlStoreImpl();
        tfsManagerXmlStore.setXmlStoreFiles("/tfs/tfsStore/tfs_demo_files.xml");
    }

    /**
     * 测试获取文件
     *
     * @throws Exception exception
     */
    public void testFetchFile() throws Exception {
        //读取文件内容
        String tfsKey = "1";
        String fileContent = getTextContent(tfsKey, ".xml");
        assertTrue("获取TFS内容失败：key:" + tfsKey, fileContent.contains("log4j"));
        //读取text文本内容
        tfsKey = "2";
        fileContent = getTextContent(tfsKey, ".xml");
        assertTrue("获取TFS内容失败：key:" + tfsKey, fileContent.contains("雷卷"));
    }

    /**
     * test to save file
     *
     * @throws Exception exception
     */
    public void testSaveFile() throws Exception {
        String tfsKey = "3";
        String content = "Hi 雷卷";
        tfsManagerXmlStore.saveFile(content.getBytes(), tfsKey, null);
        String content2 = getTextContent(tfsKey, null);
        assertEquals("Failed to save TFS file", content, content2);
    }

    /**
     * get text content from kfs key
     *
     * @param tfsKey tfs key
     * @param suffix suffix
     * @return text content
     * @throws Exception exception
     */
    private String getTextContent(String tfsKey, String suffix) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        tfsManagerXmlStore.fetchFile(tfsKey, suffix, outputStream);
        outputStream.flush();
        return new String(outputStream.toByteArray());
    }
}
