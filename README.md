# LoyaltyPlantBank
This application is a banking system emulation. </br>
It has a web UI showing info about banking accounts: account's id, owner's name, account number and actual balance.

User can manage accounts:
* create new account;
* edit account's details;
* delete acounts.

Also user can manage funds:</br>
* add funds to account;
* withdraw money from account;
* transfer money from one account to another.

The system prohibits the appearance of a negative balance in the bank accounts and ensures consistency of data at all loads.

All data stores in a MySQL data base. Communications with the data base are via JDBC. Architecture based on Spring MVC, web UI created with JSP.

Application has 24 unit tests to check all functionality and stability. 

Used technologies and frameworks:
- Maven
- Tomcat 7
- Spring Framefork, Spring MVC
- servlets, JSP, JSPF, JSTL
- HTML, CSS, Bootstrap
- MySQL, JDBC
- log4j
- JUnit, Mockito, Hamcrest, RestAssured

######INSTALL AND RUN
To run this app with full funtionality you need to setup and create two instances of MySQL database: test and production.</br>
Test DB should has a name: mybanktest.</br>
Production DB should has a name: mybank.</br>
Also, you need to create one table in each DB. Here's SQL command to do that:
```
CREATE TABLE `accounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `number` varchar(45) NOT NULL,
  `balance` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;
```
After that you should check your DB's URL in DataSource settings in my code, to adapt in to your particular environment.</br>

You need to have runnig instance of a Tomcat web-server to deploy application you will get on the next step.

Finally, you need to assemble WAR file with `mvn clean install` command in your terminal. Before that you need to ensure that tests in [this test class: LoadIntegtationTest.java](https://github.com/singeev/LoyaltyPlantBank/blob/master/src/test/java/com/singeev/bank/tests/LoadIntegtationTest.java) has `@Ignored` annotation! This test loads running web site you haven't run yet. 

**Summarize install and run**:
- two MySQL data bases (mybank and mybanktest) with one table (accounts) in each;
- Tomcat 7;
- `@Ignore` load tests; 
- `mvn clean install` Maven command;
- deploy WAR to Tomcat; 
- enjoy!

