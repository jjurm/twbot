package com.jjurm.twbot.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import snaq.db.ConnectionPool;

/**
 * Connection pool class that extends {@link ConnectionPool} and every returned
 * {@code Connection} has auto-commit disabled.
 * 
 * @author JJurM
 */
public class NoAutoCommitConnectionPool extends ConnectionPool {

	/**
	 * Creates a new {@code NoAutoCommitConnectionPool} instance.
	 * 
	 * @param name pool name
	 * @param minPool minimum number of pooled connections, or 0 for none
	 * @param maxPool maximum number of pooled connections, or 0 for none
	 * @param maxSize maximum number of possible connections, or 0 for no limit
	 * @param idleTimeout idle timeout (seconds) for idle pooled connections, or
	 *            0 for no timeout
	 * @param url JDBC connection URL
	 * @param props connection properties
	 */
	public NoAutoCommitConnectionPool(String name, int minPool, int maxPool, int maxSize,
			long idleTimeout, String url, Properties props) {
		super(name, minPool, maxPool, maxSize, idleTimeout, url, props);
	}

	private void disableAutoCommit(Connection con) throws SQLException {
		con.setAutoCommit(false);
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection con = super.getConnection();
		disableAutoCommit(con);
		return con;
	}

	@Override
	public Connection getConnection(long timeout) throws SQLException {
		Connection con = super.getConnection(timeout);
		disableAutoCommit(con);
		return con;
	}

}
