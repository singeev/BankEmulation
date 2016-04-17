package com.singeev.bank.tests;

import static com.jayway.restassured.RestAssured.given;

import com.goebl.david.Webb;
import com.singeev.bank.dao.Account;
import com.singeev.bank.service.AccountsService;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Test // тоже работает, на другой библиотеке (объект создается прямо внутри метода), с ExecutorService не работает
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
    }


    @Test // не работает
    public void isRequestWorking() {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    int fromid = list.get(rand1.nextInt(19)).getId();
                    int toid = list.get(rand2.nextInt(19)).getId();
                    int summ = 1000;
                    given().parameters("fromid", fromid, "toid", toid, "summ", summ)
                            .when().post().then().assertThat().statusCode(302);
                    System.out.println("Transferred " + summ + "$ from id[" + fromid + "] to id[" + toid + "].");
                }
            });
        }
        executorService.shutdown();
    }


    @Test //РАБОТАЕТ: делает 50 успешных запросов ПОСТ за счет цикла
    @Ignore
    public void loadWithoutThreads() {
        for (int i = 0; i < 50; i++) {
            int fromid = list.get(rand1.nextInt(19)).getId();
            int toid = list.get(rand2.nextInt(19)).getId();
            int summ = 1000;
            given().parameters("fromid", fromid, "toid", toid, "summ", summ)
                    .when().post().then().assertThat().statusCode(302);
            System.out.println("Transferred " + summ + "$ from id[" + fromid + "] to id[" + toid + "].");
        }
    }

    @Test // РАБОТАЕТ: но, похоже, что не в параллельных потоках (чтобы его запустить нужно раскоментить @Rule)
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
    }

    @After
    public void checkBalances() {
        int summBalance = 0;
        for (Account account : list) {
            summBalance += account.getBalance();
        }
        System.out.println("Should be 2000000. In fact: " + summBalance);
    }
}
