package com.singeev.bank.tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.singeev.bank.dao.Account;
import com.singeev.bank.dao.AccountsDao;
import java.util.List;


@ActiveProfiles("dev")
@ContextConfiguration(locations = {
		"classpath:service-context.xml",
		"classpath:datasource.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class AccountsDaoTest {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private AccountsDao accountsDao;

	@Before
	public void init() {
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		jdbc.execute("delete from accounts");
	}

	@Test
	public void shouldCreateNewAccount() {
		Account account = new Account("Chuck Norris", "CHUCK-777");
		assertTrue("Account creation should return TRUE:", accountsDao.createAccount(account));
	}

	@Test
	public void shouldReturnAllAccounts() {
		Account account1 = new Account("Chuck Norris", "CHUCK-777");
		Account account2 = new Account("Jet Li", "JET-LI-999");
		accountsDao.createAccount(account1);
		accountsDao.createAccount(account2);
		List<Account> accounts = accountsDao.getAllAccounts();
		assertEquals("Should return 2 accounts.", 2, accounts.size());
	}

	@Test
	public void shouldReturnAccountById() {
		Account account = new Account("Chuck Norris", "CHUCK-777");
		accountsDao.createAccount(account);
		Account retrievedAccount = accountsDao.getAllAccounts().get(0);
		assertTrue("Retrieved account should be indentical to created one!",
				(account.getName().equals(retrievedAccount.getName()) && account.getNumber().equals(retrievedAccount.getNumber()))
						&& account.getBalance() == retrievedAccount.getBalance());
	}

	@Test
	public void shouldUpdateAccount() {
		Account account = new Account("Chuck Norris", "CHUCK-777");
		accountsDao.createAccount(account);
		Account retrievedAccount = accountsDao.getAllAccounts().get(0);
		retrievedAccount.setName("New Name");
		assertTrue("Account update should return TRUE.", accountsDao.updateAccount(retrievedAccount));
		Account updated = accountsDao.getAccount(retrievedAccount.getId());
		assertEquals("Updated account should be indentical to retrieved updated account.", retrievedAccount, updated);
	}

	@Test
	public void shouldDeleteAccount() {
		Account account = new Account("Chuck Norris", "CHUCK-777");
		assertTrue("Account creation should return TRUE:", accountsDao.createAccount(account));
		List<Account> accounts = accountsDao.getAllAccounts();
		accountsDao.deleteAccount(accounts.get(0).getId());
		accounts = accountsDao.getAllAccounts();
		assertEquals("Should return 0 accounts.", 0, accounts.size());
	}
}
