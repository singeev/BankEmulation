package com.singeev.bank.tests;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;

import com.singeev.bank.dao.Account;
import com.singeev.bank.service.AccountsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


// Test class with Mock instead of real AccountsService
// it's possible to test controllers without connected DataBase

//@formatter:off
@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:datasource.xml",
		"classpath:controllertestsconfig.xml",
		"classpath:dao-context.xml",
		"file:src/main/webapp/WEB-INF/lpbank-servlet.xml"})
@WebAppConfiguration
public class AccountsControllerMockTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService todoServiceMock;

	@Autowired
	private WebApplicationContext context;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void shouldRedirectToAccountsPage() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status()
			.isOk())
			.andExpect(view()
			.name("accounts-list"));
	}

	@Test
	public void shouldReturnAllAccountsList() throws Exception{
		Account account1 = new Account(1, "Chuck Norris", "CHUCK-777", 100);
		Account account2 = new Account(2, "Jet Li", "JET-LI-999", 200);

		when(todoServiceMock.getAllAccounts()).thenReturn(Arrays.asList(account1, account2));

		mockMvc.perform(get("/accounts"))
			.andExpect(status()
			.isOk())
			.andExpect(view().name("accounts-list"))
				.andExpect(forwardedUrl("/WEB-INF/views/accounts-list.jsp"))
			.andExpect(model().attribute("accounts", hasSize(2)))
			.andExpect(model().attribute("accounts", hasItem(
					allOf(
							hasProperty("id", equalTo(1)),
							hasProperty("name", equalTo("Chuck Norris")), 
							hasProperty("number", equalTo("CHUCK-777")),
							hasProperty("balance", equalTo(100)
						)))))
			.andExpect(model().attribute("accounts", hasItem(
					allOf(
							hasProperty("id", equalTo(2)),
							hasProperty("name", equalTo("Jet Li")),
							hasProperty("number", equalTo("JET-LI-999")),
							hasProperty("balance", equalTo(200)
							)))));

		verify(todoServiceMock, atMost(2)).getAllAccounts();
		verifyNoMoreInteractions(todoServiceMock);
	}
}
