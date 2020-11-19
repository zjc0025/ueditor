package com.xxx.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName HomeController
 * @Description
 * @Author ZJC
 * @Date 2020/11/18 10:55
 */
@Slf4j
@Controller
public class HomeController {

    @ResponseBody
    @GetMapping("/ueditor/home/hello")
    public void hello() {
        System.out.println("hello");
    }

    /**
     * 上传UMeditor中的图片到临时文件夹
     */
    @RequestMapping("/ueditor/uploadDocsTemp")
    @ResponseBody
    public String uploadDocsTemp(MultipartFile upfile, HttpServletRequest request) throws IOException {
        String path = request.getServletContext().getRealPath("/") + "down/temp/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = upfile.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + fileType;
        File newFile = new File(dir, newFileName);
        newFileName = new String(newFileName.getBytes("UTF-8"), "ISO-8859-1");
        try {
            upfile.transferTo(newFile);
        } catch (IOException e) {
            return "{\"name\":\"" + upfile.getName() + "\", \"originalName\": \"" + upfile.getOriginalFilename() + "\", \"size\": " + upfile.getSize() + ", \"state\": \"" + "SUCCESS" + "\", \"type\": \"" + getFileExt(upfile.getOriginalFilename()) + "\", \"url\": \"" + "/down/temp/" + newFileName + "\"}";
        }
        System.out.println(newFile.getCanonicalPath());
        return "{\"name\":\"" + upfile.getName() + "\", \"originalName\": \"" + upfile.getOriginalFilename() + "\", \"size\": " + upfile.getSize() + ", \"state\": \"" + "SUCCESS" + "\", \"type\": \"" + getFileExt(upfile.getOriginalFilename()) + "\", \"url\": \"" + "/down/temp/" + newFileName + "\"}";
    }

    /**
     * 获取文件扩展名
     *
     * @return string
     */
    private String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }







    @RequestMapping(value = "/upload2")
    public void uploadImg2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        JSONObject obj1 = new JSONObject();
        obj1.put("error", 0);
        obj1.put("url", "http://csdnimg.cn/cdn/content-toolbar/csdn-logo.png?v=20200416.1");

        writer.println(obj1.toJSONString());
    }

    /**
     * 富文本框编辑-上传图片
     *
     * @param localUrl
     * @return
     * @throws IOException
     * @throws FileUploadException
     */
    @RequestMapping(value = "/upload")
    public void uploadImg(HttpServletRequest request, HttpServletResponse response) {
// 设置Response响应的编码
        response.setContentType("text/html; charset=UTF-8");

// 获取一个Response的Write对象
        PrintWriter writer = null;

        try {
            writer = response.getWriter();



// 文件保存目录路径
            String savePath = request.getServletContext().getRealPath("/") + "attached/";

// 文件保存目录URL
            String saveUrl = request.getContextPath() + "/attached/";

// 定义允许上传的文件扩展名
            HashMap<String, String> extMap = new HashMap<String, String>();
            extMap.put("image", "gif,jpg,jpeg,png,bmp");

// 最大文件大小
            long maxSize = 1000000;

// 判断是否是一个文件
            if (!ServletFileUpload.isMultipartContent(request)) {
                writer.println(getError("请选择文件。"));
                return;
            }

// 检查目录upload, 没有则创建一个
            File uploadDir = new File(savePath);
            if (!uploadDir.isDirectory()) {
                uploadDir.mkdirs();
            }

// 检查目录写权限
            if (!uploadDir.canWrite()) {
                writer.println(getError("上传目录没有写权限。"));
                return;
            }

            String dirName = request.getParameter("dir");
            if (dirName == null) {
                dirName = "image";
            }
            if (!extMap.containsKey(dirName)) {
                writer.println(getError("目录名不正确。"));
                return;
            }

// 创建文件夹
            savePath += dirName + "/";
            saveUrl += dirName + "/";
            File saveDirFile = new File(savePath);
            if (!saveDirFile.exists()) {
                saveDirFile.mkdirs();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String ymd = sdf.format(new Date());
            savePath += ymd + "/";
            saveUrl += ymd + "/";
            File dirFile = new File(savePath);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }

            DefaultMultipartHttpServletRequest servletRequest = (DefaultMultipartHttpServletRequest) request;
            Iterator<String> iterator = servletRequest.getFileNames();
            while (iterator.hasNext()) {
                MultipartFile file = servletRequest.getFile(iterator.next());
                String fileName = file.getOriginalFilename();

                // 检查文件大小
                if (file.getSize() > maxSize) {
                    writer.println(getError("上传文件大小超过限制。"));
                    return;
                }

                // 检查扩展名
                String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                if (!Arrays.<String>asList(extMap.get(dirName).split(",")).contains(fileExt)) {
                    writer.println(getError("上传文件扩展名是不允许的扩展名。\n只允许" + extMap.get(dirName) + "格式。"));
                    return;
                }

                // 以时间重新命名文件名
                String newFileName = fileName + UUID.randomUUID();

                File uploadedFile = new File(savePath, newFileName);
                file.transferTo(uploadedFile);

                JSONObject obj = new JSONObject();
                obj.put("error", 0);
                obj.put("url", saveUrl + newFileName);

                writer.println(obj.toJSONString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件失败!" + e.getMessage());
        } finally {
            // 将writer对象中的内容输出
            writer.flush();
            // 关闭writer对象
            writer.close();
        }
    }

    /**
     * 上传图片-响应错误信息
     *
     * @param message
     * @return
     * @author 戴飞
     */
    private String getError(String message) {
        JSONObject obj = new JSONObject();
        obj.put("error", 1);
        obj.put("message", message);

        return obj.toJSONString();
    }
}
