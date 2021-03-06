package com.xxx.web.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

@Slf4j
public class ResponseUtil {

    /**
     * 将文件写入response
     *
     * @param filePath, response
     * @return void
     **/
    public static void writeToResponse(String filePath, HttpServletResponse response) {
        ServletOutputStream out = null;
        FileInputStream ips = null;
        try {
            //获取图片存放路径
            ips = new FileInputStream(new File(filePath));
            response.setContentType("audio/mpeg");
            out = response.getOutputStream();

            IOUtils.copy(ips, out);

        } catch (Exception e) {
            log.error("文件读取错误！文件路径【{}】", filePath);
        }

    }

    /**
     * 支持录音断点续传
     *
     * @param filePath, response
     * @return void
     **/
    public static void breakPointWrite(HttpServletRequest request, HttpServletResponse response, String filePath) {
        File proposeFile = new File(filePath);
        log.info("录音文件路径：{}", proposeFile.getPath());
        InputStream inputStream = null;
        OutputStream bufferOut = null;
        try {
            // 设置响应报头
            long fSize = proposeFile.length();
            response.setContentType("application/x-download");
            // Content-Disposition: attachment; filename=WebGoat-OWASP_Developer-5.2.zip
            response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(proposeFile.getName(), "utf-8"));
            // Accept-Ranges: bytes
            response.setHeader("Accept-Ranges", "bytes");
            long pos = 0, last = fSize - 1, sum = 0;//pos开始读取位置;  last最后读取位置;  sum记录总共已经读取了多少字节
            if (null != request.getHeader("Range")) {
                // 断点续传
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                try {
                    // 情景一：RANGE: bytes=2000070- 情景二：RANGE: bytes=2000070-2000970
                    String numRang = request.getHeader("Range").replaceAll("bytes=", "");
                    String[] strRange = numRang.split("-");
                    if (strRange.length == 2) {
                        pos = Long.parseLong(strRange[0].trim());
                        last = Long.parseLong(strRange[1].trim());
                    } else {
                        pos = Long.parseLong(numRang.replaceAll("-", "").trim());
                    }
                } catch (NumberFormatException e) {
                    log.error("{} is not Number!", request.getHeader("Range"));
                    pos = 0;
                }
            }
            long rangLength = last - pos + 1;// 总共需要读取的字节
            // Content-Range: bytes 10-1033/304974592
            String contentRange = new StringBuffer("bytes ").append(pos).append("-").append(last).append("/").append(fSize).toString();
            response.setHeader("Content-Range", contentRange);
            // Content-Length: 1024
            response.addHeader("Content-Length", String.valueOf(rangLength));

            // 跳过已经下载的部分，进行后续下载
            bufferOut = new BufferedOutputStream(response.getOutputStream());
            inputStream = new BufferedInputStream(new FileInputStream(proposeFile));
            inputStream.skip(pos);

//            byte[] buffer = new byte[1024];
//            int length = 0;
//            while (sum < rangLength) {
//                length = inputStream.read(buffer, 0, ((rangLength - sum) <= buffer.length ? ((int) (rangLength - sum)) : buffer.length));
//                sum = sum + length;
//                bufferOut.write(buffer, 0, length);
//            }
            IOUtils.copy(inputStream, bufferOut);
        } catch (Throwable e) {
            if (e instanceof ClientAbortException) {
                // 浏览器点击取消
                log.info("用户取消下载!");
            } else {
                log.info("下载文件失败....");
                e.printStackTrace();
            }
        } finally {
            try {
                if (bufferOut != null) {
                    bufferOut.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
