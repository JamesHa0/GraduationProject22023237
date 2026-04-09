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
import java.util.UUID;

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
}
