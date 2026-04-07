package com.jameshao.gp22023237.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    /**
     * 查询学生列表
     * @param studentNo 学号（可选）
     * @param studentName 学生姓名（可选）
     * @param status 状态（可选）1-在读，2-休学，3-毕业，4-退学
     * @param department 院系（可选）
     * @param major 专业（可选）
     * @return 学生列表
     */
    @GetMapping("/list")
    public String list(String studentNo, String studentName, Integer status, String department, String major) {
        try {
            QueryWrapper<Student> wrapper = new QueryWrapper<>();

            if (studentNo != null && !studentNo.isEmpty()) {
                wrapper.like("student_no", studentNo);
            }
            if (studentName != null && !studentName.isEmpty()) {
                wrapper.like("student_name", studentName);
            }
            if (status != null) {
                wrapper.eq("status", status);
            }
            if (department != null && !department.isEmpty()) {
                wrapper.like("department", department);
            }
            if (major != null && !major.isEmpty()) {
                wrapper.like("major", major);
            }

            wrapper.orderByDesc("create_time");
            List<Student> list = studentService.list(wrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取学生详情
     * @param id 学生ID
     * @return 学生详情
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            Student student = studentService.getById(id);
            if (student == null) {
                return jsonReturn.returnFailed("学生不存在");
            }
            return jsonReturn.returnSuccess(student);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 根据学号获取学生信息
     * @param studentNo 学号
     * @return 学生信息
     */
    @GetMapping("/byNo/{studentNo}")
    public String getByNo(@PathVariable String studentNo) {
        try {
            QueryWrapper<Student> wrapper = new QueryWrapper<>();
            wrapper.eq("student_no", studentNo);
            Student student = studentService.getOne(wrapper);
            if (student == null) {
                return jsonReturn.returnFailed("学生不存在");
            }
            return jsonReturn.returnSuccess(student);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增学生
     * @param student 学生信息
     * @return 操作结果
     */
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public String add(@RequestBody Student student) {
        try {
            // 检查学号是否已存在
            QueryWrapper<Student> wrapper = new QueryWrapper<>();
            wrapper.eq("student_no", student.getStudentNo());
            Student existStudent = studentService.getOne(wrapper);
            if (existStudent != null) {
                return jsonReturn.returnFailed("学号已存在");
            }

            // 检查用户名（学号）是否已存在
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("username", student.getStudentNo());
            User existUser = userService.getOne(userWrapper);
            if (existUser != null) {
                return jsonReturn.returnFailed("学号已存在（用户账号）");
            }

            // 创建用户账号
            User user = new User();
            user.setUsername(student.getStudentNo()); // 学号作为用户名
            user.setPassword(student.getStudentNo()); // 默认密码为学号
            user.setName(student.getStudentName());
            user.setRoleId(1); // 1-学生角色
            user.setStatus(1); // 默认为正常状态
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            boolean userSuccess = userService.save(user);
            if (!userSuccess) {
                return jsonReturn.returnFailed("创建用户账号失败");
            }

            // 设置创建时间和更新时间
            student.setCreateTime(new Date());
            student.setUpdateTime(new Date());
            // 设置关联的用户ID
            student.setUserId(user.getId());

            // 设置默认状态
            if (student.getStatus() == null) {
                student.setStatus(1); // 默认为在读状态
            }
            if (student.getSelectionStatus() == null) {
                student.setSelectionStatus(0); // 默认为未开始双选状态
            }

            boolean success = studentService.save(student);
            if (success) {
                return jsonReturn.returnSuccess("新增学生成功，默认密码为学号");
            } else {
                return jsonReturn.returnFailed("新增学生失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 更新学生信息
     * @param student 学生信息
     * @return 操作结果
     */
    @PutMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public String update(@RequestBody Student student) {
        try {
            if (student.getId() == null) {
                return jsonReturn.returnFailed("学生ID不能为空");
            }

            // 检查学生是否存在
            Student existStudent = studentService.getById(student.getId());
            if (existStudent == null) {
                return jsonReturn.returnFailed("学生不存在");
            }

            // 如果修改学号，检查新学号是否已存在
            if (student.getStudentNo() != null && !student.getStudentNo().equals(existStudent.getStudentNo())) {
                QueryWrapper<Student> wrapper = new QueryWrapper<>();
                wrapper.eq("student_no", student.getStudentNo());
                Student duplicateStudent = studentService.getOne(wrapper);
                if (duplicateStudent != null) {
                    return jsonReturn.returnFailed("学号已存在");
                }
            }

            // 设置更新时间
            student.setUpdateTime(new Date());

            // 更新学生信息
            boolean success = studentService.updateById(student);

            // 同步更新用户表（姓名）
            if (success && existStudent.getUserId() != null) {
                User user = userService.getById(existStudent.getUserId());
                if (user != null) {
                    if (student.getStudentName() != null) {
                        user.setName(student.getStudentName());
                    }
                    if (student.getStudentNo() != null) {
                        user.setUsername(student.getStudentNo());
                    }
                    user.setUpdateTime(new Date());
                    userService.updateById(user);
                }
            }

            if (success) {
                return jsonReturn.returnSuccess("更新学生成功");
            } else {
                return jsonReturn.returnFailed("更新学生失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除学生
     * @param id 学生ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public String delete(@PathVariable Long id) {
        try {
            Student student = studentService.getById(id);
            if (student == null) {
                return jsonReturn.returnFailed("学生不存在");
            }

            Long userId = student.getUserId();

            boolean success = studentService.removeById(id);

            // 同步删除用户账号
            if (success && userId != null) {
                userService.removeById(userId);
            }

            if (success) {
                return jsonReturn.returnSuccess("删除学生成功");
            } else {
                return jsonReturn.returnFailed("删除学生失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 批量删除学生
     * @param ids 学生ID列表
     * @return 操作结果
     */
    @DeleteMapping("/deleteBatch")
    @Transactional(rollbackFor = Exception.class)
    public String deleteBatch(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return jsonReturn.returnFailed("请选择要删除的学生");
            }

            // 先查询需要删除的学生，获取对应的userId
            List<Student> students = studentService.listByIds(ids);
            List<Long> userIds = students.stream()
                    .map(Student::getUserId)
                    .filter(userId -> userId != null)
                    .toList();

            boolean success = studentService.removeByIds(ids);

            // 同步删除用户账号
            if (success && !userIds.isEmpty()) {
                userService.removeByIds(userIds);
            }

            if (success) {
                return jsonReturn.returnSuccess("批量删除成功");
            } else {
                return jsonReturn.returnFailed("批量删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的学生信息
     * @return 当前学生信息
     */
    @GetMapping("/current")
    public String getCurrentStudentInfo() {
        try {
            Long userId = CurrentUserUtil.getCurrentUserId();
            if (userId == null) {
                return jsonReturn.returnFailed("未登录");
            }

            QueryWrapper<Student> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            Student student = studentService.getOne(wrapper);

            if (student == null) {
                return jsonReturn.returnFailed("学生信息不存在");
            }

            return jsonReturn.returnSuccess(student);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 更新学生状态
     * @param id 学生ID
     * @param status 新状态：1-在读，2-休学，3-毕业，4-退学
     * @return 操作结果
     */
    @PutMapping("/updateStatus")
    public String updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        try {
            Student student = studentService.getById(id);
            if (student == null) {
                return jsonReturn.returnFailed("学生不存在");
            }

            student.setStatus(status);
            student.setUpdateTime(new Date());

            boolean success = studentService.updateById(student);
            if (success) {
                return jsonReturn.returnSuccess("更新状态成功");
            } else {
                return jsonReturn.returnFailed("更新状态失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 更新学生双选状态
     * @param id 学生ID
     * @param selectionStatus 双选状态：0-未开始，1-第一轮，2-第二轮，3-已确定
     * @return 操作结果
     */
    @PutMapping("/updateSelectionStatus")
    public String updateSelectionStatus(@RequestParam Long id, @RequestParam Integer selectionStatus) {
        try {
            Student student = studentService.getById(id);
            if (student == null) {
                return jsonReturn.returnFailed("学生不存在");
            }

            student.setSelectionStatus(selectionStatus);
            student.setUpdateTime(new Date());

            boolean success = studentService.updateById(student);
            if (success) {
                return jsonReturn.returnSuccess("更新双选状态成功");
            } else {
                return jsonReturn.returnFailed("更新双选状态失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

}
