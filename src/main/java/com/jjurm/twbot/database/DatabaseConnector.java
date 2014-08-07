package com.jjurm.twbot.database;

import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.configuration.HierarchicalConfiguration;

import snaq.db.ConnectionPool;

/**
 * Class for simply returning the <tt>Connection</tt>s to the database.
 * 
 * @author JJurM
 */
public class DatabaseConnector {
	private DatabaseConnector() {} // Prevent instantiating

	/**
	 * Factory for creating connections to database.
	 */
	static ConnectionPoolFactory factory;

	/**
	 * This will load database login configuration
	 * 
	 * @param config
	 */
	public static void loadConfig(HierarchicalConfiguration config) {
		// Get data
		String host = config.getString("host");
		String user = config.getString("user");
		String password = config.getString("password");
		String database = config.getString("database");

		// Create factory
		factory = new ConnectionPoolFactory(host, user, password, database);
	}

	/**
	 * This creates new connection to the database.
	 * 
	 * @return new connection
	 * @throws SQLException
	 */
	public static ConnectionPool newConnectionPool() {
		return factory.newConnectionPool();
	}

	/**
	 * Factory for creating new connection to the database
	 * 
	 * @author JJurM
	 */
	static class ConnectionPoolFactory {

		String host;
		String user;
		String password;
		String db;

		ConnectionPoolFactory(String host, String user, String password, String db) {
			this.host = host;
			this.user = user;
			this.password = password;
			this.db = db;
		}

		/**
		 * Creates new connection.
		 * 
		 * @return new connection
		 * @throws SQLException
		 */
		ConnectionPool newConnectionPool() {

			Properties props = new Properties();
			props.put("user", user);
			props.put("password", password);

			String url = "jdbc:mysql://" + host + "/" + db;
			ConnectionPool pool = new NoAutoCommitConnectionPool("local", 0, 5, 5, 0, url, props);
			pool.init(2);

			return pool;

		}

	}

}
