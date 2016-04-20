package com.singeev.bank.controllers;

import com.google.common.util.concurrent.Striped;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.singeev.bank.dao.Account;
import com.singeev.bank.dao.Transaction;
import com.singeev.bank.service.AccountsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;


@Controller
public class TransfersController {

    private static Logger logger = Logger.getLogger(TransfersController.class);

    // how many different locks do you want?
    Striped<Lock> lockStriped = Striped.lock(20);

    @Autowired
    private AccountsService service;

    /*
     show page with transaction forms and
     pass there a blank Transaction object
    */
    @RequestMapping(value = "/transfers", method = RequestMethod.GET)
    public String showTransfersPage(Model model) {
        model.addAttribute("transaction", new Transaction());
        return "transfers";
    }

    /*
     method to add money to account
     check if account exists, if summ < 0, if form not blank
     Three times go to DB:
     1. check if account with this ID is exists
     2. get account
     3. update balance (add money)
    */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    @RequestMapping(value = "/addfunds", method = RequestMethod.POST)
    public String addFunds(ModelMap model, Transaction transaction) {

        int addSumm = transaction.getSumm();
        int toid = transaction.getToid();

        List<Lock> locks = (List<Lock>) lockStriped.bulkGet(new ArrayList<>(Arrays.asList(toid)));
        locks.get(0).lock();
        try {

            if (toid == 0 || addSumm == 0) {
                model.addAttribute("errMsg1", "Please, fill in all fields!");
                return "transfers";
            }

            if (addSumm < 0) {
                model.addAttribute("errMsg3", "If you want to withdraw money, please, use special form below! Or input a positive number!");
                return "transfers";
            }

            if (!service.isExists(toid)) {
                model.addAttribute("errMsg2", "There's no account with ID #" + toid + "!");
                return "transfers";
            }

            Account account = service.getAccount(toid);
            int balance = account.getBalance();

            account.setBalance(balance + addSumm);
            service.updateBalance(account);
            model.clear();
            logger.info("Add " + addSumm + "$ to account with id#" + account.getId());
            return "redirect:accounts";
        } finally {
            locks.get(0).unlock();
        }
    }

    /*
     method to withdraw money from account
     check if account exists, if summ < 0,
     if there's enough money on account, if form not blank
     Three times go to DB:
     1. check if account with this ID is exists
     2. get account
     3. update balance (withdraw money)
    */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    @RequestMapping(value = "/withdraw", method = RequestMethod.POST)
    public String withdraw(ModelMap model, Transaction transaction) {

        int withdrawSumm = transaction.getSumm();
        int fromid = transaction.getFromid();

        List<Lock> locks = (List<Lock>) lockStriped.bulkGet(new ArrayList<>(Arrays.asList(fromid)));
        locks.get(0).lock();
        try {

            if (fromid == 0 || withdrawSumm == 0) {
                model.addAttribute("errMsg4", "Please, fill in all fields!");
                return "transfers";
            }

            if (withdrawSumm < 0) {
                model.addAttribute("errMsg6", "Please, input a positive number!");
                return "transfers";
            }

            if (!service.isExists(fromid)) {
                model.addAttribute("errMsg5", "There's no account with ID #" + fromid + "!");
                return "transfers";
            }

            Account account = service.getAccount(fromid);
            int balance = account.getBalance();

            if (balance - withdrawSumm < 0) {
                model.addAttribute("errMsg7", "Sorry, not enough money on that account! There's only " + account.getBalance() + "$");
                return "transfers";
            }
            account.setBalance(balance - withdrawSumm);
            service.updateBalance(account);
            model.clear();
            logger.info("Withdraw " + withdrawSumm + "$ from account with id#" + account.getId());
            return "redirect:accounts";
        } finally {
            locks.get(0).unlock();
        }
    }

    /*
     Method to transfer money form one account to another
     check if accounts exists, if summ < 0,
     if form not blank, if there's enough money on the first account
     Four times go to DB:
     1. check if accounts with this ID is exists (1st)
     2. check if accounts with this ID is exists (2nd)
     5. get 1st account
     6. get 2nd account
     7. update 1st accounts balance (withdraw money)
     8. update 2nd accounts balance (add money)
    */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public String transfer(ModelMap model, Transaction transaction) {

        int transferSumm = transaction.getSumm();
        int fromid = transaction.getFromid();
        int toid = transaction.getToid();

        /*
        Returns the stripes that correspond to the passed objects,
        in ascending order.
        Thus, threads that use the stripes in the order returned by this method
        are guaranteed to not deadlock each other.
         */

        List<Lock> locks = (List<Lock>) lockStriped.bulkGet(new ArrayList<>(Arrays.asList(fromid, toid)));
        locks.get(0).lock();
        try {
            locks.get(1).lock();
            try {

                if (toid == 0 || fromid == 0 || transferSumm == 0) {
                    model.addAttribute("errMsg8", "Please, fill in all fields!");
                    return "transfers";
                }

                if (toid == fromid) {
                    model.addAttribute("errMsg13", "Please, choose two different accounts!");
                    return "transfers";
                }

                if (transferSumm < 0) {
                    model.addAttribute("errMsg11", "Please, input a positive number!");
                    return "transfers";
                }

                if (!service.isExists(fromid)) {
                    model.addAttribute("errMsg9", "There's no account with ID #" + fromid + "!");
                    return "transfers";
                }

                if (!service.isExists(toid)) {
                    model.addAttribute("errMsg10", "There's no account with ID #" + toid + "!");
                    return "transfers";
                }

                Account fromAccount = service.getAccount(fromid);
                Account toAccount = service.getAccount(toid);
                int balanceFrom = fromAccount.getBalance();
                int balanceTo = toAccount.getBalance();

                if (balanceFrom - transferSumm < 0) {
                    model.addAttribute("errMsg12", "Sorry, not enough money on the first account! There's only " + balanceFrom + "$");
                    return "transfers";
                }

                fromAccount.setBalance(balanceFrom - transferSumm);
                service.updateBalance(fromAccount);
                toAccount.setBalance(balanceTo + transferSumm);
                service.updateBalance(toAccount);
                model.clear();
                logger.info("Transfer " + transferSumm + "$ from account with id#" + fromAccount.getId() + " to account with id#" + toAccount.getId());
                return "redirect:accounts";
            } finally {
                locks.get(1).unlock();
            }
        } finally {
            locks.get(0).unlock();
        }
    }
}
