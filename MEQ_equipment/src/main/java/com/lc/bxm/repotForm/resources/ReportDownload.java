package com.lc.bxm.repotForm.resources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.FileUtil;


/**
 * 数据报表通用下载
 * @author lh
 *
 */
@RestController
@RequestMapping("/download")
public class ReportDownload {
	
	
	@Autowired
	Message message;
	
	private static FileUtil fileUtil = new FileUtil();
	
	
	/**
	 * 获取报表文件下载功能
	 * @param request
	 * @param response
	 * @param fileCode
	 * @return
	 */
	@RequestMapping(value = "repotDownload", method = RequestMethod.POST)
	@ResponseBody
	public String fileDownload(HttpServletRequest request, HttpServletResponse response,@RequestBody String filePath) {
		try {
			String fileName = filePath+".xlsx";
			fileUtil.fileDownload(response, fileName);
			return message.getSuccessInfo("下载成功");
		} catch (Exception e) {
		}
		return message.getErrorInfo("下载失败");
	}

}
