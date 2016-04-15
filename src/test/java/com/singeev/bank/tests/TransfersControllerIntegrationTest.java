package com.singeev.bank.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
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
import com.singeev.bank.dao.Transaction;
import com.singeev.bank.service.AccountsService;


// Test class with real AccountsService interacting with test MySQL DataBase
//@formatter:off

@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:service-context.xml",
		"classpath:datasource.xml",
		"file:src/main/webapp/WEB-INF/lpbank-servlet.xml" })
@WebAppConfiguration
public class TransfersControllerIntegrationTest {

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

	/*----------------------------------------
			     ADD FUNDS TESTS
	----------------------------------------*/
	
	// test adding funds to account: 
	// should add money to account by ID, write changes to DataBase, show refreshed accounts list page
	@Test
	public void shouldAddFundsToAccountById() throws Exception{
		accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
		Account account = accountsService.getAllAccounts().get(0);
		assertEquals("Balance should be zero!", 0, account.getBalance());
		
		mockMvc.perform(post("/addfunds")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("fromid", "0")
                .param("toid", String.valueOf(account.getId()))
                .param("summ", "500")
                .sessionAttr("transaction", new Transaction()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:accounts"))
                .andExpect(redirectedUrl("accounts"));
		
		Account richAccount = accountsService.getAccount(account.getId());
		assertEquals("New balance should be 500!", 500, richAccount.getBalance());
		
	}
	
	// test adding funds with validation (wrong ID) fails:
	// should not add anything to DataBase, stay on the same page, keep entered data
	@Test
	public void shouldNotAddFundsIfValidationFail() throws Exception{
		accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
		accountsService.createAccount(new Account("Jet Li", "JET-LI-999"));
		
		Account account1 = accountsService.getAllAccounts().get(0);
		Account account2 = accountsService.getAllAccounts().get(1);
		
		// to be sure ID doesn't exist in the DataBase
		int id = Math.max(account1.getId(), account2.getId()) + 1;
		
		mockMvc.perform(post("/addfunds")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("fromid", "0")
                .param("toid", String.valueOf(id))
                .param("summ", "500")
                .sessionAttr("transaction", new Transaction()))
        		.andExpect(status().isOk())
        		.andExpect(view().name("transfers"))
        		.andExpect(forwardedUrl("/WEB-INF/views/transfers.jsp"))
        		.andExpect(model().attribute("transaction", hasProperty("fromid", equalTo(0))))
        		.andExpect(model().attribute("transaction", hasProperty("toid", equalTo(id))))
        		.andExpect(model().attribute("transaction", hasProperty("summ", equalTo(500))));
		
		Account account3 = accountsService.getAllAccounts().get(0);
		Account account4 = accountsService.getAllAccounts().get(1);
		
		assertEquals("Balance should be 0!", 0,account3.getBalance());
		assertEquals("Balance should be 0!", 0,account4.getBalance());
	}
	
	
	/*----------------------------------------
                 WITHDRAW TESTS
    ----------------------------------------*/
	
	// test withdraw money from account: 
	// should withdraw money from account by ID, write changes to DataBase, show refreshed accounts list page
	@Test
	public void shouldWithdrawMoneyFromAccountById() throws Exception{
		accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
		Account account = accountsService.getAllAccounts().get(0);
		account.setBalance(1000);
		accountsService.updateBalance(account);
		Account accountFromDb = accountsService.getAllAccounts().get(0);
		assertEquals("Balance should be 1000!", 1000, accountFromDb.getBalance());
			
		mockMvc.perform(post("/withdraw")
	            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	            .param("fromid", String.valueOf(account.getId()))
	            .param("toid", "0")
	            .param("summ", "500")
	            .sessionAttr("transaction", new Transaction()))
	            .andExpect(status().is3xxRedirection())
	            .andExpect(view().name("redirect:accounts"))
	            .andExpect(redirectedUrl("accounts"));
			
		Account poorAccount = accountsService.getAccount(accountFromDb.getId());
		assertEquals("New balance should be 500!", 500, poorAccount.getBalance());
	}
	
	
	// test withdraw money when not enough money on balance: 
	// should not withdraw money from account, write nothing to DataBase, show same page, keep the data
	@Test
	public void shouldNotWithdrawIfNotEnoughMoney() throws Exception{
		accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
		Account account = accountsService.getAllAccounts().get(0);
		account.setBalance(1000);			
		accountsService.updateBalance(account);
		Account accountFromDb = accountsService.getAllAccounts().get(0);
		assertEquals("Balance should be 1000!", 1000, accountFromDb.getBalance());
				
		mockMvc.perform(post("/withdraw")
	            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
		        .param("fromid", String.valueOf(account.getId()))
		        .param("toid", "0")
		        .param("summ", "1500")
		        .sessionAttr("transaction", new Transaction()))
				.andExpect(status().isOk())
				.andExpect(view().name("transfers"))
				.andExpect(forwardedUrl("/WEB-INF/views/transfers.jsp"))
				.andExpect(model().attribute("transaction", hasProperty("fromid", equalTo(account.getId()))))
				.andExpect(model().attribute("transaction", hasProperty("toid", equalTo(0))))
				.andExpect(model().attribute("transaction", hasProperty("summ", equalTo(1500))));
				
		Account poorAccount = accountsService.getAccount(accountFromDb.getId());
		assertEquals("Balance should be 1000!", 1000, poorAccount.getBalance());
	}	
	
	// test withdraw money when input wrong ID: 
	// should not withdraw money from account, write nothing to DataBase, show same page, keep the data
		@Test
		public void shouldNotWithdrawIfIdIsWrong() throws Exception{
			accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
			Account account = accountsService.getAllAccounts().get(0);
			account.setBalance(1000);
			accountsService.updateBalance(account);
			// to be sure ID doesn't exist in the DataBase
			int id = account.getId() + 1;
			
			mockMvc.perform(post("/withdraw")
	                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	                .param("fromid", String.valueOf(id))
	                .param("toid", "0")
	                .param("summ", "500")
	                .sessionAttr("transaction", new Transaction()))
	        		.andExpect(status().isOk())
	        		.andExpect(view().name("transfers"))
	        		.andExpect(forwardedUrl("/WEB-INF/views/transfers.jsp"))
	        		.andExpect(model().attribute("transaction", hasProperty("fromid", equalTo(id))))
	        		.andExpect(model().attribute("transaction", hasProperty("toid", equalTo(0))))
	        		.andExpect(model().attribute("transaction", hasProperty("summ", equalTo(500))));
			
			Account accountFromDb = accountsService.getAllAccounts().get(0);
			assertEquals("Balance should be 1000!", 1000, accountFromDb.getBalance());
		}	
	
	
	/*----------------------------------------
                TRANSFERS TESTS
    ----------------------------------------*/
	
		// test money transfer between two accounts:
		// should take money from one account, put them to another, write changes to DataBase, show refreshed accounts list
		@Test
		public void shouldTransferMoneyBetweenAccounts() throws Exception{
			accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
			accountsService.createAccount(new Account("Jet Li", "JET-LI-999"));
			
			Account account1 = accountsService.getAllAccounts().get(0);
			Account account2 = accountsService.getAllAccounts().get(1);
			
			account1.setBalance(1000);
			accountsService.updateBalance(account1);
			
			mockMvc.perform(post("/transfer")
		            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
		            .param("fromid", String.valueOf(account1.getId()))
		            .param("toid", String.valueOf(account2.getId()))
		            .param("summ", "500")
		            .sessionAttr("transaction", new Transaction()))
		            .andExpect(status().is3xxRedirection())
		            .andExpect(view().name("redirect:accounts"))
		            .andExpect(redirectedUrl("accounts"));
			
			Account account3 = accountsService.getAllAccounts().get(0);
			Account account4 = accountsService.getAllAccounts().get(1);
			assertEquals("Balance should be 500!", 500, account3.getBalance());
			assertEquals("Balance should be 500!", 500, account4.getBalance());
		}
		
		// test transfer money when not enough money on first account's balance: 
		// should not withdraw money from 1st account, not add money to the 2nd one, 
		// should write nothing to DataBase, show same page, keep the data
		@Test
		public void shouldNotTransferIfNotEnoughMoney() throws Exception{
			accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
			accountsService.createAccount(new Account("Jet Li", "JET-LI-999"));
			
			Account account1 = accountsService.getAllAccounts().get(0);
			Account account2 = accountsService.getAllAccounts().get(1);
			
			account1.setBalance(1000);
			accountsService.updateBalance(account1);
					
			mockMvc.perform(post("/transfer")
		            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
			        .param("fromid", String.valueOf(account1.getId()))
			        .param("toid", String.valueOf(account2.getId()))
			        .param("summ", "1500")
			        .sessionAttr("transaction", new Transaction()))
					.andExpect(status().isOk())
					.andExpect(view().name("transfers"))
					.andExpect(forwardedUrl("/WEB-INF/views/transfers.jsp"))
					.andExpect(model().attribute("transaction", hasProperty("fromid", equalTo(account1.getId()))))
					.andExpect(model().attribute("transaction", hasProperty("toid", equalTo(account2.getId()))))
					.andExpect(model().attribute("transaction", hasProperty("summ", equalTo(1500))));
					
			Account account3 = accountsService.getAllAccounts().get(0);
			Account account4 = accountsService.getAllAccounts().get(1);
			assertEquals("Balance should be 1000!", 1000, account3.getBalance());
			assertEquals("Balance should be 0!", 0, account4.getBalance());
			
		}
		
		//test for transfer attempt with using wrong ID's of accounts:
		// should transfer nothing, write nothing to DataBase, stay on the same page, keep data
		@Test
		public void shouldNotTransferIfUsedWrongId() throws Exception{
			accountsService.createAccount(new Account("Chuck Norris", "CHUCK-777"));
			accountsService.createAccount(new Account("Jet Li", "JET-LI-999"));
			
			Account account1 = accountsService.getAllAccounts().get(0);
			Account account2 = accountsService.getAllAccounts().get(1);
			
			account1.setBalance(1000);
			account2.setBalance(1000);
			accountsService.updateBalance(account1);
			accountsService.updateBalance(account2);
			
			// to be sure ID doesn't exist in the DataBase
			int id = Math.max(account1.getId(), account2.getId()) + 1;
			
			//request with wrong 1st ID
			mockMvc.perform(post("/transfer")
		            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
			        .param("fromid", String.valueOf(id))
			        .param("toid", String.valueOf(account2.getId()))
			        .param("summ", "500")
			        .sessionAttr("transaction", new Transaction()))
					.andExpect(status().isOk())
					.andExpect(view().name("transfers"))
					.andExpect(forwardedUrl("/WEB-INF/views/transfers.jsp"))
					.andExpect(model().attribute("transaction", hasProperty("fromid", equalTo(id))))
					.andExpect(model().attribute("transaction", hasProperty("toid", equalTo(account2.getId()))))
					.andExpect(model().attribute("transaction", hasProperty("summ", equalTo(500))));
					
			Account account3 = accountsService.getAllAccounts().get(0);
			Account account4 = accountsService.getAllAccounts().get(1);
			assertEquals("Balance should be 1000!", 1000, account3.getBalance());
			assertEquals("Balance should be 1000!", 1000, account4.getBalance());
			
			//request with wrong 2nd ID
			mockMvc.perform(post("/transfer")
		            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
			        .param("fromid", String.valueOf(account1.getId()))
			        .param("toid", String.valueOf(id))
			        .param("summ", "500")
			        .sessionAttr("transaction", new Transaction()))
					.andExpect(status().isOk())
					.andExpect(view().name("transfers"))
					.andExpect(forwardedUrl("/WEB-INF/views/transfers.jsp"))
					.andExpect(model().attribute("transaction", hasProperty("fromid", equalTo(account1.getId()))))
					.andExpect(model().attribute("transaction", hasProperty("toid", equalTo(id))))
					.andExpect(model().attribute("transaction", hasProperty("summ", equalTo(500))));
			
			account3 = accountsService.getAllAccounts().get(0);
			account4 = accountsService.getAllAccounts().get(1);
			assertEquals("Balance should be 1000!", 1000, account3.getBalance());
			assertEquals("Balance should be 1000!", 1000, account4.getBalance());
		}
}
