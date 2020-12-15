package com.lc.bxm.system.resources;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.Menu;

/**
 * @author LH
 * @createtime 2020/7/6 17:36.
 * @describe 用于封装树型结构树（无限层级） 
 */
@RestController
@RequestMapping({ "/menu" })
public class MenuUtil {
	
	@Autowired
	PostgreSQLConn dbConn;
	
	/**
	 * @方法名: getTree
	 * @描述: 组装菜单
	 * @param list 数据库里面获取到的全量菜单列表
	 * @return
	 */
	@RequestMapping(value = "menuJsonNew", method =RequestMethod.GET )
	@ResponseBody
		public List<Menu> getTree(@RequestParam String userCode, String is_admin,String user_id) {
		if (is_admin==null||is_admin.equals("")) {
		   is_admin = "null";
		}
	        List<Menu> baseLists = selectMenuData(userCode, is_admin, user_id);
	        List<Menu> menuLsit =new ArrayList<Menu>();
	        // 总菜单，出一级菜单，一级菜单没有父id
	        for (Menu e: baseLists) {
	            if( e.getParentId()==null ){
	            	menuLsit.add( e );
	            }
	        }
	        // 遍历一级菜单
	        for (Menu e: menuLsit) {
	            // 将子元素 set进一级菜单里面
	            e.setChildren(getChild(e.getId(),baseLists) );
	        }
	        return menuLsit;
	    }
	
		 /**
	     * 获取子节点
	     * @param pid
	     * @param elements
	     * @return
	     */
	    private List<Menu> getChild(String pid , List<Menu> elements){
	        List<Menu> childs = new ArrayList<>();
	        for (Menu e: elements) {
	            if(e.getParentId()!=null){
	                if(e.getParentId().equals(pid)){
	                    // 子菜单的下级菜单
	                    childs.add( e );
	                }
	            }
	        }
	        // 把子菜单的子菜单再循环一遍
	        for (Menu e: childs) {
	            // 继续添加子元素
	           e.setChildren( getChild( e.getId() , elements ) );
	        }
	        //停下来的条件，如果 没有子元素了，则停下来
	        if( childs.size()==0 ){
	            return null;
	        }
	        return childs;
	    }
	    
	    /**
	     * 判断角色获取所有菜单数据
	     * @param userCode
	     * @param is_admin
	     * @param user_id
	     * @return 
	     */
		public  List<Menu> selectMenuDataBak(String userCode, String is_admin,String user_id) {
			Menu menu =null;
			List<Menu> menulist = new ArrayList<Menu>();
			Boolean isAdmin =false;
			String sql = null;
//			ResultSet rsIsAdmin =dbConn.query(String.format("SELECT bxm_administrator_check('%s')",  userCode ));
			ResultSet rsIsAdmin =dbConn.query(String.format("SELECT bxm_administrator_check_by_uid('%s')",  user_id ));

				try {
					if (rsIsAdmin.next()) {
						isAdmin = rsIsAdmin.getBoolean(1);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				if (userCode.equals("bxmadmin")||is_admin.equals("bxmadmin")) {
					sql = "SELECT uid,name,parent_uid,url,menu_image FROM v_active_menu_tree ORDER BY idx";
				} else if (isAdmin) {
					sql = "SELECT uid,name,parent_uid,url,menu_image FROM v_not_deactive_menu_tree ORDER BY idx ";
				} else {
					sql = String.format("SELECT n.uid,n.name,n.parent_uid,n.url,n.menu_image FROM ( WITH RECURSIVE m AS"
							+ " (SELECT g.user_code,g.num,g.uid,g.name,g.idx,g.parent_uid,g.is_menu,g.url,g.para,g.menu_image "
							+ "FROM (select * from v_all_not_deactive_group_and_menu_tree where user_code = '%s' or not is_menu) "
							+ "g  WHERE g.is_menu UNION ALL SELECT a.user_code,a.num,a.uid,a.name,a.idx,a.parent_uid,a.is_menu,"
							+ "a.url,a.para,a.menu_image FROM (select * from v_all_not_deactive_group_and_menu_tree where "
							+ "user_code = '%s' or not is_menu) a  JOIN m m_1 ON a.uid = m_1.parent_uid) SELECT DISTINCT "
							+ "m.user_code,m.num,m.uid,m.name,m.idx,m.parent_uid,m.is_menu,m.url,m.para,m.menu_image FROM m)"
							+ " n order by idx",
							new Object[] { userCode, userCode });
				}
			  ResultSet rs = dbConn.query(sql);
			try {
				while(rs.next()) {
					menu = new Menu();
					menu.setId(rs.getString(1));
					menu.setName(rs.getString(2));
					menu.setParentId(rs.getString(3));
					menu.setUrl(rs.getString(4));
					menu.setIcon(rs.getString(5));
					menulist.add(menu);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return menulist;
		}
		
		
		
		 /**
	     * 判断角色获取所有菜单数据
	     * @param userCode
	     * @param is_admin
	     * @param user_id
	     * @return
	     */
		public  List<Menu> selectMenuData(String userCode, String is_admin,String user_id) {
			Menu menu =null;
			List<Menu> menulist = new ArrayList<Menu>();
			Boolean isAdmin =false;
			String sql = null;
			try {
				if (userCode.equals("bxmadmin")||is_admin.equals("bxmadmin")) {
					sql = "SELECT uid,name,parent_uid,url,menu_image FROM v_active_menu_tree ORDER BY idx";
				} else {
					ResultSet rsIsAdmin =dbConn.query(String.format("SELECT bxm_administrator_check_by_uid('%s')",  user_id ));
					if (rsIsAdmin.next()) {
						isAdmin = rsIsAdmin.getBoolean(1);
					}
					if (isAdmin) {
						sql = "SELECT uid,name,parent_uid,url,menu_image FROM v_not_deactive_menu_tree ORDER BY idx ";
					}else {
						sql = String.format("SELECT n.uid,n.name,n.parent_uid,n.url,n.menu_image FROM ( WITH RECURSIVE m AS"
								+ " (SELECT g.user_code,g.num,g.uid,g.name,g.idx,g.parent_uid,g.is_menu,g.url,g.para,g.menu_image "
								+ "FROM (select * from v_all_not_deactive_group_and_menu_tree where user_code = '%s' or not is_menu) "
								+ "g  WHERE g.is_menu UNION ALL SELECT a.user_code,a.num,a.uid,a.name,a.idx,a.parent_uid,a.is_menu,"
								+ "a.url,a.para,a.menu_image FROM (select * from v_all_not_deactive_group_and_menu_tree where "
								+ "user_code = '%s' or not is_menu) a  JOIN m m_1 ON a.uid = m_1.parent_uid) SELECT DISTINCT "
								+ "m.user_code,m.num,m.uid,m.name,m.idx,m.parent_uid,m.is_menu,m.url,m.para,m.menu_image FROM m)"
								+ " n order by idx",
							  userCode, userCode );  
						System.err.println(sql);
					}
				}
			  ResultSet rs = dbConn.query(sql);
				while(rs.next()) {
					menu = new Menu();
					menu.setId(rs.getString(1));
					menu.setName(rs.getString(2));
					menu.setParentId(rs.getString(3));
					menu.setUrl(rs.getString(4));
					menu.setIcon(rs.getString(5));
					menulist.add(menu);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return menulist;
		}

}
