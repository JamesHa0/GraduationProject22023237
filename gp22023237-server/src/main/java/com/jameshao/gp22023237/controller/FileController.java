package com.jameshao.gp22023237.controller;

import com.jameshao.gp22023237.common.JSONReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private JSONReturn jsonReturn;

    // 文件上传保存的路径（可配置）
    private static final String UPLOAD_PATH = "upload/";

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
}
