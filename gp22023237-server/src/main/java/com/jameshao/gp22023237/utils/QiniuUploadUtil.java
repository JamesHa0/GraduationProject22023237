package com.jameshao.gp22023237.utils;

import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;

import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Base64;

@Component
public class QiniuUploadUtil {

    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    @Value("${qiniu.signature-prefix}")
    private String signaturePrefix;

    @Value("${qiniu.avatar-prefix:asset/avatar/}")
    private String avatarPrefix;

    @Value("${qiniu.academic-prefix:resource/academic/}")
    private String academicPrefix;

    private Auth auth;
    private UploadManager uploadManager;
    private BucketManager bucketManager;

    @PostConstruct
    public void init() {
        auth = Auth.create(accessKey, secretKey);
        Configuration cfg = new Configuration(Region.autoRegion());
        uploadManager = new UploadManager(cfg);
        bucketManager = new BucketManager(auth, cfg);
    }

    public String uploadFile(byte[] data, String fileName) throws Exception {
        String upToken = auth.uploadToken(bucket);
        Response response = uploadManager.put(data, fileName, upToken);
        if (response.isOK()) {
            return domain + "/" + fileName;
        } else {
            throw new RuntimeException("上传失败: " + response.toString());
        }
    }

    public String uploadBase64Image(String base64Data, Long userId) throws Exception {
        String base64Image = base64Data;
        if (base64Data.contains(",")) {
            base64Image = base64Data.split(",")[1];
        }

        byte[] data = Base64.getDecoder().decode(base64Image);
        String fileName = signaturePrefix + "signature_" + userId + "_" + System.currentTimeMillis() + ".png";

        return uploadFile(data, fileName);
    }

    public String uploadSignatureFile(byte[] fileData, String originalFilename, Long userId) throws Exception {
        String extension = ".png";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = signaturePrefix + "signature_" + userId + "_" + System.currentTimeMillis() + extension;

        return uploadFile(fileData, fileName);
    }

    public String uploadAvatarBase64(String base64Data, Long userId) throws Exception {
        String base64Image = base64Data;
        if (base64Data.contains(",")) {
            base64Image = base64Data.split(",")[1];
        }

        byte[] data = Base64.getDecoder().decode(base64Image);
        String fileName = avatarPrefix + "avatar_" + userId + "_" + System.currentTimeMillis() + ".png";

        return uploadFile(data, fileName);
    }

    public String uploadAvatarFile(byte[] fileData, String originalFilename, Long userId) throws Exception {
        String extension = ".png";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = avatarPrefix + "avatar_" + userId + "_" + System.currentTimeMillis() + extension;

        return uploadFile(fileData, fileName);
    }

    /**
     * 上传学术相关附件到七牛云
     * @param fileData 文件字节数据
     * @param originalFilename 原始文件名
     * @param userId 上传用户ID
     * @return 文件访问URL
     */
    public String uploadAcademicFile(byte[] fileData, String originalFilename, Long userId) throws Exception {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = academicPrefix + "academic_" + userId + "_" + System.currentTimeMillis() + extension;

        return uploadFile(fileData, fileName);
    }

    public void deleteFile(String fileUrl) throws Exception {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        String key = fileUrl.replace(domain + "/", "");
        bucketManager.delete(bucket, key);
    }

    public String getSignaturePrefix() {
        return signaturePrefix;
    }

    public String getDomain() {
        return domain;
    }

    /**
     * 生成私有签名URL，用于访问私有空间的文件
     * @param publicUrl 公开访问URL
     * @param expires 过期时间（秒），默认3600秒
     * @return 带签名的私有URL
     */
    public String getPrivateUrl(String publicUrl, long expires) {
        if (publicUrl == null || publicUrl.isEmpty()) {
            return null;
        }
        return auth.privateDownloadUrl(publicUrl, expires);
    }

    /**
     * 生成私有签名URL（带自定义下载文件名）
     * 注意：attname参数在七牛CDN对中文文件名支持不佳，已改用后端代理下载方式指定文件名
     * @param publicUrl 公开访问URL
     * @param expires 过期时间（秒）
     * @param attname 下载时显示的文件名（已弃用，仅保留接口兼容）
     * @return 带签名的私有URL
     */
    public String getPrivateUrl(String publicUrl, long expires, String attname) {
        // attname参数已弃用，直接使用基础签名方式
        return getPrivateUrl(publicUrl, expires);
    }

    /**
     * 生成私有签名URL，默认1小时过期
     */
    public String getPrivateUrl(String publicUrl) {
        return getPrivateUrl(publicUrl, 3600);
    }
}
