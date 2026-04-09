package com.jameshao.gp22023237.controller.system;

import com.jameshao.gp22023237.DTO.UserAvatarResponseDTO;
import com.jameshao.gp22023237.DTO.UserPasswordUpdateDTO;
import com.jameshao.gp22023237.DTO.UserProfileDTO;
import com.jameshao.gp22023237.DTO.UserProfileUpdateDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import com.jameshao.gp22023237.utils.QiniuUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/system/user")
public class SystemUserController {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ILLEGAL_PASSWORD_PATTERN = Pattern.compile("[<>\"'|\\\\]");

    @Autowired
    private UserService userService;
    @Autowired
    private JSONReturn jsonReturn;
    @Autowired
    private QiniuUploadUtil qiniuUploadUtil;

    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize, String userName, String phonenumber, String status) {
        try {
            Page<User> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(userName), User::getName, userName)
                    .like(!ObjectUtils.isEmpty(phonenumber), User::getPhone, phonenumber)
                    .eq(!ObjectUtils.isEmpty(status), User::getStatus, status);
            Page<User> result = userService.page(page, queryWrapper);
            Map<String, Object> data = new HashMap<>();
            data.put("rows", result.getRecords());
            data.put("total", result.getTotal());
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @RequestMapping("/{userId}")
    public String getInfo(@PathVariable Long userId) {
        try {
            User user = userService.getById(userId);
            return jsonReturn.returnSuccess(user);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping
    public String add(@RequestBody User user) {
        try {
            LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(User::getUsername, user.getUsername());
            User existUser = userService.getOne(checkWrapper);
            if (existUser != null) {
                return jsonReturn.returnFailed("用户名已存在");
            }
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            userService.save(user);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping
    public String edit(@RequestBody User user) {
        try {
            user.setUpdateTime(new Date());
            userService.updateById(user);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @DeleteMapping("/{userIds}")
    public String remove(@PathVariable Long[] userIds) {
        try {
            for (Long userId : userIds) {
                userService.removeById(userId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping("/changeStatus")
    public String changeStatus(@RequestBody User user) {
        try {
            user.setUpdateTime(new Date());
            userService.updateById(user);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping("/resetPwd")
    public String resetPwd(@RequestBody User user) {
        try {
            user.setUpdateTime(new Date());
            userService.updateById(user);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public String profile() {
        try {
            User loginUser = CurrentUserUtil.getCurrentUser();
            if (loginUser == null || loginUser.getId() == null) {
                return jsonReturn.returnFailed("未登录或登录状态已失效");
            }
            User dbUser = userService.getById(loginUser.getId());
            if (dbUser == null) {
                return jsonReturn.returnFailed("用户不存在");
            }

            UserProfileDTO profileDTO = new UserProfileDTO();
            profileDTO.setUserId(dbUser.getId());
            profileDTO.setUsername(dbUser.getUsername());
            profileDTO.setName(dbUser.getName());
            profileDTO.setPhone(dbUser.getPhone());
            profileDTO.setEmail(dbUser.getEmail());
            profileDTO.setGender(dbUser.getGender());
            profileDTO.setCreateTime(dbUser.getCreateTime());
            if (!ObjectUtils.isEmpty(dbUser.getAvatar())) {
                profileDTO.setAvatar(dbUser.getAvatar());
            } else {
                profileDTO.setAvatar("/profile/avatar/placeholder/" + dbUser.getId());
            }
            // 签名URL需要生成私有签名，否则无法访问
            if (!ObjectUtils.isEmpty(dbUser.getSignature())) {
                profileDTO.setSignature(qiniuUploadUtil.getPrivateUrl(dbUser.getSignature()));
            }
            return jsonReturn.returnSuccess(profileDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public String updateProfile(@RequestBody UserProfileUpdateDTO updateDTO) {
        try {
            User loginUser = CurrentUserUtil.getCurrentUser();
            if (loginUser == null || loginUser.getId() == null) {
                return jsonReturn.returnFailed("未登录或登录状态已失效");
            }
            User dbUser = userService.getById(loginUser.getId());
            if (dbUser == null) {
                return jsonReturn.returnFailed("用户不存在");
            }
            if (updateDTO == null) {
                return jsonReturn.returnFailed("请求参数不能为空");
            }
            if (ObjectUtils.isEmpty(updateDTO.getName())) {
                return jsonReturn.returnFailed("姓名不能为空");
            }
            if (ObjectUtils.isEmpty(updateDTO.getPhone()) || !PHONE_PATTERN.matcher(updateDTO.getPhone()).matches()) {
                return jsonReturn.returnFailed("请输入正确的手机号码");
            }
            if (ObjectUtils.isEmpty(updateDTO.getEmail()) || !updateDTO.getEmail().contains("@")) {
                return jsonReturn.returnFailed("请输入正确的邮箱地址");
            }
            Integer gender = updateDTO.getGender();
            if (gender != null && gender != 1 && gender != 2) {
                return jsonReturn.returnFailed("性别参数非法");
            }

            Date now = new Date();
            boolean updated = userService.updateProfileFields(
                    dbUser.getId(),
                    updateDTO.getName(),
                    updateDTO.getPhone(),
                    updateDTO.getEmail(),
                    gender,
                    now
            );
            if (!updated) {
                return jsonReturn.returnFailed("更新个人资料失败");
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping("/profile/updatePwd")
    public String updatePwd(@RequestBody UserPasswordUpdateDTO passwordDTO) {
        try {
            User loginUser = CurrentUserUtil.getCurrentUser();
            if (loginUser == null || loginUser.getId() == null) {
                return jsonReturn.returnFailed("未登录或登录状态已失效");
            }
            User dbUser = userService.getById(loginUser.getId());
            if (dbUser == null) {
                return jsonReturn.returnFailed("用户不存在");
            }
            if (passwordDTO == null) {
                return jsonReturn.returnFailed("请求参数不能为空");
            }
            if (ObjectUtils.isEmpty(passwordDTO.getOldPassword()) || ObjectUtils.isEmpty(passwordDTO.getNewPassword())) {
                return jsonReturn.returnFailed("旧密码和新密码不能为空");
            }
            if (!passwordDTO.getOldPassword().equals(dbUser.getPassword())) {
                return jsonReturn.returnFailed("旧密码错误");
            }
            String newPassword = passwordDTO.getNewPassword();
            if (newPassword.length() < 6 || newPassword.length() > 20 || ILLEGAL_PASSWORD_PATTERN.matcher(newPassword).find()) {
                return jsonReturn.returnFailed("新密码长度需在6到20之间且不能包含非法字符");
            }
            if (newPassword.equals(passwordDTO.getOldPassword())) {
                return jsonReturn.returnFailed("新密码不能与旧密码相同");
            }

            boolean updated = userService.updatePassword(dbUser.getId(), newPassword, new Date());
            if (!updated) {
                return jsonReturn.returnFailed("密码更新失败");
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam(value = "avatarfile", required = false) MultipartFile avatarFile) {
        try {
            User loginUser = CurrentUserUtil.getCurrentUser();
            if (loginUser == null || loginUser.getId() == null) {
                return jsonReturn.returnFailed("未登录或登录状态已失效");
            }
            if (avatarFile == null || avatarFile.isEmpty()) {
                return jsonReturn.returnFailed("头像文件不能为空");
            }

            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return jsonReturn.returnFailed("只能上传图片文件");
            }

            long maxSize = 2 * 1024 * 1024;
            if (avatarFile.getSize() > maxSize) {
                return jsonReturn.returnFailed("文件大小不能超过2MB");
            }

            User dbUser = userService.getById(loginUser.getId());
            if (dbUser == null) {
                return jsonReturn.returnFailed("用户不存在");
            }

            if (!ObjectUtils.isEmpty(dbUser.getAvatar())) {
                try {
                    qiniuUploadUtil.deleteFile(dbUser.getAvatar());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String avatarUrl = qiniuUploadUtil.uploadAvatarFile(
                    avatarFile.getBytes(),
                    avatarFile.getOriginalFilename(),
                    loginUser.getId()
            );

            boolean updated = userService.updateAvatar(loginUser.getId(), avatarUrl, new Date());
            if (!updated) {
                return jsonReturn.returnFailed("头像保存失败");
            }

            UserAvatarResponseDTO responseDTO = new UserAvatarResponseDTO();
            responseDTO.setImgUrl(avatarUrl);
            return jsonReturn.returnSuccess(responseDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/profile/avatar/base64")
    public String uploadAvatarBase64(@RequestBody Map<String, String> params) {
        try {
            User loginUser = CurrentUserUtil.getCurrentUser();
            if (loginUser == null || loginUser.getId() == null) {
                return jsonReturn.returnFailed("未登录或登录状态已失效");
            }

            String base64Data = params.get("avatar");
            if (ObjectUtils.isEmpty(base64Data)) {
                return jsonReturn.returnFailed("头像数据不能为空");
            }

            User dbUser = userService.getById(loginUser.getId());
            if (dbUser == null) {
                return jsonReturn.returnFailed("用户不存在");
            }

            if (!ObjectUtils.isEmpty(dbUser.getAvatar())) {
                try {
                    qiniuUploadUtil.deleteFile(dbUser.getAvatar());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String avatarUrl = qiniuUploadUtil.uploadAvatarBase64(base64Data, loginUser.getId());

            boolean updated = userService.updateAvatar(loginUser.getId(), avatarUrl, new Date());
            if (!updated) {
                return jsonReturn.returnFailed("头像保存失败");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/profile/signature/base64")
    public String uploadSignatureBase64(@RequestBody Map<String, String> params) {
        try {
            User loginUser = CurrentUserUtil.getCurrentUser();
            if (loginUser == null || loginUser.getId() == null) {
                return jsonReturn.returnFailed("未登录或登录状态已失效");
            }

            String base64Data = params.get("signature");
            if (ObjectUtils.isEmpty(base64Data)) {
                return jsonReturn.returnFailed("签名数据不能为空");
            }

            User dbUser = userService.getById(loginUser.getId());
            if (dbUser == null) {
                return jsonReturn.returnFailed("用户不存在");
            }

            if (!ObjectUtils.isEmpty(dbUser.getSignature())) {
                try {
                    qiniuUploadUtil.deleteFile(dbUser.getSignature());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String signatureUrl = qiniuUploadUtil.uploadBase64Image(base64Data, loginUser.getId());

            boolean updated = userService.updateSignature(loginUser.getId(), signatureUrl, new Date());
            if (!updated) {
                return jsonReturn.returnFailed("签名保存失败");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("signatureUrl", signatureUrl);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/profile/signature/file")
    public String uploadSignatureFile(@RequestParam("file") MultipartFile file) {
        try {
            User loginUser = CurrentUserUtil.getCurrentUser();
            if (loginUser == null || loginUser.getId() == null) {
                return jsonReturn.returnFailed("未登录或登录状态已失效");
            }

            if (file == null || file.isEmpty()) {
                return jsonReturn.returnFailed("签名文件不能为空");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return jsonReturn.returnFailed("只能上传图片文件");
            }

            long maxSize = 2 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return jsonReturn.returnFailed("文件大小不能超过2MB");
            }

            User dbUser = userService.getById(loginUser.getId());
            if (dbUser == null) {
                return jsonReturn.returnFailed("用户不存在");
            }

            if (!ObjectUtils.isEmpty(dbUser.getSignature())) {
                try {
                    qiniuUploadUtil.deleteFile(dbUser.getSignature());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String signatureUrl = qiniuUploadUtil.uploadSignatureFile(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    loginUser.getId()
            );

            boolean updated = userService.updateSignature(loginUser.getId(), signatureUrl, new Date());
            if (!updated) {
                return jsonReturn.returnFailed("签名保存失败");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("signatureUrl", signatureUrl);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @DeleteMapping("/profile/signature")
    public String deleteSignature() {
        try {
            User loginUser = CurrentUserUtil.getCurrentUser();
            if (loginUser == null || loginUser.getId() == null) {
                return jsonReturn.returnFailed("未登录或登录状态已失效");
            }

            User dbUser = userService.getById(loginUser.getId());
            if (dbUser == null) {
                return jsonReturn.returnFailed("用户不存在");
            }

            if (!ObjectUtils.isEmpty(dbUser.getSignature())) {
                try {
                    qiniuUploadUtil.deleteFile(dbUser.getSignature());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            boolean updated = userService.updateSignature(loginUser.getId(), null, new Date());
            if (!updated) {
                return jsonReturn.returnFailed("签名删除失败");
            }

            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @RequestMapping("/listAll")
    public String listAll() {
        try {
            List<User> list = userService.list();
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
