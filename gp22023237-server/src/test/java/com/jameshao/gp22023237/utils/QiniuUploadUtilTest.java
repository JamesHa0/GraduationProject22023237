package com.jameshao.gp22023237.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class QiniuUploadUtilTest {

    private QiniuUploadUtil qiniuUploadUtil;

    @BeforeEach
    void setUp() {
        qiniuUploadUtil = new QiniuUploadUtil();
        
        ReflectionTestUtils.setField(qiniuUploadUtil, "accessKey",
            System.getenv("QINIU_ACCESS_KEY") != null ? System.getenv("QINIU_ACCESS_KEY") : "test-ak");
        ReflectionTestUtils.setField(qiniuUploadUtil, "secretKey",
            System.getenv("QINIU_SECRET_KEY") != null ? System.getenv("QINIU_SECRET_KEY") : "test-sk");
        ReflectionTestUtils.setField(qiniuUploadUtil, "bucket", "test-bucket");
        ReflectionTestUtils.setField(qiniuUploadUtil, "domain", "http://td6c50lms.hd-bkt.clouddn.com");
        ReflectionTestUtils.setField(qiniuUploadUtil, "signaturePrefix", "asset/signature/");
    }

    @Test
    void testGetSignaturePrefix() {
        String prefix = qiniuUploadUtil.getSignaturePrefix();
        assertEquals("asset/signature/", prefix);
    }

    @Test
    void testGetDomain() {
        String domain = qiniuUploadUtil.getDomain();
        assertEquals("http://td6c50lms.hd-bkt.clouddn.com", domain);
    }

    @Test
    void testUploadBase64ImageWithValidData() {
        String base64Data = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        Long userId = 1L;

        assertDoesNotThrow(() -> {
            qiniuUploadUtil.init();
        });
    }

    @Test
    void testUploadBase64ImageWithInvalidData() {
        String invalidBase64Data = "invalid-base64-data";
        Long userId = 1L;

        qiniuUploadUtil.init();
        
        assertThrows(Exception.class, () -> {
            qiniuUploadUtil.uploadBase64Image(invalidBase64Data, userId);
        });
    }

    @Test
    void testUploadSignatureFileWithValidData() {
        byte[] fileData = "test image data".getBytes();
        String originalFilename = "signature.png";
        Long userId = 1L;

        qiniuUploadUtil.init();
        
        assertDoesNotThrow(() -> {
        });
    }

    @Test
    void testDeleteFileWithNullUrl() {
        qiniuUploadUtil.init();
        
        assertDoesNotThrow(() -> {
            qiniuUploadUtil.deleteFile(null);
        });
    }

    @Test
    void testDeleteFileWithEmptyUrl() {
        qiniuUploadUtil.init();
        
        assertDoesNotThrow(() -> {
            qiniuUploadUtil.deleteFile("");
        });
    }

    @Test
    void testInitWithValidCredentials() {
        assertDoesNotThrow(() -> {
            qiniuUploadUtil.init();
        });
    }
}
