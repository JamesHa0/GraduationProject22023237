package com.jameshao.gp22023237.controller.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import com.jameshao.gp22023237.utils.QiniuUploadUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemUserController.class)
class SystemUserControllerSignatureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private QiniuUploadUtil qiniuUploadUtil;

    @MockBean
    private CurrentUserUtil currentUserUtil;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setName("测试用户");
        mockUser.setPhone("13800138000");
        mockUser.setEmail("test@example.com");
        mockUser.setGender(1);
        mockUser.setCreateTime(new Date());
        mockUser.setSignature(null);
    }

    @Test
    void testUploadSignatureBase64_Success() throws Exception {
        when(currentUserUtil.getCurrentUser()).thenReturn(mockUser);
        when(userService.getById(anyLong())).thenReturn(mockUser);
        when(qiniuUploadUtil.uploadBase64Image(anyString(), anyLong()))
                .thenReturn("http://td6c50lms.hd-bkt.clouddn.com/asset/signature/signature_1_123456.png");
        when(userService.updateSignature(anyLong(), anyString(), any(Date.class)))
                .thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("signature", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        mockMvc.perform(post("/system/user/profile/signature/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testUploadSignatureBase64_Unauthorized() throws Exception {
        when(currentUserUtil.getCurrentUser()).thenReturn(null);

        Map<String, String> request = new HashMap<>();
        request.put("signature", "data:image/png;base64,test");

        mockMvc.perform(post("/system/user/profile/signature/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testUploadSignatureBase64_EmptySignature() throws Exception {
        when(currentUserUtil.getCurrentUser()).thenReturn(mockUser);

        Map<String, String> request = new HashMap<>();
        request.put("signature", "");

        mockMvc.perform(post("/system/user/profile/signature/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testUploadSignatureFile_Success() throws Exception {
        when(currentUserUtil.getCurrentUser()).thenReturn(mockUser);
        when(userService.getById(anyLong())).thenReturn(mockUser);
        when(qiniuUploadUtil.uploadSignatureFile(any(), anyString(), anyLong()))
                .thenReturn("http://td6c50lms.hd-bkt.clouddn.com/asset/signature/signature_1_123456.png");
        when(userService.updateSignature(anyLong(), anyString(), any(Date.class)))
                .thenReturn(true);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "signature.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/system/user/profile/signature/file")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testUploadSignatureFile_Unauthorized() throws Exception {
        when(currentUserUtil.getCurrentUser()).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "signature.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/system/user/profile/signature/file")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testUploadSignatureFile_InvalidFileType() throws Exception {
        when(currentUserUtil.getCurrentUser()).thenReturn(mockUser);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "signature.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test text content".getBytes()
        );

        mockMvc.perform(multipart("/system/user/profile/signature/file")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testDeleteSignature_Success() throws Exception {
        mockUser.setSignature("http://td6c50lms.hd-bkt.clouddn.com/asset/signature/signature_1_123456.png");
        
        when(currentUserUtil.getCurrentUser()).thenReturn(mockUser);
        when(userService.getById(anyLong())).thenReturn(mockUser);
        when(userService.updateSignature(anyLong(), any(), any(Date.class)))
                .thenReturn(true);

        mockMvc.perform(delete("/system/user/profile/signature"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteSignature_Unauthorized() throws Exception {
        when(currentUserUtil.getCurrentUser()).thenReturn(null);

        mockMvc.perform(delete("/system/user/profile/signature"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testGetProfile_WithSignature() throws Exception {
        mockUser.setSignature("http://td6c50lms.hd-bkt.clouddn.com/asset/signature/signature_1_123456.png");
        
        when(currentUserUtil.getCurrentUser()).thenReturn(mockUser);
        when(userService.getById(anyLong())).thenReturn(mockUser);

        mockMvc.perform(get("/system/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.signature").value("http://td6c50lms.hd-bkt.clouddn.com/asset/signature/signature_1_123456.png"));
    }

    @Test
    void testGetProfile_WithoutSignature() throws Exception {
        when(currentUserUtil.getCurrentUser()).thenReturn(mockUser);
        when(userService.getById(anyLong())).thenReturn(mockUser);

        mockMvc.perform(get("/system/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.signature").isEmpty());
    }
}
