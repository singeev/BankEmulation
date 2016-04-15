package com.singeev.bank.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component("accountsDao")
public class AccountsDao {

	private NamedParameterJdbcTemplate jdbc;

	@Autowired
	public void setDataSource(DataSource jdbc) {
		this.jdbc = new NamedParameterJdbcTemplate(jdbc);
	}

	// retrieve list of all existing accounts
	public List<Account> getAllAccounts() {
		return jdbc.query("select * from accounts", BeanPropertyRowMapper.newInstance(Account.class));
	}

	// create new account
	public Boolean createAccount(Account account) {
		BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(account);
		return jdbc.update("insert into accounts (name, number) values (:name, :number)", params) == 1;
	}

	// retrieve account by ID
	public Account getAccount(int id) {
		MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

		return jdbc.queryForObject("select * from accounts where id=:id", params, new RowMapper<Account>() {

			public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
				Account account = new Account();

				account.setId(rs.getInt("id"));
				account.setName(rs.getString("name"));
				account.setNumber(rs.getString("number"));
				account.setBalance(rs.getInt("balance"));

				return account;
			}
		});
	}

	// updates only owner's name and account's number
	public Boolean updateAccount(Account account) {
		BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(account);
		return jdbc.update("update accounts set name=:name, number=:number where id=:id", params) == 1;
	}

	// delete account by ID
	public Boolean deleteAccount(int id) {
		MapSqlParameterSource params = new MapSqlParameterSource("id", id);
		return jdbc.update("delete from accounts where id=:id", params) == 1;
	}

	// updates only accounts balance
	public Boolean updateBalance(Account account) {
		BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(account);
		return jdbc.update("update accounts set balance=:balance where id=:id", params) == 1;
	}

	// check if account with this ID is exists
	public boolean isExists(int id) {
		return jdbc.queryForObject("select count(*) from accounts where id=:id", new MapSqlParameterSource("id", id), Integer.class) > 0;
	}
}
