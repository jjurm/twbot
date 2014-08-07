package com.jjurm.twbot.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test class.
 * 
 * @author JJurM
 */
public class Test {

	/**
	 * This will test connection to the database. If the connection is not
	 * valid, it throws <tt>SQLException</tt>.
	 * 
	 * @param con Connection to the database
	 * @throws SQLException
	 */
	public static void testConnection(Connection con) throws SQLException {

		try (Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT 'ok' AS message")) {
			if (!rs.next()) {
				throw new SQLException("Database test failed");
			}
			String message = rs.getString("message");
			if (!message.equals("ok")) {
				throw new SQLException("Database test failed");
			}
		} catch (SQLException e) {
			throw e;
		}

	}

}
