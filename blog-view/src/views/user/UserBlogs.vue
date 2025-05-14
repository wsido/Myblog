<template>
  <div class="user-blogs">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>我的博客</span>
        <el-button style="float: right;" type="primary" size="small" @click="goToWrite">写文章</el-button>
      </div>
      
      <!-- 博客列表 -->
      <div v-if="blogs.length > 0" class="blog-list">
        <div v-for="blog in blogs" :key="blog.id" class="blog-item">
          <div class="blog-header">
            <h3 class="blog-title">
              <router-link :to="{ path: `/blog/${blog.id}` }">{{ blog.title }}</router-link>
            </h3>
            <span class="blog-date">{{ formatDate(blog.createTime) }}</span>
          </div>
          <div class="blog-content">
            <div v-if="blog.firstPicture" class="blog-image">
              <img :src="blog.firstPicture" :alt="blog.title">
            </div>
            <div class="blog-desc">
              {{ blog.description || '暂无描述' }}
            </div>
          </div>
          <div class="blog-footer">
            <div class="blog-meta">
              <el-tag v-if="blog.category" size="mini">{{ blog.category.name }}</el-tag>
              <span class="blog-views"><i class="el-icon-view"></i> {{ blog.views }}</span>
              
              <el-tag 
                v-if="blog.published" 
                type="success" 
                size="mini">已发布</el-tag>
              <el-tag 
                v-else 
                type="info" 
                size="mini">草稿</el-tag>
              
              <el-tag 
                v-if="blog.password && blog.password !== ''" 
                type="warning" 
                size="mini">密码保护</el-tag>
            </div>
            <div class="blog-actions">
              <el-button type="text" size="mini" @click="editBlog(blog)">编辑</el-button>
              <el-button type="text" size="mini" @click="changeBlogVisibility(blog)">
                {{ blog.published ? '转为草稿' : '发布' }}
              </el-button>
              <el-button type="text" size="mini" style="color: #F56C6C;" @click="deleteBlogConfirm(blog)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无博客，请前往管理后台发表文章">
          <!-- 不在这里显示写文章按钮，避免重复 -->
        </el-empty>
      </div>
      
      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :current-page.sync="pageNum"
        :page-size="pageSize"
        :total="total"
        @current-change="handleCurrentChange"
        class="pagination">
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
import { deleteBlog, getUserBlogs, updateBlogVisibility } from '@/api/user'

export default {
  name: "UserBlogs",
  data() {
    return {
      blogs: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false
    }
  },
  created() {
    this.fetchUserBlogs()
  },
  methods: {
    fetchUserBlogs() {
      this.loading = true
      getUserBlogs(this.pageNum, this.pageSize).then(res => {
        if (res.code === 200) {
          this.blogs = res.data.blogs || []
          this.total = res.data.total || 0
        } else {
          this.$message.error('获取博客列表失败')
        }
        this.loading = false
      }).catch(() => {
        this.$message.error('请求失败')
        this.loading = false
      })
    },
    handleCurrentChange(newPage) {
      this.pageNum = newPage
      this.fetchUserBlogs()
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    },
    goToWrite() {
      // 跳转到后台写文章页面，使用完整URL
      window.open('http://localhost:8080/admin/#/blog/write', '_blank')
    },
    editBlog(blog) {
      // 跳转到后台编辑页面，使用完整URL
      window.open(`http://localhost:8080/admin/#/blog/edit/${blog.id}`, '_blank')
    },
    changeBlogVisibility(blog) {
      const newVisibility = {
        appreciation: blog.appreciation,
        commentEnabled: blog.commentEnabled,
        published: !blog.published,  // 切换发布状态
        recommend: blog.recommend,
        top: blog.top,
        password: blog.password || ''
      }
      
      updateBlogVisibility(blog.id, newVisibility).then(res => {
        if (res.code === 200) {
          this.$message.success(`已${newVisibility.published ? '发布' : '转为草稿'}`)
          // 更新本地数据，避免重新请求
          blog.published = newVisibility.published
        } else {
          this.$message.error(res.msg || '操作失败')
        }
      }).catch(() => {
        this.$message.error('请求失败')
      })
    },
    deleteBlogConfirm(blog) {
      this.$confirm(`确定要删除博客 "${blog.title}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.doDeleteBlog(blog.id)
      }).catch(() => {
        // 取消删除，不做处理
      })
    },
    doDeleteBlog(blogId) {
      deleteBlog(blogId).then(res => {
        if (res.code === 200) {
          this.$message.success('删除成功')
          // 重新加载博客列表
          this.fetchUserBlogs()
        } else {
          this.$message.error(res.msg || '删除失败')
        }
      }).catch(() => {
        this.$message.error('请求失败')
      })
    }
  }
}
</script>

<style scoped>
.user-blogs {
  max-width: 800px;
  margin: 20px auto;
}
.blog-list {
  padding: 0;
}
.blog-item {
  padding: 15px 0;
  border-bottom: 1px solid #EBEEF5;
}
.blog-item:last-child {
  border-bottom: none;
}
.blog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.blog-title {
  margin: 0;
  font-size: 18px;
}
.blog-title a {
  color: #303133;
  text-decoration: none;
}
.blog-title a:hover {
  color: #409EFF;
}
.blog-date {
  color: #909399;
  font-size: 12px;
}
.blog-content {
  display: flex;
  margin-bottom: 10px;
}
.blog-image {
  width: 120px;
  height: 80px;
  margin-right: 15px;
  overflow: hidden;
}
.blog-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.blog-desc {
  flex: 1;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
.blog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.blog-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.blog-views {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}
.blog-actions {
  display: flex;
  gap: 5px;
}
.empty-state {
  padding: 30px 0;
  text-align: center;
}
.pagination {
  text-align: center;
  margin-top: 20px;
}
</style> 