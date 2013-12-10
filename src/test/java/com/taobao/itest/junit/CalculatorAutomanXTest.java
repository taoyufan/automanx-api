package com.taobao.itest.junit;

import org.junit.runner.RunWith;

import com.taobao.itest.core.ItestDataDriverRunner;
import com.taobao.itest.core.TestListeners;
import com.taobao.itest.listener.ITestDataSetBeforeListener;
import com.taobao.itest.listener.ITestDataSetListener;
import com.taobao.itest.listener.ITestHsfStarterListener;
import com.taobao.itest.listener.ITestResourceListener;
import com.taobao.itest.listener.ITestSpringContextListener;
import com.taobao.itest.listener.ITestSpringInjectionListener;
import com.taobao.itest.listener.ItestDataPrepareListener;

@RunWith(ItestDataDriverRunner.class)
@TestListeners({ ITestHsfStarterListener.class, ITestSpringContextListener.class, ITestSpringInjectionListener.class, ITestResourceListener.class, ITestDataSetBeforeListener.class, ITestDataSetListener.class, ItestDataPrepareListener.class })
public class CalculatorAutomanXTest {

}
