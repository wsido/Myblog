<template>
  <div class="user-center">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>个人信息</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleEdit">编辑</el-button>
      </div>
      
      <div class="user-info">
        <div class="avatar-container">
          <el-avatar :size="100" :src="userInfo.avatar" @error="handleAvatarError">
            <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
          </el-avatar>
        </div>
        <div class="info-list">
          <div class="info-item">
            <span class="label">用户名：</span>
            <span class="value">{{ userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">昵称：</span>
            <span class="value">{{ userInfo.nickname }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱：</span>
            <span class="value">{{ userInfo.email }}</span>
          </div>
          <div class="info-item">
            <span class="label">注册时间：</span>
            <span class="value">{{ formatDate(userInfo.createTime) }}</span>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>我的统计</span>
      </div>
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-number">{{ stats.blogCount || 0 }}</div>
          <div class="stat-name">发布的博客</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ stats.viewCount || 0 }}</div>
          <div class="stat-name">总浏览量</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ stats.commentCount || 0 }}</div>
          <div class="stat-name">收到的评论</div>
        </div>
      </div>
    </el-card>
    
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>安全设置</span>
      </div>
      <div class="security-settings">
        <div class="setting-item">
          <div>
            <div class="setting-title">登录密码</div>
            <div class="setting-desc">安全性高的密码可以保护您的账号更加安全</div>
          </div>
          <el-button type="text" @click="showChangePasswordDialog">修改</el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 编辑个人信息对话框 -->
    <el-dialog title="编辑个人信息" :visible.sync="editDialogVisible" width="500px">
      <el-form ref="editForm" :model="editForm" :rules="editFormRules" label-width="80px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="/api/user/avatar"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <div class="avatar-tip">点击上传头像，仅支持JPG、PNG格式，大小不超过2MB</div>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEditForm">确 定</el-button>
      </div>
    </el-dialog>
    
    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" :visible.sync="passwordDialogVisible" width="500px">
      <el-form ref="passwordForm" :model="passwordForm" :rules="passwordFormRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmNewPassword">
          <el-input v-model="passwordForm.confirmNewPassword" show-password></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitPasswordForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserInfo, getUserStats, updatePassword, updateUserInfo } from '@/api/user';

export default {
  name: "UserCenter",
  data() {
    // 密码确认校验规则
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    
    return {
      userInfo: {
        id: '',
        username: '',
        nickname: '',
        avatar: '',
        email: '',
        createTime: '',
        type: ''
      },
      stats: {
        blogCount: 0,
        viewCount: 0,
        commentCount: 0
      },
      editDialogVisible: false,
      passwordDialogVisible: false,
      uploadHeaders: {
        Authorization: localStorage.getItem('userToken')
      },
      editForm: {
        id: '',
        nickname: '',
        avatar: '',
        email: ''
      },
      editFormRules: {
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      },
      passwordFormRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmNewPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchUserInfo()
    this.fetchUserStats()
  },
  methods: {
    fetchUserInfo() {
      getUserInfo().then(res => {
        if (res.code === 200) {
          this.userInfo = res.data
        } else {
          this.$message.error('获取用户信息失败')
        }
      }).catch(() => {
        this.$message.error('请求失败')
      })
    },
    fetchUserStats() {
      // 调用获取用户统计数据的API
      getUserStats().then(res => {
        if (res.code === 200) {
          this.stats = res.data
        } else {
          // 如果API请求失败，使用默认数据
          this.stats = {
            blogCount: 0,
            viewCount: 0,
            commentCount: 0
          }
        }
      }).catch(() => {
        // 如果API请求异常，使用默认数据
        this.stats = {
          blogCount: 0,
          viewCount: 0,
          commentCount: 0
        }
      })
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    },
    handleAvatarError() {
      this.$message.error('加载头像失败')
    },
    handleEdit() {
      this.editForm = {
        id: this.userInfo.id,
        nickname: this.userInfo.nickname,
        avatar: this.userInfo.avatar,
        email: this.userInfo.email
      }
      this.editDialogVisible = true
    },
    showChangePasswordDialog() {
      this.passwordDialogVisible = true
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      }
    },
    handleAvatarSuccess(res, file) {
      if (res.code === 200) {
        this.editForm.avatar = res.data
        this.$message.success('头像上传成功')
      } else {
        this.$message.error(res.msg || '头像上传失败')
      }
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg'
      const isPNG = file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG && !isPNG) {
        this.$message.error('上传头像图片只能是 JPG 或 PNG 格式!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
        return false
      }
      return (isJPG || isPNG) && isLt2M
    },
    submitEditForm() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          updateUserInfo(this.editForm).then(res => {
            if (res.code === 200) {
              this.$message.success('更新成功')
              this.editDialogVisible = false
              this.fetchUserInfo() // 重新获取用户信息
            } else {
              this.$message.error(res.msg || '更新失败')
            }
          }).catch(() => {
            this.$message.error('请求失败')
          })
        }
      })
    },
    submitPasswordForm() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          const { confirmNewPassword, ...passwordData } = this.passwordForm
          updatePassword(passwordData).then(res => {
            if (res.code === 200) {
              this.$message.success('密码修改成功，请重新登录')
              this.passwordDialogVisible = false
              // 清除登录状态，返回登录页
              window.localStorage.removeItem('userToken')
              this.$router.push('/login')
            } else {
              this.$message.error(res.msg || '修改失败')
            }
          }).catch(() => {
            this.$message.error('请求失败')
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.user-center {
  max-width: 800px;
  margin: 20px auto;
}
.box-card {
  margin-bottom: 20px;
}
.user-info {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}
.avatar-container {
  margin-right: 30px;
  margin-bottom: 20px;
}
.info-list {
  flex: 1;
}
.info-item {
  margin-bottom: 15px;
  font-size: 14px;
}
.label {
  color: #606266;
  margin-right: 10px;
}
.value {
  font-weight: bold;
}
.security-settings {
  padding: 5px 0;
}
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #EBEEF5;
}
.setting-item:last-child {
  border-bottom: none;
}
.setting-title {
  font-weight: bold;
  margin-bottom: 5px;
}
.setting-desc {
  color: #909399;
  font-size: 12px;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}
.avatar {
  width: 100px;
  height: 100px;
  display: block;
}
.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.stat-cards {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  padding: 10px 0;
}

.stat-card {
  text-align: center;
  padding: 20px;
  width: 120px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 10px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-name {
  font-size: 14px;
  color: #606266;
}
</style> 