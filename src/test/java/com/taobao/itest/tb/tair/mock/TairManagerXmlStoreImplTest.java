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

import javax.annotation.Resource;

import org.junit.Test;

import com.taobao.common.tair.DataEntry;
import com.taobao.common.tair.Result;
import com.taobao.common.tair.TairManager;
import com.taobao.itest.ITestSpringContextBaseCase;
import com.taobao.itest.annotation.ITestSpringContext;

import static org.junit.Assert.*;


/**
 * tair manager xml store test case
 *
 * @author leijuan
 */
@ITestSpringContext({"/tair/applicationContext.xml","/tair/applicationContext-test.xml"})
public class TairManagerXmlStoreImplTest extends ITestSpringContextBaseCase {
    /**
     * tair manager xml store
     */
	@Resource
    private TairManager tairManager;
    /**
     * name space id
     */
    private int namespace = 1;

   

    /**
     * test to get value form xml file
     */
    @Test
    public void testGetValueFromXml() {
        String key = "name";
        Result<DataEntry> dataEntryResult = tairManager.get(namespace, key);
        String value = (String) dataEntryResult.getValue().getValue();
        assertNotNull("Failed to get value from xml: key:" + key, value);
    }

    /**
     * test to get value form groovy code
     */
    @SuppressWarnings({"CastToConcreteClass"})
    @Test
    public void testGetValueFromGroovy() {
        String key = "groovy";
        Result<DataEntry> dataEntryResult = tairManager.get(namespace, key);
        UserDO user = (UserDO) dataEntryResult.getValue().getValue();
        assertNotNull("Failed to get value from groovy: key:" + key, user);
    }

    /**
     * test to put value
     *
     * @throws Exception exception
     */
    @Test
    public void testPut() throws Exception {
        String key = "name";
        String value = "leijuan";
        tairManager.put(namespace, key, value);
        Result<DataEntry> dataEntryResult = tairManager.get(namespace, key);
        String value2 = (String) dataEntryResult.getValue().getValue();
        assertEquals("Failed to cache value", value, value2);
    }
}
