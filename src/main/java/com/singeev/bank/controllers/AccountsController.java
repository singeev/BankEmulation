package com.singeev.bank.controllers;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.singeev.bank.dao.Account;
import com.singeev.bank.service.AccountsService;


@Controller
public class AccountsController {

	private static Logger logger = Logger.getLogger(AccountsController.class);

	@Autowired
	private AccountsService service;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String redirectFromRootPage(Model model) {
		model.addAttribute("accounts", service.getAllAccounts());
		return "accounts-list";
	}

	@RequestMapping(value = "/accounts", method = RequestMethod.GET)
	public String showAccountsPage(Model model) {
		model.addAttribute("accounts", service.getAllAccounts());
		return "accounts-list";
	}

	// show page with blank form to create new account
	@RequestMapping(value = "/account", method = RequestMethod.GET)
	public String showAccount(Model model) {
		model.addAttribute("account", new Account());
		return "newaccount";
	}

	// create new account and show refreshed accounts list
	@RequestMapping(value = "/docreate", method = RequestMethod.POST)
	public String addAccount(ModelMap model, @Valid Account account, BindingResult result) {
		if (result.hasErrors()) {
			return "newaccount";
		}
		service.createAccount(account);
		model.clear();
		logger.info("Created new account: " + account);
		return "redirect:accounts";
	}

	// show account edit page filled with existing account details
	@RequestMapping(value = "/update-account", method = RequestMethod.GET)
	public String showAccountForUpdate(ModelMap model, @RequestParam int id) {
		model.addAttribute("account", service.getAccount(id));
		return "editaccount";
	}

	// update account and show refreshed accounts list
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	@RequestMapping(value = "/update-account", method = RequestMethod.POST)
	public String updateAccount(@Valid Account account, BindingResult result) {
		/*
		 if the account was deleted by other user while our user
		 was filling in the form - he'll be redirecting to refreshed
		 accounts list without an attempt to create new account in the DB.
		 Not very good, maybe it's better to inform user and offer him
		 to create a new account. Do that later.
		*/
		if (result.hasErrors()) {
			return "editaccount";
		}
		if (!service.isExists(account.getId())) {
			return "redirect:accounts";
		}
		service.updateAccount(account);
		logger.info("Account updated: " + account);
		return "redirect:accounts";
	}

	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	@RequestMapping(value = "/delete-account", method = RequestMethod.GET)
	public String deleteAccount(ModelMap model, @RequestParam int id) {
		if (!service.isExists(id)) {
			return "redirect:accounts";
		}
		service.deleteAccount(id);
		logger.info("Deleted account with id #" + id);
		return "redirect:accounts";
	}
}
