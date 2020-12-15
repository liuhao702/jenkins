package com.lc.bxm.common.helper;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import com.lc.bxm.dbconnection.PostgreSQLConn;

public class FileUtil { // D:/code/ getRealPath()
	
	
	PostgreSQLConn dbConn = new PostgreSQLConn();
	//图片
     File uploadDirectorys = new File(dbConn.getPropertiesYun("CreateFilePath"));
 	//图片
     File uploadDirectoryAly = new File(dbConn.getPropertiesYun("CreateFilePathAly"));
    //模板
     File uploadDirectory = new File(dbConn.getPropertiesYun("CreateFile_tempalate"));
     
     //报表
 	private String filePath = null;
	
	/**
	 * 保存图片
     * @param savePath
     * @param fileFullName
     * @param file
     * @return
     * @throws Exception
     */
    public boolean saveFiles( HttpServletRequest request , String fileFullName, MultipartFile file) throws Exception {
        byte[] data = readInputStream(file.getInputStream());
        //new一个文件对象用来保存图片，默认保存当前工程根目录
        File uploadFile = new File(uploadDirectorys+"/"+fileFullName);
        //判断文件夹是否存在，不存在就创建一个
        File fileDirectory = new File(uploadDirectorys.toString());
        synchronized (uploadDirectorys){
            if (!fileDirectory.exists()) {
                if (!fileDirectory.mkdirs()) {
                    throw new Exception("文件夹创建失败！路径为：" + fileDirectory);
                }
            }
        }

        FileOutputStream outStream = null;
       //创建输出流
       try{//写入数据
        	outStream = new FileOutputStream(uploadFile);
            outStream.write(data);
            outStream.flush();
        } catch (Exception e) {
           e.printStackTrace();
           throw e;
        }finally {
       	outStream.close();
		}
        return uploadFile.exists();
    }
    
	/**
	 * 保存图片阿里云数据
     * @param savePath
     * @param fileFullName
     * @param file
     * @return
     * @throws Exception
     */
    public boolean saveFilesAly( HttpServletRequest request , String fileFullName, MultipartFile file) throws Exception {
        byte[] data = readInputStream(file.getInputStream());
        //new一个文件对象用来保存图片，默认保存当前工程根目录
        File uploadFile = new File(uploadDirectoryAly+"/"+fileFullName);
        //判断文件夹是否存在，不存在就创建一个
        File fileDirectory = new File(uploadDirectoryAly.toString());
        synchronized (uploadDirectoryAly){
            if (!fileDirectory.exists()) {
                if (!fileDirectory.mkdirs()) {
                    throw new Exception("文件夹创建失败！路径为：" + fileDirectory);
                }
            }
        }

        FileOutputStream outStream = null;
       //创建输出流
       try{//写入数据
        	outStream = new FileOutputStream(uploadFile);
            outStream.write(data);
            outStream.flush();
        } catch (Exception e) {
           e.printStackTrace();
           throw e;
        }finally {
       	outStream.close();
		}
        return uploadFile.exists();
    }
    
    /**
     * 获取报表
     * @param response
     * @param fileName
     * @throws Exception
     */
    public void fileDownload(HttpServletResponse response,String fileName) throws Exception {
    	 filePath=dbConn.getPropertiesYun("fileRepot");
    	InputStream is =null;
		OutputStream fos = null;
		byte[] data = null;
		try {
			is = new FileInputStream(filePath + "/" + fileName);
			fos = new BufferedOutputStream(response.getOutputStream());
		    data = new byte[1024*1024]; 
			int len =-1;
			while((len = is.read(data))!= -1){
				fos.write(data,0,len);
			}    
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			is.close();
			fos.close();
		}
    }
    
    public byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len;
        //使用一个输入流从buffer里把数据读取出来
        while ((len = inStream.read(buffer)) != -1) {
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }
 
    
    /**
     * 获得绝对路径（不带文件名）
     *
     * @return
     */
    
     public String getRealPath() {
        String path = FileUtil.class.getResource("/../..").getFile();
//       int index = path.indexOf("build");
//        realPath = path.substring(0, index) + "/src/main/webapp/";
//        realPath = realPath.replaceFirst("/", "");
       return path;
    }
     
     
     /**
      * 判断文件大小
      *
      * @param len
      *            文件长度
      * @param size
      *            限制大小
      * @param unit
      *            限制单位（B,K,M,G）
      * @return
      */
     public static boolean checkFileSize(Long len, int size, String unit) {
//         long len = file.length();
         double fileSize = 0;
         if ("B".equals(unit.toUpperCase())) {
             fileSize = (double) len;
         } else if ("K".equals(unit.toUpperCase())) {
             fileSize = (double) len / 1024;
         } else if ("M".equals(unit.toUpperCase())) {
             fileSize = (double) len / 1048576;
         } else if ("G".equals(unit.toUpperCase())) {
             fileSize = (double) len / 1073741824;
         }
         if (fileSize > size) {
             return true;
         }
         return false;
     }
     
      /**  添加模板方法
        * @param savePath
        * @param fileFullName
        * @param file
        * @return
        * @throws Exception
        */
       public boolean saveFileTem( HttpServletRequest request , String savePath, String fileFullName, MultipartFile file) throws Exception {
    	   System.err.println(uploadDirectory+fileFullName);
           byte[] data = readInputStream(file.getInputStream());
           //new一个文件对象用来保存图片，默认保存当前工程根目录
           File uploadFile = new File(uploadDirectory+"/"+fileFullName);
           //判断文件夹是否存在，不存在就创建一个
           File fileDirectory = new File(uploadDirectory.toString());
           synchronized (uploadDirectory){
               if (!fileDirectory.exists()) {
                   if (!fileDirectory.mkdirs()) {
                       throw new Exception("文件夹创建失败！路径为：" + fileDirectory);
                   }
               }
           }

           FileOutputStream outStream = null;
          //创建输出流
          try{//写入数据
           	outStream = new FileOutputStream(uploadFile);
               outStream.write(data);
               outStream.flush();
           } catch (Exception e) {
              e.printStackTrace();
              throw e;
           }finally {
          	outStream.close();
   		}
           return uploadFile.exists();
       }
       
       /**
        * 模板下载
        * @param response
        * @param filePath
        * @throws Exception
        */
       public void fileDownloadTemplate(HttpServletResponse response,String filePath) throws Exception {
       	String urls=dbConn.getPropertiesYun("fileTempalate_Url") + filePath;
       	System.err.println(urls);
       	URL url = new URL(urls);
       	InputStream is =null;
   		OutputStream fos = null;
   		byte[] data = null;
   		try {
   			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
   			is = connection.getInputStream();
   			fos = new BufferedOutputStream(response.getOutputStream());
   		    data = new byte[1024*1024]; 
   			int len =-1;
   			while((len = is.read(data))!= -1){
   				fos.write(data,0,len);
   			}    
   			fos.flush();
   		} catch (Exception e) {
   			e.printStackTrace();
   		}finally {
   			is.close();
   			fos.close();
   		}
       }
       
       /**  添加报表方法
        * @param savePath
        * @param fileFullName
        * @param file
        * @return
        * @throws Exception
        */
       public boolean saveFile( HttpServletRequest request , String savePath, String fileFullName, MultipartFile file) throws Exception {
           byte[] data = readInputStream(file.getInputStream());
           //new一个文件对象用来保存图片，默认保存当前工程根目录
           File uploadFile = new File(uploadDirectory + "/" + savePath + "/" + fileFullName);
           //判断文件夹是否存在，不存在就创建一个
           File fileDirectory = new File(uploadDirectory + "/" + savePath);
           synchronized (uploadDirectory){
               if (!fileDirectory.exists()) {
                   if (!fileDirectory.mkdir()) {
                       throw new Exception("文件夹创建失败！路径为：" + savePath);
                   }
               }
           }   
           FileOutputStream outStream = null;
           //创建输出流
           try{//写入数据
            	outStream = new FileOutputStream(uploadFile);
                outStream.write(data);
                outStream.flush();
            } catch (Exception e) {
               e.printStackTrace();
               throw e;
            }finally {
           	outStream.close();
    		}
            return uploadFile.exists();
        }
}
