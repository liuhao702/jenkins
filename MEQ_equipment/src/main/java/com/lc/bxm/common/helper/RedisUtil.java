package com.lc.bxm.common.helper;

import java.util.Iterator;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.lc.bxm.dbconnection.PostgreSQLConn;

import redis.clients.jedis.Jedis;

/**
 * @author lh
 * redis操作类
 */
@Repository
public class RedisUtil {
	
	static Jedis js = null;
    
	PostgreSQLConn dbConn = new PostgreSQLConn();
   
    private  String hostUrl=dbConn.getPropertiesYun("redis.host");
    private  int hostPort =Integer.parseInt(dbConn.getPropertiesYun("redis.port"));
    private  String pwd =dbConn.getPropertiesYun("redis.pwd");
 
	    /**
	     * 链接redis数据库，进行初始化
	     * @return：返回是否初始化链接成功
	     */
	    public Jedis init() {
	        if (js == null) {
	            js = new Jedis(hostUrl, hostPort);
	            js.auth(pwd);
	        }
	        if (js != null) {
	            //System.err.println("初始化成功");
	            return js;        
	        }
	        return js;  
	    }


	    /**
	     * 删除数据
	     *
	     * @param key：要删除数据的key
	     * @return：返回boolean值，表示是否删除成功
	     */
	    public boolean delete(String key) {
	        if (js.exists(key)) {
	            if (js.del(key) == 1) {
	                return true;
	            } else {
	                return false;
	            }
	        } else {
	            return false;
	        }
	    }

	    /**
	     * 按照关键字删除redis缓存数据
	     */
	    public void deleteData(String keys) {
	        //链接redis数据库，进行初始化
	        init();
	        Set<String> set = js.keys("*");
	        if (set.size() != 0) {
	            Iterator<String> it = set.iterator();
	            while (it.hasNext()) {
	                String key = it.next();
	                if (key.contains(keys)) {
	                    delete(key);
	                }
	            }
	        }
	        //关闭链接
	        unInit();
	    }


	    /**
	     * 关闭链接
	     */
	    public void unInit() {
	        if (js != null) {
	            js.close();
	            js = null;
	        }
	    }
}
