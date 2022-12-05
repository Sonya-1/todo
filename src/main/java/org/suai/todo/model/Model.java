package org.suai.todo.model;

import java.util.ArrayList;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;

public class Model {
	private static Connection conn = null;
	
	public static void init(String dbFileName) throws ServletException {
		if (conn != null) {
			return;
		}
		try {
			Class.forName("org.sqlite.JDBC");
			
			File f = new File(dbFileName);
			String url = "jdbc:sqlite:" + f.getAbsolutePath();
			
			if(f.exists()) {
				conn = DriverManager.getConnection(url);
			} else {
				createConn(url);
			}
		} catch (Exception err) {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {}
			
			throw new ServletException(err);
		}
	}
	
	private static void createConn(String url) throws SQLException {
		Statement stmt = null;
		String sql;
		
		conn = DriverManager.getConnection(url);
		DatabaseMetaData meta = conn.getMetaData();
		System.out.println("The driver name is " + meta.getDriverName());
		System.out.println("A new database has been created.");
		
		sql = "CREATE TABLE IF NOT EXISTS users (\n"
            + "	userid integer PRIMARY KEY AUTOINCREMENT,\n"
            + "	name text NOT NULL,\n"
            + "	salt text NOT NULL,\n"
            + "	hash text NOT NULL,\n"
            + "	itemsperpage integer NOT NULL\n"
            + ");";
		stmt = conn.createStatement();
		stmt.executeUpdate(sql);
		
        sql = "CREATE TABLE IF NOT EXISTS todos (\n"
            + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
            + "	parent integer NOT NULL,\n"
            + "	owner integer NOT NULL,\n"
            + "	priority integer NOT NULL,\n"
            + "	done integer NOT NULL,\n"
            + "	created integer NOT NULL,\n"
            + "	deadline integer NOT NULL,\n"
            + "	caption text NOT NULL,\n"
            + "	comment text\n"
            + ");";
		stmt.executeUpdate(sql);
		stmt.close();
	}
	
	public static User getUser(Integer id) throws ServletException {
		User user = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		if (id == null || id < 1) {
			return user;
		}
		if (conn == null) {
			throw new ServletException("No database connection");
		}
		try {
			String sql = "SELECT * FROM users WHERE userid = ?;";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				user = new User(rs);
			}
		} catch (SQLException err) {
			throw new ServletException(err);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {}
			}
		}
		return user;
	}
	
	public static User getUserByName(String username) throws ServletException {
		User u = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		if (username == null || username.length() == 0) {
			return u;
		}
		if (conn == null) {
			throw new ServletException("No database connection");
		}
		try {
			String sql = "SELECT * FROM users WHERE name = ?;";
			stmt  = conn.prepareStatement(sql);
			stmt.setString(1, username);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				u = new User(rs);
			}
		} catch (SQLException err) {
			throw new ServletException(err);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {}
			}
		}
		return u;
	}
	
	public static void save(User user) throws ServletException {
		PreparedStatement stmt = null;
		String sql;
		
		if (conn == null) {
			throw new ServletException("No database connection");
		}
		try {
			if (user.id == 0) {
				sql = "INSERT INTO users (name, salt, hash, itemsperpage) " +
				      "VALUES (?, ?, ?, ?);";
				stmt = conn.prepareStatement(sql);
				
				stmt.setString(1, user.name);
				stmt.setString(2, user.salt);
				stmt.setString(3, user.hash);
				stmt.setInt(4, user.itemsPerPage);
			} else {
				sql = "UPDATE users " +
				      "SET name = ?, salt = ?, hash = ?, itemsperpage = ? " +
				      "WHERE userid = ?;";
			
				stmt = conn.prepareStatement(sql);
						
				stmt.setString(1, user.name);
				stmt.setString(2, user.salt);
				stmt.setString(3, user.hash);
				stmt.setInt(4, user.itemsPerPage);
				stmt.setInt(5, user.id);
			}
			stmt.executeUpdate();
		} catch (SQLException err) {
			throw new ServletException(err);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {}
			}
		}
	}
	
	public static void save(Record r) throws ServletException {
		PreparedStatement stmt = null;
		String sql;

		Long created = Record.toUnixTimestamp(r.created);
		Long deadline = Record.toUnixTimestamp(r.deadline);
		
		if (conn == null) {
			throw new ServletException("No database connection");
		}
		try {
			if (r.id > 0) {
				
				sql = "UPDATE todos " +
					  "SET parent = ?, owner = ?, priority = ?, done = ?, " +
						   "created = ?, deadline = ?, caption = ?, comment = ? " +
					  "WHERE id = ?;";
				      
				stmt = conn.prepareStatement(sql);					
				stmt.setInt(1, r.parent);
				stmt.setInt(2, r.owner);
				stmt.setInt(3, r.priority);
				stmt.setInt(4, r.done);
				stmt.setLong(5, created);
				stmt.setLong(6, deadline);
				stmt.setString(7, r.caption);
				stmt.setString(8, r.comment);
				stmt.setInt(9, r.id);
				
			} else {
				sql = "INSERT INTO todos (parent, owner, priority, done, " +
					                        "created, deadline, caption, comment) " +
				       "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
				
			    stmt = conn.prepareStatement(sql);
			    stmt.setInt(1, r.parent);
				stmt.setInt(2, r.owner);
				stmt.setInt(3, r.priority);
				stmt.setInt(4, r.done);
				stmt.setLong(5, created);
				stmt.setLong(6, deadline);
				stmt.setString(7, r.caption);
				stmt.setString(8, r.comment);
			}
			stmt.executeUpdate();
		} catch (SQLException err) {
			throw new ServletException(err);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {}
			}
		}
	}
	
	public static void delete(Record r) throws ServletException {
		delete(r.id);
	}
	
	private static void delete(Integer id) throws ServletException {
		PreparedStatement stmt = null;
		String sql;
		
		if (conn == null) {
			throw new ServletException("No database connection");
		}
		try {
			sql = "SELECT * FROM todos WHERE parent = ?;";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				delete(rs.getInt("id"));
			}
			sql = "DELETE FROM todos WHERE id = ?;";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.executeUpdate();
		} catch (SQLException err) {
			throw new ServletException(err);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {}
			}
		}
	}
		
	public static Integer getCount(User user, Integer parent) throws ServletException {
		PreparedStatement stmt = null;
		String sql;
		
		if (conn == null) {
			throw new ServletException("No database connection");
		}
		try {
			sql = "SELECT COUNT(*) FROM todos WHERE owner = ? AND parent = ?;";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, user.id);
			stmt.setInt(2, parent);
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException err) {
			throw new ServletException(err);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {}
			}
		}
		return 0;
	}
	
	public static Record getTodo(User user, Integer id) throws ServletException {
		if (user == null || id == null || id < 1) {
			return null;
		}
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Record r = null;
		String sql;
		
		if (conn == null) {
			throw new ServletException("No database connection");
		}
		try {
			sql = "SELECT * FROM todos " +
			          "WHERE owner = ? AND id = ?;";
			stmt  = conn.prepareStatement(sql);
			stmt.setInt(1, user.id);
			stmt.setInt(2, id);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				r = new Record(rs);
			}
		} catch (SQLException err) {
			throw new ServletException(err);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {}
			}
		}
		return r;
	}
	
	public static ArrayList<Record> getTodos() throws ServletException {
		ArrayList<Record> list = new ArrayList<Record>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql;
		
		if (conn == null) {
			throw new ServletException("No database connection");
		}
		try {
			sql = "SELECT * FROM todos ORDER BY id;";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				list.add(new Record(rs));
			}
		} catch (SQLException err) {
			throw new ServletException(err);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {}
			}
		}
		return list;
	}
	
	public static ArrayList<Record> getTodos(
			User user, 
			Integer parent, 
			Integer offset) 
	throws ServletException {

		Integer limit = user.itemsPerPage;
		ArrayList<Record> list = new ArrayList<Record>();
		PreparedStatement stmt = null;
		ResultSet rs;
		String sql;
		
		if (conn == null) {
			throw new ServletException("No database connection");
		}
		try {
			sql = "SELECT * FROM todos " +
				           "WHERE owner = ? AND parent = ? " + 
				           "ORDER BY done, priority DESC, deadline, created " +
				           "LIMIT ? OFFSET ?;";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, user.id);
			stmt.setInt(2, parent);
			stmt.setInt(3, limit);
			stmt.setInt(4, offset);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				list.add(new Record(rs));
			}
		} catch (SQLException err) {
			throw new ServletException(err);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {}
			}
		}
		return list;
	}	
}
