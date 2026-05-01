package com.jameshao.gp22023237.controller;

import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import com.jameshao.gp22023237.utils.QiniuUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private QiniuUploadUtil qiniuUploadUtil;

    // 文件上传保存的路径（可配置）
    private static final String UPLOAD_PATH = "upload/";

    // 允许上传的文件类型
    private static final String[] ALLOWED_EXTENSIONS = {
        "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf",
        "txt", "zip", "rar", "7z", "jpg", "jpeg", "png", "gif"
    };

    // 最大文件大小：20MB
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    @Log(title = "文件管理", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return jsonReturn.returnFailed("文件不能为空");
            }

            // 确保上传目录存在
            File uploadDir = new File(UPLOAD_PATH);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".dat";
            String fileName = UUID.randomUUID().toString() + extension;

            // 保存文件
            File destFile = new File(UPLOAD_PATH + fileName);
            file.transferTo(destFile);

            // 返回文件路径
            String fileUrl = "/upload/" + fileName;
            return jsonReturn.returnSuccess(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return jsonReturn.returnError("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("上传失败: " + e.getMessage());
        }
    }

    /**
     * 学术附件上传至七牛云
     */
    @Log(title = "学术附件上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload-academic")
    public String uploadAcademicFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "originalFileName", required = false) String originalFileName) {
        try {
            if (file.isEmpty()) {
                return jsonReturn.returnFailed("文件不能为空");
            }

            // 文件大小校验
            if (file.getSize() > MAX_FILE_SIZE) {
                return jsonReturn.returnFailed("文件大小不能超过20MB");
            }

            // 优先使用前端额外传递的originalFileName（避免multipart中文文件名乱码）
            String originalFilename = (originalFileName != null && !originalFileName.isEmpty())
                    ? originalFileName
                    : file.getOriginalFilename();
            if (originalFilename != null && !isAllowedExtension(originalFilename)) {
                return jsonReturn.returnFailed("不支持的文件类型，允许上传：doc/docx/xls/xlsx/ppt/pptx/pdf/txt/zip/rar/7z/jpg/jpeg/png/gif");
            }

            // 获取当前用户ID
            Long userId = CurrentUserUtil.getCurrentUserId();
            if (userId == null) {
                userId = 0L;
            }

            // 上传至七牛云
            String fileUrl = qiniuUploadUtil.uploadAcademicFile(
                file.getBytes(),
                originalFilename,
                userId
            );

            // 返回文件URL
            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", originalFilename);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除七牛云学术附件
     */
    @Log(title = "学术附件删除", businessType = BusinessType.DELETE)
    @PostMapping("/delete-academic")
    public String deleteAcademicFile(@RequestParam("fileUrl") String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return jsonReturn.returnFailed("文件URL不能为空");
            }
            qiniuUploadUtil.deleteFile(fileUrl);
            return jsonReturn.returnSuccess("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("删除失败: " + e.getMessage());
        }
    }

    private boolean isAllowedExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取七牛云私有文件的签名访问URL
     * @param fileUrl 文件的公开URL
     * @return 带签名的私有访问URL
     */
    @GetMapping("/signed-url")
    public String getSignedUrl(@RequestParam("fileUrl") String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return jsonReturn.returnFailed("文件URL不能为空");
            }
            String signedUrl = qiniuUploadUtil.getPrivateUrl(fileUrl, 3600);
            Map<String, Object> result = new HashMap<>();
            result.put("url", signedUrl);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("获取签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 代理下载七牛云私有文件，通过后端转发并设置Content-Disposition指定原文件名
     * 解决七牛attname参数对中文文件名支持不佳的问题
     * @param fileUrl 文件的公开URL
     * @param fileName 下载时显示的文件名（可选）
     */
    @Log(title = "文件下载", businessType = BusinessType.EXPORT)
    @GetMapping("/download")
    public void downloadFile(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam(value = "fileName", required = false) String fileName,
            HttpServletResponse response) {
        HttpURLConnection conn = null;
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                response.sendError(400, "文件URL不能为空");
                return;
            }
            // 生成七牛签名URL
            String signedUrl = qiniuUploadUtil.getPrivateUrl(fileUrl, 3600);
            if (signedUrl == null || signedUrl.isEmpty()) {
                response.sendError(500, "生成签名URL失败");
                return;
            }

            // 从七牛CDN下载文件
            URL url = java.net.URI.create(signedUrl).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(60000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                response.sendError(502, "从存储服务获取文件失败");
                return;
            }

            // 获取CDN响应的Content-Type
            String contentType = conn.getContentType();
            int contentLength = conn.getContentLength();

            if (contentType != null) {
                response.setContentType(contentType);
            } else {
                response.setContentType("application/octet-stream");
            }
            if (contentLength > 0) {
                response.setContentLengthLong(contentLength);
            }

            // 设置Content-Disposition，用filename*支持中文文件名
            if (fileName != null && !fileName.isEmpty()) {
                String encodedName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
                // 同时提供filename（兼容旧浏览器）和filename*（RFC 5987标准，支持UTF-8）
                String asciiName = fileName.replaceAll("[^\\x00-\\x7F]", "_");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encodedName);
            } else {
                response.setHeader("Content-Disposition", "attachment");
            }

            // 流式传输文件内容
            try (InputStream is = conn.getInputStream();
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    os.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(500, "下载失败: " + e.getMessage());
            } catch (IOException ignored) {}
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
