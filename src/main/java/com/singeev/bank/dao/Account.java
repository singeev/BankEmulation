package com.singeev.bank.dao;

import javax.validation.constraints.Size;

public class Account {
	private int id;

	@Size(min = 4, max = 15, message = "Name should be between 4 and 15 characters!")
	private String name;

	@Size(min = 4, max = 15, message = "Number should be between 4 and 15 characters!")
	private String number;
	private int balance;

	public Account() {

	}

	public Account(String name, String number) {
		this.name = name;
		this.number = number;
	}

	public Account(int id, String name, String number, int balance) {
		this.id = id;
		this.name = name;
		this.number = number;
		this.balance = balance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "Account [id=" + id + ", name=" + name + ", number=" + number + ", balance=" + balance + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Account)) {
			return false;
		}

		Account account = (Account) o;

		if (id != account.id) {
			return false;
		}
		return number.equals(account.number);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + number.hashCode();
		return result;
	}
}
