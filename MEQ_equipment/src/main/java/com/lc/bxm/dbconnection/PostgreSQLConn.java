package com.lc.bxm.dbconnection;

import java.io.IOException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.lc.bxm.common.helper.RedisUtil;
import com.lc.bxm.entity.TestConnection;
import redis.clients.jedis.Jedis;

/**
 * 帮小蛮系统JDBC连接类
 * 
 * @author JF
 * @date 2019年4月17日
 */
@Repository
public class PostgreSQLConn {

	@Autowired
	TestConnection testConn;

	@Autowired
	RedisUtil redis;

	private String error = null;

	public String getErrorMessage() {
		return error;
	}

	/**
	 * 获取database目录下的配置文件信息
	 */
	private String getProperties(String parm) throws IOException {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("jdbc.properties");
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
		} catch (IOException ioE) {
			ioE.printStackTrace();
		} finally {
			inputStream.close();
		}
		return properties.getProperty(parm);
	}

	/**
	 * CONN连接 原版存db在数据库里
	 */
	public Connection getConn() {
		this.error = "";
		Connection connection = null;
//		if (testConn.getState()!=null) {
		if (error.equals("")) {
			try {
				Class.forName(getProperties("jdbc.driver")).newInstance();
				connection = DriverManager.getConnection(getProperties("jdbc.url"), getProperties("jdbc.userName"),
						getProperties("jdbc.password"));
			} catch (Exception e) {
				e.printStackTrace();
				this.error = e.getMessage();
			}
			return connection;
		} else {
			String dbName = null;
			ResultSet rs = null;
			if (testConn.getToken().contains(",")) {
				String tokenString[] = testConn.getToken().split(",");
				String token = tokenString[tokenString.length - 1].toString().trim();
				rs = PostgreSQLConn.this.queryUser("select dbname from dbbasename where token ='" + token + "'");
			} else {
				rs = PostgreSQLConn.this.queryUser("select dbname from dbbasename where token ='" + testConn.getToken() + "'");
			}
			try {
				if (rs.next()) {
					dbName = rs.getString(1);
				}
				Class.forName(getProperties("jdbc.driver")).newInstance();
				String url = getProperties("dynamic.url")+ dbName + "";
				connection = DriverManager.getConnection(url, getProperties("jdbc.userName"),
						getProperties("jdbc.password"));
			} catch (Exception e) {
				this.error = e.getMessage();
			}
			return connection;
		}
	}
	
     
	/**
	 * CONN连接 新版存db在redis
	 */
	public Connection getConn1() {
		this.error = "";
		Connection connection = null;
			String dbName = null;
			Jedis jedis = redis.init();
			if (testConn.getToken().contains(",")) {
				String tokenString[] = testConn.getToken().split(",");
				String token = tokenString[tokenString.length - 1].toString().trim();
				dbName = jedis.get(token);
			} else {
				dbName = jedis.get(testConn.getToken());
			}
			try {
				Class.forName(getProperties("jdbc.driver")).newInstance();
				String url = getProperties("dynamic.url")+ dbName + "";
				connection = DriverManager.getConnection(url, getProperties("jdbc.userName"),
						getProperties("jdbc.password"));
			} catch (Exception e) {
				this.error = e.getMessage();
			}
			return connection;
		}

	/**
	 * CONN连接 第一次用户登录没有token直接使用 testConn.getDatabaseName()————————————————————————————————————————————————————————
	 */
	public Connection getConnUserLogin() {
		this.error = "";
		Connection connection = null;
		try {
			try {
				Class.forName(getProperties("jdbc.driver")).newInstance();
				String url = getProperties("dynamic.url")+  testConn.getDatabaseName() + "";
				//String url = "jdbc:postgresql://47.113.94.121:5432/" + testConn.getDatabaseName() + "";
				connection = DriverManager.getConnection(url, getProperties("jdbc.userName"),
						getProperties("jdbc.password"));
			} catch (IOException e) {
				this.error = e.getMessage();
			}
		} catch (InstantiationException e) {
			this.error = e.getMessage();
		} catch (IllegalAccessException e) {
			this.error = e.getMessage();
		} catch (ClassNotFoundException e) {
			this.error = e.getMessage();
		} catch (SQLException e) {
			this.error = e.getMessage();
		}
		return connection;
	}

	/**
	 * 数据库查询
	 */
	public ResultSet queryUserLogin(String sql) {
		error = "";
		Connection conn = getConnUserLogin();
		PreparedStatement pStatement = null;
		ResultSet rs = null;
		try {
			pStatement = conn.prepareStatement(sql);
			rs = pStatement.executeQuery();
		} catch (SQLException e) {
			error = e.getMessage();
		} finally {
			try {
				if (conn != null) {
					conn.close();// 释放资源
				}
			} catch (SQLException e) {
				error = e.getMessage();
			}
		}
		return rs;
	}

	/**
	 * 数据增删改 第一次用户登录没有token直接使用token去redis寻找的db getConn ——————————————————————————————————————————————————————————————————
	 */
	public boolean queryUpdateUserLogin(String sql) {
		error = "";
		Connection conn = getConn();
		PreparedStatement pStatement = null;
		int rs = 0;
		try {
			pStatement = conn.prepareStatement(sql);
			rs = pStatement.executeUpdate();
		} catch (SQLException e) {
			error = e.getMessage();
		} finally {
			try {
				if (conn != null) {
					conn.close();// 释放资源
				}
			} catch (SQLException e) {
				error = e.getMessage();
			}
		}
		if (rs > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 数据库查询
	 */
	public ResultSet query(String sql) {
		error = "";
		Connection conn = getConn();
		PreparedStatement pStatement = null;
		ResultSet rs = null;
		try {
			pStatement = conn.prepareStatement(sql);
			rs = pStatement.executeQuery();
		} catch (SQLException e) {
			error = e.getMessage();
		} finally {
			try {
				if (conn != null) {
					conn.close();// 释放资源
				}
			} catch (SQLException e) {
				error = e.getMessage();
			}
		}
		return rs;
	}
	
	
	

	/**
	 * 数据增删改
	 */
	public boolean queryUpdate(String sql) {
		error = "";
		Connection conn = getConn();
		PreparedStatement pStatement = null;
		int rs = 0;
		try {
			pStatement = conn.prepareStatement(sql);
			rs = pStatement.executeUpdate();
		} catch (SQLException e) {
			error = e.getMessage();
		} finally {
			try {
				if (conn != null) {
					conn.close();// 释放资源
				}
			} catch (SQLException e) {
				error = e.getMessage();
			}
		}
		if (rs > 0) {
			return true;
		}
		return false;
	}
	


	/**
	 * 批量增加权限
	 */
	public boolean addPerList(List<String> list, String roleId) {
		error = "";
		Connection conn = getConn();
		PreparedStatement ps = null;
		String sql = "insert into sys_permissions(role_uid,fun_uid) values(?::uuid,?::uuid)";
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql);
			// 设置手动提交
			for (int i = 0; i < list.size(); i++) {
				ps.setString(1, roleId);
				ps.setString(2, list.get(i));
				ps.addBatch();
			}
			ps.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			error = e.getMessage();
			return false;
		} finally {
			try {
				if (conn != null) {
					conn.close();// 释放资源
				}
			} catch (SQLException e) {
				error = e.getMessage();
			}
		}
		return true;
	}

	
	 // 获取云门户数据库连接信息 jdbcYun.properties————————————————————————————————————————————————————————————————————————————————————————————
	
	public String getPropertiesYun(String parm) {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("jdbcYun.properties");
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
		} catch (IOException ioE) {
			ioE.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return properties.getProperty(parm);
	}

	// 连接云门户数据库——————————————————————————————————————————————————————————————————————————————————————————————————————————————————
	
	public Connection getConnection1() {
		error = "";
		Connection connection = null;
		try {
			// 加载数据驱动
			Class.forName(getPropertiesYun("jdbc.driver")).newInstance();
			connection = DriverManager.getConnection(getPropertiesYun("jdbc.url"), getPropertiesYun("jdbc.userName"),
					getPropertiesYun("jdbc.password"));
			
		} catch (InstantiationException e) {
			error = e.getMessage();
		} catch (IllegalAccessException e) {
			error = e.getMessage();
		} catch (ClassNotFoundException e) {
			error = e.getMessage();
		} catch (SQLException e) {
			error = e.getMessage();
		}
		return connection;
	}

	
	
//	public Connection getConnection() {
//		String url = testConn.getUrl();
//		Connection con = null;
//		try {
//			Class.forName("org.postgresql.Driver");
//			con = DriverManager.getConnection(url, testConn.getUserName(), testConn.getPassWord());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return con;
//	}

	/**
	 * 数据库查询
	 */
	public ResultSet queryUser(String sql) {
		error = "";
		Connection conn = getConnection1();
		PreparedStatement pStatement = null;
		ResultSet rs = null;
		try {
			pStatement = conn.prepareStatement(sql);
			rs = pStatement.executeQuery();
		} catch (SQLException e) {
			error = e.getMessage();
		} finally {
			try {
				if (conn != null) {
					conn.close();// 释放资源
				}
			} catch (SQLException e) {
				error = e.getMessage();
			}
		}
		return rs;
	}

	/**
	 * 数据增删改
	 */
	public boolean userUpdate(String sql) {
		error = "";
		// Connection conn = getConnection();
		Connection conn = getConnection1();
		PreparedStatement pStatement = null;
		int rs = 0;
		try {
			pStatement = conn.prepareStatement(sql);
			rs = pStatement.executeUpdate();
		} catch (SQLException e) {
			error = e.getMessage();
		} finally {
			try {
				if (conn != null) {
					conn.close();// 释放资源
				}
			} catch (SQLException e) {
				error = e.getMessage();
			}
		}
		if (rs > 0) {
			return true;
		}
		return false;
	}

	public boolean affair(String sql) {
		error = "";
		Connection conn = getConnection1();
		PreparedStatement pStatement = null;
		try {
			conn.setAutoCommit(false);
			pStatement = conn.prepareStatement(sql);
			pStatement.executeUpdate();
			conn.commit();
			conn.setAutoCommit(true);// 恢复JDBC事务的默认提交方式
			pStatement.close();
			return true;

		} catch (Exception e) {
			try {
				conn.rollback();// 回滚JDBC事务
				pStatement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return false;
		}
	}
	
	
	/**
	 * cyjq修改版本数据增删改
	 */
	public Object queryUpdateCyjq(String sql) {
		error = "";
		Connection conn = getConn();
		PreparedStatement pStatement = null;
		int rs = 0;
		try {
			pStatement = conn.prepareStatement(sql);
			rs = pStatement.executeUpdate();
		} catch (SQLException e) {
			error = e.getMessage();
			return error;
		} finally {
			try {
				if (conn != null) {
					conn.close();// 释放资源
				}
			} catch (SQLException e) {
				error = e.getMessage();
			}
		}
		if (rs > 0) {
			return true;
		}
		return false;
	}

}
