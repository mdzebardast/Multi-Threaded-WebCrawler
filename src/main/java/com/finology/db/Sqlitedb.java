package com.finology.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Sqlitedb {
	static final String url = "jdbc:sqlite:products.db";

	public static void initialiseDatabase() throws Exception {
		Class.forName("org.sqlite.JDBC");
		DriverManager.getConnection(url);
		
		createTAble();
	}

	private static Connection connect() throws SQLException {
        Connection conn = null;
        conn = DriverManager.getConnection(url);
        return conn;
    }
	
	private static void createTAble() throws SQLException{
		Connection conn = null;
		Statement statement = null;
		try {
			String sql = "CREATE TABLE IF NOT EXISTS product (\n" 
					+ "    id integer PRIMARY KEY,\n"
					+ "    origin_id integer ,\n"
					+ "    name text NOT NULL,\n" 
					+ "    price text,\n" 
					+ "    description text,\n"
					+ "    extrainfo text\n" + ");";

			conn = connect();
			statement = conn.createStatement();
			// create a new table
			statement.execute(sql);
		} finally {
			try {
				if (conn != null)
					conn.close();
				if(statement !=null)
					statement.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e.getMessage());
			}
		}
	}
	
	public static int insert(long origin_id, String name, String price, String description, String extrainfo)throws Exception {
		String sql = "INSERT INTO product(origin_id, name, price, description, extrainfo) VALUES( ?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = connect();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, origin_id);
			pstmt.setString(2, name);
			pstmt.setString(3, price);
			pstmt.setString(4, description);
			pstmt.setString(5, extrainfo);

			return pstmt.executeUpdate();
		} finally {
			try {
				if (conn != null)
					conn.close();
				if (pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e.getMessage());
			}
		}
	}
	
	public static boolean findProductByOriginid(long origin_id)throws Exception {
		String sql = "SELECT id FROM product WHERE origin_id = ?";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			conn = connect();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, origin_id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next())
				return true;
		} finally {
			try {
				if (conn != null)
					conn.close();
				if (pstmt != null)
					pstmt.close();
				if (resultSet != null)
					resultSet.close();

			} catch (SQLException e) {
				// connection close failed.
				System.out.println(e.getMessage());
			}
		}

		return false;
	}
}















