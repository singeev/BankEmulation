package com.singeev.bank.tests;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import com.goebl.david.Webb;
import com.singeev.bank.dao.Account;
import com.singeev.bank.service.AccountsService;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.jayway.restassured.RestAssured;
import org.databene.contiperf.PerfTest;
import org.junit.*;
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
        "classpath:integrdatasource.xml"})
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
        RestAssured.baseURI = "http://localhost:8080/lpbank-1.0/transfer";
        // RestAssured.baseURI = "http://159.122.77.197:8080/lpbank-1.0/transfer";

        // clean DataBase before tests
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("delete from accounts");
        System.out.println("DataBase cleared!");

        // create 20 accounts with identical balance
        for (int i = 0; i < 20; i++) {
            accountsService.createAccount(new Account("--NameForTest--", "000-TEST-000"));
            Account account = accountsService.getAllAccounts().get(i);
            account.setBalance(100000);
            accountsService.updateBalance(account);
            list = accountsService.getAllAccounts();
        }
        System.out.println("DataBase prepared for test and contains " + list.size() + " accounts.");
    }

    // WORKS: makes 50 threads pool, makes 50 threads, make 1000 POST requests in each thread via for loop
    // waits 2 minutes for termination
    @Test
    public void isRequestWorking() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        int fromid = list.get(rand1.nextInt(19)).getId();
                        int toid = list.get(rand2.nextInt(19)).getId();
                        int summ = 1000;
                        given().parameters("fromid", fromid, "toid", toid, "summ", summ)
                                .when().post().then().assertThat().statusCode(302);
                        System.out.println("Transferred " + summ + "$ from id[" + fromid + "] to id[" + toid + "].");
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.MINUTES);

        System.out.println("Finished with requests, calculating results...");
        // checking results
        int summBalance = 0;
        for (Account account : list) {
            summBalance += account.getBalance();
        }
        assertEquals("Should be 2000000.", 2000000, summBalance);
        System.out.println("All good: money wasn't lost!");
    }

    //WORKS: makes POST requests in for loop without multithreading.
    @Test
    @Ignore
    public void loadWithoutThreads() {
        for (int i = 0; i < 50; i++) {
            int fromid = list.get(rand1.nextInt(19)).getId();
            int toid = list.get(rand2.nextInt(19)).getId();
            int summ = 100;
            given().parameters("fromid", fromid, "toid", toid, "summ", summ)
                    .when().post().then().assertThat().statusCode(302);
            System.out.println("Transferred " + summ + "$ from id[" + fromid + "] to id[" + toid + "].");
        }
        System.out.println("Finished with requests, calculating results...");
        // checking results
        int summBalance = 0;
        for (Account account : list) {
            summBalance += account.getBalance();
        }
        assertEquals("Should be 2000000.", 2000000, summBalance);
        System.out.println("All good: money wasn't lost!");
    }

    // WORKS, but looks like not in parallel threads. To run this test you should uncomment @Rule annotation.
    @Test
    @Ignore
    @PerfTest(threads = 50)
    public void testBloadTest() throws Exception {
        int count = 0;
        for (int i = 0; i < 1000; i++) {
            int fromid = list.get(rand1.nextInt(19)).getId();
            int toid = list.get(rand2.nextInt(19)).getId();
            int summ = 1;
            given().parameters("fromid", fromid, "toid", toid, "summ", summ)
                    .when().post().then().assertThat().statusCode(302);
            System.out.println(count++ + " - Transferred " + summ + "$ from id[" + fromid + "] to id[" + toid + "].");
        }
        System.out.println("Finished with requests, calculating results...");
        // checking results
        int summBalance = 0;
        for (Account account : list) {
            summBalance += account.getBalance();
        }
        assertEquals("Should be 2000000.", 2000000, summBalance);
        System.out.println("All good: money wasn't lost!");
    }

    // WORKS as well. Based on http://hgoebl.github.io/DavidWebb/ library.
    // in this implementation - without multithreading
    @Test
    @Ignore
    public void test() {
        int fromid = list.get(rand1.nextInt(19)).getId();
        int toid = list.get(rand2.nextInt(19)).getId();
        int summ = 1000;
        Webb webb = Webb.create();
        webb.post("http://localhost:8080/lpbank-1.0/transfer")
                .param("fromid", fromid)
                .param("toid", toid)
                .param("summ", summ)
                .asVoid();
        System.out.println("Finished with requests, calculating results...");
        // checking results
        int summBalance = 0;
        for (Account account : list) {
            summBalance += account.getBalance();
        }
        assertEquals("Should be 2000000.", 2000000, summBalance);
        System.out.println("All good: money wasn't lost!");
    }
}
