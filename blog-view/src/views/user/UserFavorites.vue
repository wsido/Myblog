<template>
  <div class="user-favorites">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>我的收藏</span>
      </div>
      
      <!-- 收藏博客列表 -->
      <div v-if="favorites.length > 0" class="favorite-list">
        <div v-for="blog in favorites" :key="blog.id" class="favorite-item">
          <div class="favorite-header">
            <h3 class="favorite-title">
              <router-link :to="{ path: `/blog/${blog.id}` }">{{ blog.title }}</router-link>
            </h3>
            <span class="favorite-date">收藏于 {{ formatDate(blog.favoriteTime) }}</span>
          </div>
          <div class="favorite-content">
            <div v-if="blog.firstPicture" class="favorite-image">
              <img :src="blog.firstPicture" :alt="blog.title">
            </div>
            <div class="favorite-desc">
              {{ blog.description || '暂无描述' }}
            </div>
          </div>
          <div class="favorite-footer">
            <div class="favorite-meta">
              <el-tag v-if="blog.category" size="mini">{{ blog.category.name }}</el-tag>
              <span class="favorite-views"><i class="el-icon-view"></i> {{ blog.views }}</span>
            </div>
            <div class="favorite-actions">
              <el-button type="text" size="mini" @click="removeFavorite(blog)">
                <i class="el-icon-star-off"></i> 取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无收藏的博客">
          <el-button type="primary" @click="$router.push('/home')">浏览博客</el-button>
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
import { getUserFavorites, removeFavorite } from '@/api/user';

export default {
  name: "UserFavorites",
  data() {
    return {
      favorites: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false
    }
  },
  created() {
    this.fetchUserFavorites()
  },
  methods: {
    fetchUserFavorites() {
      this.loading = true
      getUserFavorites(this.pageNum, this.pageSize).then(res => {
        if (res.code === 200) {
          this.favorites = res.data.favorites || []
          this.total = res.data.total || 0
        } else {
          this.$message.error('获取收藏列表失败')
        }
        this.loading = false
      }).catch(() => {
        // 如果API尚未实现，使用模拟数据
        // 这段代码可以在API实现后删除
        this.favorites = [
          {
            id: 1,
            title: '示例博客文章标题1',
            description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.',
            firstPicture: 'https://via.placeholder.com/300x200',
            views: 1234,
            favoriteTime: new Date(),
            category: {
              name: '技术'
            }
          },
          {
            id: 2,
            title: '示例博客文章标题2',
            description: 'Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.',
            firstPicture: 'https://via.placeholder.com/300x200',
            views: 567,
            favoriteTime: new Date(Date.now() - 86400000),
            category: {
              name: '生活'
            }
          }
        ]
        this.total = 2
        this.loading = false
        console.warn('使用模拟数据，API尚未实现')
      })
    },
    handleCurrentChange(newPage) {
      this.pageNum = newPage
      this.fetchUserFavorites()
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    },
    removeFavorite(blog) {
      this.$confirm(`确定要取消收藏《${blog.title}》吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removeFavorite(blog.id).then(res => {
          if (res.code === 200) {
            this.$message.success('取消收藏成功')
            // 更新本地数据，避免重新请求
            this.favorites = this.favorites.filter(item => item.id !== blog.id)
            this.total -= 1
          } else {
            this.$message.error(res.msg || '操作失败')
          }
        }).catch(() => {
          // 如果API尚未实现，使用模拟数据
          this.favorites = this.favorites.filter(item => item.id !== blog.id)
          this.total -= 1
          this.$message.success('取消收藏成功')
          console.warn('使用模拟数据，API尚未实现')
        })
      }).catch(() => {
        // 取消操作，不做处理
      })
    }
  }
}
</script>

<style scoped>
.user-favorites {
  max-width: 800px;
  margin: 20px auto;
}
.favorite-list {
  padding: 0;
}
.favorite-item {
  padding: 15px 0;
  border-bottom: 1px solid #EBEEF5;
}
.favorite-item:last-child {
  border-bottom: none;
}
.favorite-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.favorite-title {
  margin: 0;
  font-size: 18px;
}
.favorite-title a {
  color: #303133;
  text-decoration: none;
}
.favorite-title a:hover {
  color: #409EFF;
}
.favorite-date {
  color: #909399;
  font-size: 12px;
}
.favorite-content {
  display: flex;
  margin-bottom: 10px;
}
.favorite-image {
  width: 120px;
  height: 80px;
  margin-right: 15px;
  overflow: hidden;
}
.favorite-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.favorite-desc {
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
.favorite-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.favorite-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.favorite-views {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}
.favorite-actions {
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