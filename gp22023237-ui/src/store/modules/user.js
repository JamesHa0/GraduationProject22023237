import { login, logout } from '@/api/login'
import { getInfo } from '@/api/user/user'
import { getToken, setToken, removeToken, getUserId, setUserId } from '@/utils/auth'
import { isHttp, isEmpty } from "@/utils/validate"
import defAva from '@/assets/images/profile.jpg'
import { getStudentInfo } from '../../api/user/user'

const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: getToken(),
      userId: getUserId(),
      name: '',
      avatar: '',
      roles: [],
      permissions: [],
      studentInfo: null, // 存储学生信息
      teacherInfo: null  // 存储教师信息
    }),
    actions: {
      // 登录
      login(userInfo) {
        const username = userInfo.username.trim()
        const password = userInfo.password
        const code = userInfo.code
        const uuid = userInfo.uuid
        return new Promise((resolve, reject) => {
          login(username, password, code, uuid).then(res => {
            setToken(res.data.token)
            setUserId(res.data.id)
            console.log(`登录成功`);
            this.userId = res.data.id
            this.token = res.data.token
            resolve()
          }).catch(error => {
            console.log(`登录失败`);
            reject(error)
          })
        })
      },
      // 获取用户信息
      getInfo(userId) {
        return new Promise((resolve, reject) => {
          getInfo(userId).then(res => {
            console.log(`获取用户信息成功：`, res.data);
            let avatar = res.data.avatar || ""
            if (!isHttp(avatar)) {
              avatar = (isEmpty(avatar)) ? defAva : import.meta.env.VITE_APP_BASE_API + avatar
            }

            // 处理不同角色类型
            if (res.data[0].roleId) { // 验证返回的roles是否非空
              this.roles = res.data[0].roleId
              this.permissions = ["PERMISSIONS_DEFAULT"]
              //this.permissions = res.permissions // 这里没有权限（我的数据库里角色代表了权限）

              // roleId为6表示学生
              if (res.data[0].roleId === 6) {
                this.fetchStudentInfo(userId);
              }
              // roleId为7表示教师
              else if (res.data[0].roleId === 7) {
                this.fetchTeacherInfo(userId);
              }


            } else {
              this.roles = ['ROLE_DEFAULT']
            }
            this.name = res.data[0].nickname
            this.avatar = avatar
            resolve(res)
          }).catch(error => {
            reject(error)
          })
        })
      },

      // 获取学生信息
      fetchStudentInfo(userId) {
        console.log(`获取学生信息`);
        return new Promise((resolve, reject) => {
          getStudentInfo(userId).then(res => {
            this.studentInfo = res.data
            console.log(`获取学生信息成功：`, res.data);

            resolve(res)
          }).catch(error => {
            reject(error)
          })
        })
      },
      // 获取教师信息
      fetchTeacherInfo(userId) {
        console.log(`获取教师信息`);
        return new Promise((resolve, reject) => {
          // getTeacherInfo(userId).then(res => {
          //   this.teacherInfo = res.data
          //   resolve(res)
          // }).catch(error => {
          //   reject(error)
          // })
        })
      },
      // 退出系统
      logOut() {
        return new Promise((resolve, reject) => {
          logout(this.token).then(() => {
            this.token = ''
            this.roles = ''
            this.permissions = []
            removeToken()
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      }
    }
  })

export default useUserStore
