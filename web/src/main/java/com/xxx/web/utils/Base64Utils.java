package com.xxx.web.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;

/**
 * @ClassName Base64Utils
 * @Description
 * @Author ZJC
 * @Date 2020/11/20 8:36
 */
public class Base64Utils {

    /**
     * @param [path]
     * @return java.lang.String
     * @author ZJC
     * @Description 图片转化成base64字符串
     * @Date 2020/11/20 8:38
     **/
    public static String getImageStr(String path) {
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);//返回Base64编码过的字节数组字符串
    }

    /**
     * @param [imgStr, path]
     * @return boolean
     * @author ZJC
     * @Description base64字符串转化成图片
     * @Date 2020/11/20 8:38
     **/
    public static boolean generateImage(String imgStr, String path) {
        if (imgStr == null) //图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }
            //生成jpeg图片
            //String imgFilePath = "D:\\tupian\\new.jpg";//新生成的图片
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
