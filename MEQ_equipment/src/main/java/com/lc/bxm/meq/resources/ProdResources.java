package com.lc.bxm.meq.resources;

import java.io.IOException;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.common.util.ReadExcel;
import com.lc.bxm.common.util.TreeDateUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONObject;

/**
 * MEQ产品接口
 * @author JF
 * @date 2019年7月2日
 */
@RestController
@RequestMapping("/prod")
public class ProdResources {
	
	@Autowired
	PostgreSQLConn dbConn;

	@Autowired
	ReadExcel read;
	
	@Autowired
	Message message;
	
	@Autowired 
	TreeDateUtil tree;
	
	
	/**
	 * JF 获取产品类别树状图JSON
	 */
	@RequestMapping(value = "prodMenuJson", method = RequestMethod.GET)
 	@ResponseBody
 	public String prodMenuJson() {
		ResultSet rs = dbConn.query("select cate_id,cate_name,parent_cate_id from meq_product_cates");
		List<Map<Object, Object>> prodJson= tree.getResultSet(rs);
//		String prodJson = getProdMenuGroupJson("");
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", prodJson);
		return "["+json+"]";
 	}
	
	/**
	 * JF 获取产品类别树状表格JSON
	 */
	@RequestMapping(value = "prodJson", method = RequestMethod.GET)
	@ResponseBody
	public String getDeptJson() {
		String prodJson = getProdGroupJson("").replace("null", "");
		return "{\"data\":[" + prodJson + "]" + getTitleJson() + "}";
	}
	
	/**
	 * JF 拼接产品类别分组JSON
	 */
	private String getProdGroupJson(String parentId) {
		StringBuilder sb = new StringBuilder();
		try {
			ResultSet rs = null;
			if (parentId == "") {
				rs = dbConn.query("select * from v_meq_product_cates where parent_cate_id is null");
			} else {
				rs = dbConn.query(String.format(
						"select * from v_meq_product_cates where parent_cate_id = '%s'", parentId));
			}
			int column = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				sb.append("{");
				for (int i = 1; i <= column; i++) {
					String names = rs.getMetaData().getColumnName(i);
					sb.append("\"" + names + "\":\"" + rs.getString(i) + "\",");
				}
				String str = getProdGroupJson(rs.getString("cate_id"));
				if (!str.equals("")) {
					sb.append("\"children\":[" + str + "]");
				} else {
					// 去掉最后面多余的逗号
					sb.deleteCharAt(sb.length() - 1);
				}
				sb.append("},");
			}
		}catch (SQLException e) {}
		return Str.delComma(sb.toString());
	}
	
	/**
	 * JF 获取列名和列
	 */
	private String getTitleJson() {
		StringBuilder sb = new StringBuilder();
		 JSONObject json = new JSONObject();
	     String str =null;
		ResultSet rs = dbConn.query(
				"select caption,column_name from sys_columns where table_name = 'v_meq_product_cates' and is_visible = 'true' order by idx");
		try {
			while (rs.next()) {
				json.put("title", rs.getString(1));
				json.put("key", rs.getString(2));
				sb.append(json.toString()+",");
			}
			str=sb.deleteCharAt(sb.length()-1).toString();
		} catch (SQLException e) {
		}
		return ","+"\"column\":["+str+"]";
	}
	
	/**
	 * JF 产品类别树状图表格分页
	 */
	@RequestMapping(value = "prodTableJson", method = RequestMethod.GET)
	@ResponseBody
	public String getProdTableJson(@RequestParam String tableName, @RequestParam int pageSize,
			@RequestParam int currentPage, @RequestParam String inputSearch, @RequestParam String order,
			@RequestParam String prop, @RequestParam String filterString, @RequestParam String id) {
		if (filterString != "") {
			filterString = filterString.replace("'", "''");
		}
		String userJson = null;
		if (order.equals("descending")) {
			order = "desc";
		}
		if (order.equals("ascending")) {
			order = "asc";
		}
		// 根据传进来的树的ID查找出所有的下级ID
		if (id != null && !id.equals("")) {
			String idArray = getId(id);
			if (idArray != null && !idArray.equals("")) {
				String res = "(" + idArray.substring(0, idArray.length() - 1) + ")";
				filterString = " cate_id in" + res;
			} else {
				if (tableName.equals("v_meq_product_cates")) {
					filterString = " cate_id = null";
				} else {
					filterString = " cate_id = " + id;
				}
			}
		}
		
		String sql =null;
		 if(tableName.equals("v_meq_products")) {
			 sql ="select bxm_get_product_page_json(" + "'*','','" + filterString + "','" + inputSearch
						+ "','" + prop + " " + order + "'," + pageSize + "," + currentPage + ")";
		 }else {
			sql = "select bxm_get_grid_page_json('" + tableName + "','*','','" + filterString + "','"
					+ inputSearch + "','" + prop + " " + order + "'," + pageSize + "," + currentPage + ")";
		}
		ResultSet rs = dbConn.query(sql);
		try {
			if (rs.next()) {
				userJson = rs.getString(1);
			}
		} catch (SQLException e) {
		}
		return userJson;
	}
	
	/**
	 * JF 根据父级ID递归查询所有的子ID
	 */
	private String getId(String id) {
		StringBuilder sb = new StringBuilder();
		String sql = "select cate_id from meq_product_cates where parent_cate_id = '" + id + "'";
		ResultSet rs = dbConn.query(sql);
		try {
			while(rs.next()) {
				sb.append(rs.getString(1) + ",");
				sb.append(getId(rs.getString(1)));
			}
		} catch (SQLException e) {}
		return sb.toString();
	}
	
	/**
	 * 导入Excel数据
	 */
	@RequestMapping(value = "productUpload", method = RequestMethod.POST)
	@ResponseBody
	public String productUpload(MultipartHttpServletRequest request) throws IOException {
		try {
			MultipartFile file = request.getFile("file");
			if (file == null) {
				return message.getErrorInfo("导入文件为空");
			}
			// 获取文件名称
			String fileName = file.getOriginalFilename();
			// 判断上传的文件是否为excel
			String fileType = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
			if (!"xlsx".equalsIgnoreCase(fileType)) {
				return message.getErrorInfo("导入失败,文件格式不符合要求");
			}
			InputStream input = file.getInputStream();
			List<List<Object>> list = read.readExcel(fileName, input, true);
			// 关闭流
			input.close();
			// 执行数据插入的操作
			String string = addProduct(list);
			if (!string.equals("导入成功")) {
				//return message.getErrorInfo("导入失败,文件内容不符合要求");
				return message.getErrorInfo(string);
			}
		} catch (Exception e) {
			return message.getErrorInfo("导入失败,文件内容不符合要求");
		}
		return message.getSuccessInfo("导入成功");
	}

	/**
	 * 导入ERP数据
	 */
	public String addProduct(List<List<Object>> list) {
		try {
			if (null != list && list.size() > 1) {
				String sqlInsert = null;
				StringBuilder valueString = new StringBuilder();
				StringBuilder valueResult = new StringBuilder();
				String temp = null;
				String columnName = null;
				ResultSet rs = null;
				// 验证数据
				// List<Object> lo2 = list.get(1);
				for (int j = 1; j < list.size(); j++) {
					valueString.setLength(0);
					List<Object> lo = list.get(j);
					// 下面是判断1,2,3列不允许为空
					for (int k = 0; k < lo.size(); k++) {
						columnName = list.get(0).get(k).toString();
						temp = lo.get(k) == null ? "" : lo.get(k).toString();
						if (columnName.equals("产品编码")) {
							if (temp.isEmpty()) {
								return  "导入失败,产品编码不能为空";
							} else {
								rs = dbConn.query(String.format(
										"SELECT EXISTS (SELECT product_code FROM meq_products_new WHERE product_code = '%s')",
										lo.get(k)));
								if (rs.next()) {
									if (rs.getBoolean(1)) {
										return "导入失败，产品编码不能重复";
									} else {
										temp = lo.get(k).toString();
									}
								}
							}
						} else if (columnName.equals("产品名称")) {
							if (temp.isEmpty()) {
								return "导入失败,产品名称不能为空";
							} else {
								temp = lo.get(k).toString();
							}
						} else if (columnName.equals("产品类别")) {
							rs = dbConn.query(String.format(
									"SELECT EXISTS (SELECT cate_id FROM meq_product_cates WHERE cate_name = '%s')",
									lo.get(k)));
							if (rs.next()) {
								if (!rs.getBoolean(1)) {
									return "导入失败,产品类别不存在";
								} else {
									temp = lo.get(k).toString();
								}
							}
						} else if (columnName.equals("产品类型")) {
							if (lo.get(k) == null) {
							} else {
								rs = dbConn.query(String.format(
										"SELECT EXISTS (SELECT type_id FROM meq_product_types WHERE type_name = '%s')",
										lo.get(k)));
								if (rs.next()) {
									if (!rs.getBoolean(1)) {
										return "导入失败,产品类型不存在";
									} else {
										temp = lo.get(k).toString();
									}
								}
							}
						} else if (columnName.equals("气种")) {
							if (lo.get(k) == null) {
							} else {
								rs = dbConn.query(String.format(
										"SELECT EXISTS (SELECT gas_id FROM meq_product_gases WHERE gas_name = '%s')",
										lo.get(k)));
								if (rs.next()) {
									if (!rs.getBoolean(1)) {
										return "导入失败,气种不存在";
									} else {
										temp = lo.get(k).toString();
									}
								}
							}
						} else if (columnName.equals("容量")) {
							if (lo.get(k)==null||lo.get(k).equals("")) {
								temp = "0";
							} else {
								temp = lo.get(k).toString();
							}
						} else if (columnName.equals("功率(KW)")) {
							if (temp.isEmpty()) {
								return "导入失败,功率(KW)不能为空";
							} else {
								temp = lo.get(k).toString();
							}
						} else if (columnName.equals("客户")) {
							if (lo.get(k) == null) {
								temp = null;
							} else {
								temp = lo.get(k).toString();
							}
						} else if (columnName.equals("客户条码")) {
							if (lo.get(k)==null||lo.get(k).equals("")) {
								temp = "false";
							} else {
								temp = lo.get(k).equals("有") ? "true" : "false";
							}
						} else if (columnName.equals("累计生产数量")) {
							if (lo.get(k)==null||lo.get(k).equals("")) {
								temp = "0";
							}else {
								temp = lo.get(k).toString();
							}
						} else if (columnName.equals("单线班产(8h)")) {
							if (temp.isEmpty()) {
								return "导入失败,单线班产不能为空";
							} else {
								temp = lo.get(k).toString();
							}
						}
						if (!(temp == null) && !temp.isEmpty()) {
							valueString.append(String.format("'%s',", temp));
						} else {
							valueString.append(String.format("null,"));
						}
					}
					if (j > 0) {
						valueResult.append(String.format("(%s),", Str.delComma(valueString.toString())));
					}
				}
				sqlInsert = String.format(
						"INSERT INTO meq_products_new (product_code,product_name,cate_id,type_id,"
								+ "gas_id,capacity,power,customer,customer_barcode,produce_qty,daily_output) VALUES %s",
						Str.delComma(valueResult.toString()));
				if (dbConn.queryUpdate(sqlInsert)) {
					return "导入成功";
				} else {
					return "导入失败,sql语句不符合要求";
				}
			} else {
				return "导入失败,表格无数据";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "导入失败，导入数据模板不符合！" ;
		}
	}
	
	
}