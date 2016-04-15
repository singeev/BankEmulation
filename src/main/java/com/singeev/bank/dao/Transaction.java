package com.singeev.bank.dao;

public class Transaction {

	private int fromid;
	private int toid;
	private int summ;

	public Transaction() {

	}

	public Transaction(int fromId, int toId, int summ) {
		this.fromid = fromId;
		this.toid = toId;
		this.summ = summ;
	}

	public int getFromid() {
		return fromid;
	}

	public void setFromid(int fromId) {
		this.fromid = fromId;
	}

	public int getToid() {
		return toid;
	}

	public void setToid(int toId) {
		this.toid = toId;
	}

	public int getSumm() {
		return summ;
	}

	public void setSumm(int summ) {
		this.summ = summ;
	}

	@Override
	public String toString() {
		return "Transaction [fromId=" + fromid + ", toId=" + toid + ", summ=" + summ + "]";
	}
}
