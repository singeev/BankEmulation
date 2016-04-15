package com.singeev.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.singeev.bank.dao.Account;
import com.singeev.bank.dao.AccountsDao;
import java.util.List;

@Service("accountsService")
public class AccountsService {

	@Autowired
	private AccountsDao accountsDao;

	public List<Account> getAllAccounts() {
		return accountsDao.getAllAccounts();
	}

	public boolean createAccount(Account account) {
		return accountsDao.createAccount(account);
	};

	public Account getAccount(int id) {
		return accountsDao.getAccount(id);
	};

	public Boolean updateAccount(Account account) {
		return accountsDao.updateAccount(account);
	};

	public Boolean deleteAccount(int id) {
		return accountsDao.deleteAccount(id);
	}

	public Boolean updateBalance(Account account) {
		return accountsDao.updateBalance(account);
	}

	public Boolean isExists(int id) {
		return accountsDao.isExists(id);
	}
}
