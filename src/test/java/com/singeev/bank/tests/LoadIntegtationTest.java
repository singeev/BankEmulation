package com.singeev.bank.tests;
import static com.jayway.restassured.RestAssured.given;

import com.singeev.bank.dao.Account;
import com.singeev.bank.service.AccountsService;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import com.jayway.restassured.RestAssured;
import org.databene.contiperf.PerfTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:service-context.xml",
		"classpath:datasource.xml" })
@WebAppConfiguration
public class LoadIntegtationTest {

	// @Rule
	// public ContiPerfRule i = new ContiPerfRule();

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private DataSource dataSource;

	List<Account> list;
	Random rand1 = new Random(47);
	Random rand2 = new Random(13);

	@Before
	public void setUp() throws Exception {
		RestAssured.baseURI = "http://localhost:8080/transfers";
		// RestAssured.baseURI = "http://159.122.77.197:8080/LPBank/transfers";

		// clean DataBase before tests
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		jdbc.execute("delete from accounts");

		// create 20 accounts with identical balance
		for (int i = 0; i < 20; i++) {
			String name = TestUtil.createStringWithLength(7);
			String number = TestUtil.createStringWithLength(7);
			accountsService.createAccount(new Account(name, number));
			Account account = accountsService.getAllAccounts().get(i);
			account.setBalance(100000);
			accountsService.updateBalance(account);
			list = accountsService.getAllAccounts();
		}
		System.out.println("Before test: " + list.size());
	}

	@Test
	public void testAloadTest() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(50);

		for (int i = 0; i < 1000; i++) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					int fromid = list.get(rand1.nextInt(19)).getId();
					int toid = list.get(rand2.nextInt(19)).getId();

					given().parameters("fromid", String.valueOf(fromid), "toid", String.valueOf(toid), "summ", "1").when().post().then().assertThat()
							.statusCode(302);
				}
			});
		}
		executorService.shutdown();
		// executorService.awaitTermination(1, TimeUnit.MINUTES);
	}

	@Test
	@Ignore
	@PerfTest(invocations = 1000, threads = 50)
	public void testBloadTest() throws Exception {
		int fromid = list.get(rand1.nextInt(19)).getId();
		int toid = list.get(rand2.nextInt(19)).getId();

		given().parameters("fromid", String.valueOf(fromid), "toid", String.valueOf(toid), "summ", "1").when().post().then().assertThat()
				.statusCode(302);
	}

	@After
	public void checkBalances() {
		int summBalance = 0;
		for (Account account : list) {
			summBalance += account.getBalance();
		}
		System.out.println("Should be 200000. In fact: " + summBalance);
	}
}
