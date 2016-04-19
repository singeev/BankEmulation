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
import java.util.concurrent.atomic.AtomicInteger;

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

    /*
     @Rule
     public ContiPerfRule i = new ContiPerfRule();
    */

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private DataSource dataSource;

    List<Account> list;
    Random rand1 = new Random(47);
    Random rand2 = new Random(13);
    private final AtomicInteger tempSummBalance = new AtomicInteger(0);

    @Before
    public void setUp() throws Exception {
        RestAssured.baseURI = "http://localhost:8080/transfer";
        // RestAssured.baseURI = "http://localhost:8080/lpbank-1.0/transfer";
        // RestAssured.baseURI = "http://159.122.77.197:8080/lpbank-1.0/transfer";

        // clean DataBase before tests
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("delete from accounts");
        System.out.println("DataBase cleared!");

        // create 20 accounts with identical balance
        for (int i = 0; i < 1000; i++) {
            accountsService.createAccount(new Account("--NameForTest--", "000-TEST-000"));
            Account account = accountsService.getAllAccounts().get(i);
            account.setBalance(100000);
            accountsService.updateBalance(account);
            list = accountsService.getAllAccounts();
        }
        System.out.println("DataBase prepared for test and contains " + list.size() + " accounts.");
        System.out.println("Summ balance before tests: " + getSummBalance() + "$");
    }

    // WORKS: makes 50 threads pool, waits 10 minutes for termination
    @Test
    public void loadTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 100; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10; j++) {
                        int fromid = list.get(rand1.nextInt(19)).getId();
                        int toid;
                        do {
                            toid = list.get(rand2.nextInt(19)).getId();
                        } while (fromid == toid); //to not transfer money from one to the same account!
                        int summ = 1000;
                        given().parameters("fromid", fromid, "toid", toid, "summ", summ)
                                .when().post().then().assertThat().statusCode(302);
                        System.out.println("Transferred " + summ + "$ from id[" + fromid + "] to id[" + toid + "].");
                    }
                    // to see in terminal when one thread finishes his requests
                    System.out.println("This thread has finished it's job! Waiting for others...");
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        System.out.println("Finished with requests, calculating results...");
        // checking results
        int summBalance = getSummBalance();
        assertEquals("Should be 2000000.", 2000000, summBalance);
        System.out.println("All good: money wasn't lost!");
    }

    @Test
    public void loadTestWithDifferentTypesOfRequests() throws InterruptedException {
        tempSummBalance.addAndGet(getSummBalance());
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 100; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Random rand = new Random(13);
                    int operationType;
                    int fromid = list.get(rand1.nextInt(19)).getId();
                    int toid;
                    do {
                        toid = list.get(rand2.nextInt(19)).getId();
                    } while (fromid == toid); //to not transfer money from one to the same account!
                    int summ = 10;

                    for (int j = 0; j < 10; j++) {
                        operationType = rand.nextInt(4);
                        switch (operationType) {
                            case 1: // transfer
                                RestAssured.baseURI = "http://localhost:8080/transfer";
                                given().parameters("fromid", fromid, "toid", toid, "summ", summ)
                                        .when().post().then().assertThat().statusCode(302);
                                System.out.println("Transferred " + summ + "$ from id[" + fromid + "] to id[" + toid + "].");
                                break;
                            case 2: // add
                                RestAssured.baseURI = "http://localhost:8080/addfunds";
                                given().parameters("fromid", 0, "toid", toid, "summ", summ)
                                        .when().post().then().assertThat().statusCode(302);
                                tempSummBalance.addAndGet(summ);
                                System.out.println("Added " + summ + "$ to id[" + toid + "].");
                                break;

                            case 3: // withdraw
                                RestAssured.baseURI = "http://localhost:8080/withdraw";
                                given().parameters("fromid", fromid, "toid", 0, "summ", summ)
                                        .when().post().then().assertThat().statusCode(302);
                                tempSummBalance.addAndGet(-summ);
                                System.out.println("Withdrawed " + summ + "$ from id[" + fromid + "].");
                                break;
                        }
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        System.out.println("Finished with requests, calculating results...");
        // checking results
        int summBalance = getSummBalance();
        assertEquals("Balances should be match.", tempSummBalance.get(), summBalance);
        System.out.println("All good: money wasn't lost!");

    }

    //WORKS: makes POST requests in for loop without multithreading.
    @Test
    @Ignore
    public void loadWithoutThreads() {
        for (int i = 0; i < 100; i++) {
            int fromid = list.get(rand1.nextInt(19)).getId();
            int toid;
            do {
                toid = list.get(rand2.nextInt(19)).getId();
            } while (fromid == toid); //to not transfer money from one to the same account!
            int summ = 100;
            given().parameters("fromid", fromid, "toid", toid, "summ", summ)
                    .when().post().then().assertThat().statusCode(302);
            System.out.println("Transferred " + summ + "$ from id[" + fromid + "] to id[" + toid + "].");
        }
        System.out.println("Finished with requests, calculating results...");
        // checking results
        int summBalance = getSummBalance();
        assertEquals("Should be 2000000.", 2000000, summBalance);
        System.out.println("All good: money wasn't lost!");
    }

    // WORKS, but looks like not in parallel threads. To run this test you should uncomment @Rule annotation.
    @Test
    @Ignore
    @PerfTest(threads = 50)
    public void loadTestWithPerfTestLib() throws Exception {
        int count = 0;
        for (int i = 0; i < 1000; i++) {
            int fromid = list.get(rand1.nextInt(19)).getId();
            int toid;
            do {
                toid = list.get(rand2.nextInt(19)).getId();
            } while (fromid == toid); //to not transfer money from one to the same account!
            int summ = 1;
            given().parameters("fromid", fromid, "toid", toid, "summ", summ)
                    .when().post().then().assertThat().statusCode(302);
            System.out.println(count++ + " - Transferred " + summ + "$ from id[" + fromid + "] to id[" + toid + "].");
        }
        System.out.println("Finished with requests, calculating results...");
        // checking results
        int summBalance = getSummBalance();
        assertEquals("Should be 2000000.", 2000000, summBalance);
        System.out.println("All good: money wasn't lost!");
    }

    /*
     WORKS as well. Based on http://hgoebl.github.io/DavidWebb/ library.
     in this implementation - without multithreading
    */
    @Test
    @Ignore
    public void loadTestWithDavidWebbLib() {
        int fromid = list.get(rand1.nextInt(19)).getId();
        int toid;
        do {
            toid = list.get(rand2.nextInt(19)).getId();
        } while (fromid == toid); //to not transfer money from one to the same account!
        int summ = 1000;
        Webb webb = Webb.create();
        webb.post("http://localhost:8080/lpbank-1.0/transfer")
                .param("fromid", fromid)
                .param("toid", toid)
                .param("summ", summ)
                .asVoid();
        System.out.println("Finished with requests, calculating results...");
        // checking results
        int summBalance = getSummBalance();
        assertEquals("Should be 2000000.", 2000000, summBalance);
        System.out.println("All good: money wasn't lost!");
    }

    // method to get summarize accounts balance
    private int getSummBalance() {
        int summBalance = 0;
        list = accountsService.getAllAccounts();
        for (Account account : list) {
            summBalance += account.getBalance();
        }
        return summBalance;
    }
}
