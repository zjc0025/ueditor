package com.xxx.web.controller;

import com.xxx.web.utils.Base64Utils;
import com.xxx.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName UeditorController
 * @Description Ueditor上传
 * @Author ZJC
 * @Date 2020/11/19 8:25
 */
@Slf4j
@Controller
public class UeditorController {

    /**
     * 上传配置：即不走config.json，模拟config.json里的内容，解决后端配置项不正确，无法上传的问题
     *
     * @return string
     */
    @RequestMapping("/ueditor/config")
    @ResponseBody
    public String uploadConfig() throws IOException {
        //读取配置文件
        ClassPathResource classPathResource = new ClassPathResource("static/ueditor/jsp/config2.json");
        InputStream fis = classPathResource.getInputStream();
        StringWriter writer = new StringWriter();
        IOUtils.copy(fis, writer, StandardCharsets.UTF_8.name());
//        String str = "{\n" +
//                "            \"imageActionName\": \"uploadimage\",\n" +
//                "                \"imageFieldName\": \"upfile\", \n" +
//                "                \"imageMaxSize\": 2048000, \n" +
//                "                \"imageAllowFiles\": [\".png\", \".jpg\", \".jpeg\", \".gif\", \".bmp\"], \n" +
//                "                \"imageCompressEnable\": true, \n" +
//                "                \"imageCompressBorder\": 1600, \n" +
//                "                \"imageInsertAlign\": \"none\", \n" +
//                "                \"imageUrlPrefix\": \"http://localhost:8100\",\n" +
//                "                \"imagePathFormat\": \"/ueditor/jsp/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\" }";
        return writer.toString();
    }

    /**
     * @param upfile
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @author ZJC
     * @Description 上传图片
     * @Date 2020/11/19 15:22
     **/
    @ResponseBody
    @PostMapping("/uploadImage")
    public Map<String, String> uploadImage(MultipartFile upfile) throws IOException {
        String oldName = upfile.getOriginalFilename();
        String newName = UUID.randomUUID().toString();
        newName += oldName.substring(oldName.lastIndexOf("."));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dataPath = sdf.format(new Date());

        File parentDir = new File("D:\\sound-path" + File.separator + dataPath);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        upfile.transferTo(new File("D:\\sound-path" + File.separator + dataPath + File.separator + newName));

        Map<String, String> map = new HashMap<>();
        map.put("state", "SUCCESS");
        map.put("url", "/getImg?imgId=123");
        map.put("title", newName);
        map.put("original", newName);
        return map;

    }

    /**
     * @param imgId, response
     * @return void
     * @author ZJC
     * @Description 获取图片（回显图片）
     * @Date 2020/11/19 15:22
     **/
    @ResponseBody
    @GetMapping("/getImg")
    public void getImg(String imgId, HttpServletResponse response) {
        //todo: 根据图片id获取图片路径
        log.info("根据图片id获取图片路径【{}】", imgId);

        ServletOutputStream out = null;
        FileInputStream ips = null;
        String path = null;
        try {
            File directory = new File("");//参数为空
            String courseFile = directory.getCanonicalPath();//标准的路径

            path = courseFile + "/web/src/main/resources/test-file/111.jpg";
            ips = new FileInputStream(new File(path));

            out = response.getOutputStream();

            IOUtils.copy(ips, out);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件读取错误！文件路径【{}】", path);
        }
    }

    /**
     * @param content, response
     * @return void
     * @author ZJC
     * @Description html转word下载
     * @Date 2020/11/19 15:22
     **/
    @GetMapping("/downloadReport")
    public void downloadReport(String content, HttpServletResponse response) throws IOException {
//        InputStream in = this.getClass().getResourceAsStream("/static/pages/liuDiaopage/liudiaoDetail/themes/iframe-word.css");
//        BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader(in));
//        //样式字符串
//        StringBuilder styleStr = new StringBuilder();
//        String sTempOneLine;
//        while ((sTempOneLine = tBufferedReader.readLine()) != null) {
//            styleStr.append(sTempOneLine);
//        }

//        response.setCharacterEncoding("utf-8");

        content = "<p><img src=\"http://localhost:8100/getImg?imgId=123\" title=\"f015054c-2041-402c-90d0-caa903448098.png\"/></p><p><img src=\"http://localhost:8100/getImg?imgId=123\" title=\"2ed1ce57-f66e-475a-9135-abf7663d12e3.jpg\"/></p><p>&nbsp; &nbsp; &nbsp; &nbsp;这里写你的初始化内容\n" +
                " &nbsp; &nbsp;<br/></p>";

        //创建 POIFSFileSystem 对象
        POIFSFileSystem poifs = new POIFSFileSystem();
        //获取DirectoryEntry
        DirectoryEntry directory = poifs.getRoot();
        //创建输出流
        OutputStream out = response.getOutputStream();
        response.setContentType("application/msword");
        response.setHeader("Content-Disposition", "filename=" + UUID.randomUUID() + ".doc");

        byte[] b = ("<html><style>" + "" + "</style><body>" + content + "</body></html>").getBytes("gbk");

        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        //创建文档,1.格式,2.HTML文件输入流
        directory.createDocument("WordDocument", bais);
        //写入
        poifs.writeFilesystem(response.getOutputStream());
        //释放资源
        out.close();
    }

    /**
     * @param upfile
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @author ZJC
     * @Description 上传视频
     * @Date 2020/11/19 15:23
     **/
    @ResponseBody
    @PostMapping("/uploadVideo")
    public Map<String, String> uploadVideo(MultipartFile upfile) throws IOException {
        String oldName = upfile.getOriginalFilename();
        String newName = UUID.randomUUID().toString();
        newName += oldName.substring(oldName.lastIndexOf("."));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dataPath = sdf.format(new Date());

        File parentDir = new File("D:\\sound-path" + File.separator + dataPath);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        upfile.transferTo(new File("D:\\sound-path" + File.separator + dataPath + File.separator + newName));

        Map<String, String> map = new HashMap<>();
        map.put("state", "SUCCESS");
        map.put("url", "/getVideo?imgId=123");
        map.put("title", newName);
        map.put("original", newName);
        return map;

    }

    /**
     * @param fileId, response
     * @return void
     * @author ZJC
     * @Description 获取视频（用于回显）
     * @Date 2020/11/19 15:24
     **/
    @ResponseBody
    @GetMapping("/getVideo")
    public void getVideo(String fileId, HttpServletRequest request, HttpServletResponse response) {
//        response.setContentType("video/mp4");
        //todo: 根据文件id获取路径(查数据库等)
        log.info("根据文件id获取图片路径【{}】", fileId);

        String path = null;
        try {
            File directory = new File("");//参数为空
            String courseFile = directory.getCanonicalPath();//标准的路径

            path = courseFile + "/web/src/main/resources/test-file/111.mp4";
//            path = courseFile + "/web/src/main/resources/test-file/222.mp3";

            ResponseUtil.breakPointWrite(request, response, path);

        } catch (Exception e) {
            log.error("文件读取错误！文件路径【{}】", path);
        }
    }


    /**
     * @param upfile
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @author ZJC
     * @Description 上传涂鸦 涂鸦是base64字符串
     * @Date 2020/11/19 15:22
     **/
    @ResponseBody
    @PostMapping("/uploadScrawl")
    public Map<String, String> uploadScrawl(String upfile) throws IOException {

        String newName = UUID.randomUUID().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dataPath = sdf.format(new Date());

        File parentDir = new File("D:\\sound-path" + File.separator + dataPath);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        Base64Utils.generateImage(upfile, "D:\\sound-path" + File.separator + dataPath + File.separator + newName + ".jpg");

        Map<String, String> map = new HashMap<>();
        map.put("state", "SUCCESS");
        map.put("url", "/getScrawl?imgId=123");
        map.put("title", newName);
        map.put("original", newName);
        return map;

    }

    /**
     * @param imgId, response
     * @return void
     * @author ZJC
     * @Description 获取图片（回显图片）
     * @Date 2020/11/19 15:22
     **/
    @ResponseBody
    @GetMapping("/getScrawl")
    public void getScrawl(String imgId, HttpServletResponse response) {
        //todo: 根据图片id获取图片路径
        log.info("根据图片id获取图片路径【{}】", imgId);

        ServletOutputStream out = null;
        FileInputStream ips = null;
        String path = null;
        try {
            File directory = new File("");//参数为空
            String courseFile = directory.getCanonicalPath();//标准的路径

            path = courseFile + "/web/src/main/resources/test-file/111.jpg";
            ips = new FileInputStream(new File(path));

            out = response.getOutputStream();

            IOUtils.copy(ips, out);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件读取错误！文件路径【{}】", path);
        }
    }
}
