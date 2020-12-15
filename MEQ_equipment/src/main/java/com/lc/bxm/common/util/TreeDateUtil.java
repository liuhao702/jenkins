package com.lc.bxm.common.util;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.TreeData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 ** 递归层级数据
 * @author liuhao
 *
 */
@Service
public class TreeDateUtil {

	
	@Autowired
	PostgreSQLConn dbConn;

	/**
	 * LH 下拉框层级数据方法 传入相应的sql
	 * @param sql
	 * @return
	 */
	public Object getTree(String sql) {
        List<TreeData> baseLists =getTreeData(sql);
        List<TreeData> treeLists =new ArrayList<TreeData>();
        // 总数据，出一级数据，一级数据没有父id
        for (TreeData e:  baseLists) {
            if(e.getParentUid()==(Object)0 || e.getParentUid()==null){
            	treeLists.add( e );
            }
        }
        // 遍历一级数据
        for (TreeData e: treeLists) {
            // 将子元素 set进一级菜单里面
               e.setChildren(getChild( e.getId(), baseLists)); 
			}
        JSONArray  json  =  JSONArray.fromObject(treeLists); 
       return traverseJson(json);
    }

	/**
	 * 	  获取子节点  递归拼接子节点
	 * @param pid  父级id
	 * @param elements 总数据集合
	 * @return
	 */
	public List<TreeData> getChild(Object pid, List<TreeData> elements) {
		List<TreeData> childs = new ArrayList<TreeData>();
		for (TreeData e : elements) {
			if (e.getParentUid() instanceof Integer) { //判断父级id如果是integer就使用==
				if (e.getParentUid()!=(Object)0) {
					if (e.getParentUid()==pid) {
						// 子菜单的下级菜单
						childs.add(e);
				}
			}
			}else {
				if (e.getParentUid()!=null) {//否则则是其他类型使用=null判断
					String str=e.getParentUid().toString(); //判断如果是uuid类型给他转换成字符串类型
					if (pid.equals(str)) {
						if (e.getParentUid() instanceof UUID) {//判断如果是uuid类型给他转换成字符串类型重新给他设值
							e.setParentUid(e.getParentUid().toString());
						}
						// 子菜单的下级菜单
						childs.add(e);
					}
			    }
	     	}
		}
		// 把子菜单的子菜单再循环一遍
		for (TreeData e : childs) {
			// 继续添加子元素
			e.setChildren(getChild(e.getId(), elements));
		}
		// 停下来的条件，如果 没有子元素了，则停下来
		if (childs.size() == 0) {
			return null;
		}
		return childs;
	}

	/**
	 * LH获取所有的数据
	 * @param valueMember   主键字段
	 * @param displayMember 显示字段
	 * @param sql 查询的数据
	 * @return
	 */
	public List<TreeData> getTreeData(String sql) {
		ResultSet rs = dbConn.query(sql);
		TreeData treeData = null;
		List<TreeData> treeList = new ArrayList<TreeData>();
		try {
			while (rs.next()) {
				treeData = new TreeData();
				if (rs.getObject(1) instanceof UUID) {   //判断如果是uuid类型给他转换成字符串类型
					treeData.setId(rs.getObject(1).toString());
				}else {
					treeData.setId(rs.getObject(1));
				}
				treeData.setLabel(rs.getString(2));
				treeData.setParentUid(rs.getObject(3));
				treeList.add(treeData);
			}
		} catch (SQLException e) {
		}
		return treeList;
	}
	
	//去掉json的空集合
	 @SuppressWarnings({ "rawtypes", "unchecked"})
	 private Object traverseJson(Object json) {
        if (json == null) {
            return null;
        }
        try {
            if (json instanceof JSONObject) {// 判断是json数据
                JSONObject jsonObj = (JSONObject)json;
				List keyList=new ArrayList();
                for(Object k:jsonObj.keySet()){ //获取json遍历key
                    String value=jsonObj.get(k).toString();//转换成字符串
                    if(value.equals("[]")){ //如果包含"[]"就清空当前元素
                        keyList.add(k);
                    }else{
                        if(isJsonObj(value)){//递归进行删除为"[]"元素
                            jsonObj.put(k, traverseJson(JSONObject.fromObject(value)));
                        }else{if(isJsonArr(value)){
                            jsonObj.put(k,  traverseJson(JSONArray.fromObject(value))) ;
                        }
                        }
                    }
                }
                for(Object k:keyList){
                    jsonObj.remove(k);
                }
                return jsonObj;
            }else if (json instanceof JSONArray) {// 判断是json数组数据
                JSONArray jsonArr = (JSONArray)json;
                int len = jsonArr.size();
                for (int i = 0; i < len; ++i) { 
                    jsonArr.set(i, traverseJson(jsonArr.get(i)));
                }
                return jsonArr;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
	 private boolean isJsonObj(Object o){
        try{
            @SuppressWarnings("unused")
			JSONObject js=JSONObject.fromObject(o.toString());
            return true;
        }catch(Exception e){
            return false;
        }
    }
    
	 private boolean isJsonArr(Object o){
        try{
            @SuppressWarnings("unused")
			JSONArray js=JSONArray.fromObject(o.toString());
            return true;
        }catch(Exception e){
            return false;
        }
    }
	 
	 
	 public List<Map<Object,Object>>  getResultSet(ResultSet rs) {
		  	List<Map<Object,Object>> list = getMenuGroupJson(rs);
		  	List<Map<Object,Object>> list2= new ArrayList<Map<Object,Object>>();
		  	for (int i = 0; i < list.size(); i++) {
					if (list.get(i).get("parentId")==null) {
						list2.add(list.get(i));
					}
		  	}
					 // 遍历一级菜单
				    for (int j = 0; j < list2.size(); j++) {
				        // 将子元素 set进一级菜单里面
				    	list2.get(j).put("children", getChildTree(list2.get(j).get("id"),list));
				    }
		  	return list2;
		  	
		  }
	 
	 private List<Map<Object,Object>> getMenuGroupJson(ResultSet rs) {
       List<Map<Object,Object>> list= new ArrayList<Map<Object,Object>>();
		try {
		 Map<Object,Object> map= null;         
		    while (rs.next()) {
		    	map= new HashMap<Object, Object>();
		        	    map.put("id", rs.getString(1));
		        	    map.put("name", rs.getString(2));
		        	    map.put("parentId", rs.getString(3));
			       list.add(map);
			     }
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		return list;
}



		/**
		* 获取子节点
		* @param object
		* @param elements
		* @return
		*/
		private List<Map<Object,Object>> getChildTree(Object object , List<Map<Object,Object>> list){
			List<Map<Object,Object>> list2= new ArrayList<Map<Object,Object>>();
		   for (int i = 0; i < list.size(); i++) {
		       if(list.get(i).get("parentId")!=null){
		           if(list.get(i).get("parentId").equals(object)){
		               // 子菜单的下级菜单
		           	list2.add(list.get(i));
		           }
		       }
		   }
		   // 把子菜单的子菜单再循环一遍
		   for (int j = 0; j < list2.size(); j++) {
			        // 将子元素 set进一级菜单里面
			    	list2.get(j).put("children", getChildTree(list2.get(j).get("id"),list));
			    }
		   //停下来的条件，如果 没有子元素了，则停下来
		   if( list2.size()==0 ){
		       return null;
		   }
		   return list2;
		}
	 
}
