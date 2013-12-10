package com.taobao.itest.transaction;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.taobao.itest.ITestSpringContextBaseCase;
import com.taobao.itest.annotation.ITestSpringContext;
import com.taobao.itest.core.TestListeners;
import com.taobao.itest.listener.TransactionalListener;

@TestListeners({ TransactionalListener.class })
@TransactionConfiguration(defaultRollback = false)
@Transactional
@ITestSpringContext({ "/transaction/applicationContext-test.xml" })
public class TransactionalListenerTestSample extends ITestSpringContextBaseCase {
	@Resource
	protected static SimpleJdbcTemplate jdbcTemplate1;
	@Resource
	protected static SimpleJdbcTemplate jdbcTemplate2;

	@BeforeTransaction
	public void verifyInitialDatabaseState() {
		// logic to verify the initial state before a transaction is started
	}

	@Before
	public void setUpTestDataWithinTransaction() {
		// set up test data within the transaction
	}

	@Test
	@Rollback
	// @ITestDataSet(dsNames = { "db1" }, teardownOperation = "NONE")
	public void test01() {
		List<?> a = jdbcTemplate1
				.queryForList("select * from user where id = '4'");
		assertThat(a, notNullValue());
		// assertThat(a.size(), is(1));
		int c = jdbcTemplate1.update("delete from user where id = ?", 4);
		a = jdbcTemplate1.queryForList("select * from user where id = '4'");
		System.out.println("done:" + c);
	}

	@Test
	// overrides the class-level defaultRollback setting
	@Rollback
	public void modifyDatabaseWithinTransaction() {
		// logic which uses the test data and modifies database state
	}

	@After
	public void tearDownWithinTransaction() {
		// execute "tear down" logic within the transaction
	}

	@AfterTransaction
	public void verifyFinalDatabaseState() {
		// logic to verify the final state after transaction has rolled back
	}

	@Test
	@NotTransactional
	public void performNonDatabaseRelatedAction() {
		// logic which does not modify database state
	}
}