package com.singeev.bank.tests;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.singeev.bank.dao.Account;
import com.singeev.bank.service.AccountsService;


// Test class with real AccountsService interacting with test MySQL DataBase

@ActiveProfiles("dev")
@ContextConfiguration(locations = {
		"classpath:service-context.xml",
		"classpath:datasource.xml",
		"file:src/main/webapp/WEB-INF/lpbank-servlet.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AccountsControllerIntegrationTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;
	
	@Autowired
	private DataSource dataSource;

	@Autowired
	private WebApplicationContext context;

	// clean test DB before each test
	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		jdbc.execute("delete from accounts");
	}

	// test redirection to accounts list (main page)
	@Test
	public void shouldRedirectToAccountsPage() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status()
			.isOk())
			.andExpect(view()
			.name("accounts-list"));
	}

	// test if main page shows proper accounts list from MySql DB
	@Test
	public void shouldReturnAllAccountsListFromDataBaseToView() throws Exception{
		accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
		accountsService.createAccount(new Account("Jet Li", "JET-LI-999"));
		Account account1 = accountsService.getAllAccounts().get(0);
		Account account2 = accountsService.getAllAccounts().get(1);

		mockMvc.perform(get("/accounts"))
			.andExpect(status()
			.isOk())
			.andExpect(view().name("accounts-list"))
			.andExpect(forwardedUrl("/WEB-INF/views/accounts-list.jsp"))
			.andExpect(model().attribute("accounts", hasSize(2)))
			.andExpect(model().attribute("accounts", hasItem(
					allOf(
							hasProperty("id", equalTo(account1.getId())),
							hasProperty("name", equalTo(account1.getName())), 
							hasProperty("number", equalTo(account1.getNumber())),
							hasProperty("balance", equalTo(account1.getBalance())
						)))))
			.andExpect(model().attribute("accounts", hasItem(
					allOf(
							hasProperty("id", equalTo(account2.getId())),
							hasProperty("name", equalTo(account2.getName())), 
							hasProperty("number", equalTo(account2.getNumber())),
							hasProperty("balance", equalTo(account2.getBalance())
						)))));
	}
	
	// test for new account creation:
	// should write new account to DB and show refreshed accounts list page
	@Test
	public void shouldCreateNewAccountAndWriteItToDb() throws Exception{
		 mockMvc.perform(post("/docreate")
	                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	                .param("name", "Chuck Norris")
	                .param("number", "CHUCK-777")
	                .sessionAttr("account", new Account()))
	                .andExpect(status().is3xxRedirection())
	                .andExpect(view().name("redirect:accounts"))
	                .andExpect(redirectedUrl("accounts"));
		
		Account retrievedAccount = accountsService.getAllAccounts().get(0);
		assertTrue("Retrieved account should be indentical to created one!",
				(retrievedAccount.getName().equals("Chuck Norris") && retrievedAccount.getNumber().equals("CHUCK-777")));
	}
	
	// test for the case when validation fails while attempt to create new account
	// should stay on the same page, keep data, show errors and should not write anything to DataBase 
	@Test
	public void shouldNotCreateNewAccountWithValidationFail() throws Exception{
		//name and number should be between 4 and 15 characters
		String name = TestUtil.createStringWithLength(16);
        String number = TestUtil.createStringWithLength(16);
        
        mockMvc.perform(post("/docreate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", name)
                .param("number", number)
                .sessionAttr("todo", new Account()))
                .andExpect(status().isOk())
                .andExpect(view().name("newaccount"))
                .andExpect(forwardedUrl("/WEB-INF/views/newaccount.jsp"))
                .andExpect(model().attributeHasFieldErrors("account", "name"))
                .andExpect(model().attributeHasFieldErrors("account", "number"))
                .andExpect(model().attribute("account", hasProperty("id", equalTo(0))))
                .andExpect(model().attribute("account", hasProperty("name", equalTo(name))))
                .andExpect(model().attribute("account", hasProperty("number", equalTo(number))))
                .andExpect(model().attribute("account", hasProperty("balance", equalTo(0))));
        assertEquals("DataBase should be empty.", 0, accountsService.getAllAccounts().size());
	}
	
	// test if show-account-for-edit works properly: 
	// should show edit page and pass there an account object from DataBase by ID
	@Test
	public void shouldReturnOneAccountForEditById() throws Exception{
		accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
		Account account1 = accountsService.getAllAccounts().get(0);
		String url = "/update-account?id=" + account1.getId();
		
		mockMvc.perform(get(url))
		.andExpect(status().isOk())
		.andExpect(view().name("editaccount"))
		.andExpect(forwardedUrl("/WEB-INF/views/editaccount.jsp"))
		.andExpect(model().attribute("account", hasProperty("id", equalTo(account1.getId()))))
		.andExpect(model().attribute("account", hasProperty("name", equalTo(account1.getName()))))
		.andExpect(model().attribute("account", hasProperty("number", equalTo(account1.getNumber()))))
		.andExpect(model().attribute("account", hasProperty("balance", equalTo(account1.getBalance()))));
	}
	
	// test account update functional:
	// should update account's fields, write it to DataBase, redirect to refreshed accounts list page
	@Test
	public void shouldUpdateAccountById() throws Exception{
		accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
		Account account = accountsService.getAllAccounts().get(0);
		
		mockMvc.perform(post("/update-account")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "xxxx")
                .param("number", "0000")
                .param("Id", String.valueOf(account.getId()))
                .sessionAttr("account", account))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:accounts"))
                .andExpect(redirectedUrl("accounts"));
		
		Account updatedAccount = accountsService.getAccount(account.getId());
		
		assertEquals("Should be only one account in DB", 1, accountsService.getAllAccounts().size());
		assertTrue("Updated account should have updated fields.", 
				updatedAccount.getName().equals("xxxx") && 
				updatedAccount.getNumber().equals("0000"));
	}
	
	// test update-account-validation fail:
	// should not change anything in DataBase, should stay on the same page, show errors and keep entered data
	@Test
	public void shouldNotUpdateAccountWithValidationFail() throws Exception{
		accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
		Account account = accountsService.getAllAccounts().get(0);
		//name and number should be between 4 and 15 characters
		String name = TestUtil.createStringWithLength(16);
        String number = TestUtil.createStringWithLength(16);
        
        mockMvc.perform(post("/update-account")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", name)
                .param("number", number)
                .param("Id", String.valueOf(account.getId()))
                .sessionAttr("account", account))
        		.andExpect(status().isOk())
        		.andExpect(view().name("editaccount"))
        		.andExpect(forwardedUrl("/WEB-INF/views/editaccount.jsp"))
        		.andExpect(model().attributeHasFieldErrors("account", "name"))
        		.andExpect(model().attributeHasFieldErrors("account", "number"))
        		.andExpect(model().attribute("account", hasProperty("id", equalTo(account.getId()))))
        		.andExpect(model().attribute("account", hasProperty("name", equalTo(name))))
        		.andExpect(model().attribute("account", hasProperty("number", equalTo(number))))
        		.andExpect(model().attribute("account", hasProperty("balance", equalTo(account.getBalance()))));
        
        Account notUpdated = accountsService.getAccount(account.getId());
        
        assertEquals("DataBase should contain only 1 account.", 1, accountsService.getAllAccounts().size());
        assertEquals("Account should be in the same state as before update attepmt.", account, notUpdated);
	}
	
	// test deleting account:
	// should delete account by ID in DataBase, redirect to refreshed accounts list page
	@Test
	public void shouldDeleteAccountById() throws Exception{
		accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
		accountsService.createAccount(new Account("Jet Li", "JET-LI-999"));
		assertEquals("Should be 2 accounts in the DataBase", 2, accountsService.getAllAccounts().size());
		Account account = accountsService.getAllAccounts().get(0);
		String url = "/delete-account?id=" + account.getId();
		
		mockMvc.perform(get(url))
		.andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:accounts"))
        .andExpect(redirectedUrl("accounts"));
		
		assertTrue("There shouldn't be an account with this ID in DataBase.", !accountsService.isExists(account.getId()));
		assertEquals("Should be only 1 account in the DataBase", 1, accountsService.getAllAccounts().size());
	}
}
