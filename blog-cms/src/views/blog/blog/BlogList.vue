<template>
	<div>
		<!--搜索-->
		<el-row>
			<el-col :span="8">
				<el-input placeholder="请输入标题" v-model="queryInfo.title" :clearable="true" @clear="search" @keyup.native.enter="search" size="small" style="min-width: 500px">
					<el-select v-model="queryInfo.categoryId" slot="prepend" placeholder="请选择分类" :clearable="true" @change="search" style="width: 160px">
						<el-option :label="item.name" :value="item.id" v-for="item in categoryList" :key="item.id"></el-option>
					</el-select>
					<el-button slot="append" icon="el-icon-search" @click="search"></el-button>
				</el-input>
			</el-col>
		</el-row>

		<el-table :data="blogList">
			<el-table-column label="序号" type="index" width="50"></el-table-column>
			<el-table-column label="标题" prop="title" show-overflow-tooltip></el-table-column>
			<el-table-column label="分类" prop="category.name" width="150"></el-table-column>
			<el-table-column label="置顶" width="80">
				<template v-slot="scope">
					<el-switch v-if="isAdminScope" v-model="scope.row.top" @change="blogTopChanged(scope.row)"></el-switch>
					<span v-else>{{ scope.row.top ? '是' : '否' }}</span>
				</template>
			</el-table-column>
			<el-table-column label="推荐" width="80">
				<template v-slot="scope">
					<el-switch v-if="isAdminScope" v-model="scope.row.recommend" @change="blogRecommendChanged(scope.row)"></el-switch>
					<span v-else>{{ scope.row.recommend ? '是' : '否' }}</span>
				</template>
			</el-table-column>
			<el-table-column label="可见性" width="100">
				<template v-slot="scope">
					<el-link icon="el-icon-edit" :underline="false" @click="editBlogVisibility(scope.row)">
						{{ scope.row.published ? (scope.row.password !== '' && scope.row.password !== null ? '密码保护' : '公开') : '私密' }}
					</el-link>
				</template>
			</el-table-column>
			<el-table-column label="创建时间" width="170">
				<template v-slot="scope">{{ scope.row.createTime | dateFormat }}</template>
			</el-table-column>
			<el-table-column label="最近更新" width="170">
				<template v-slot="scope">{{ scope.row.updateTime | dateFormat }}</template>
			</el-table-column>
			<el-table-column label="操作" width="200">
				<template v-slot="scope">
					<el-button type="primary" icon="el-icon-edit" size="mini" @click="goBlogEditPage(scope.row.id)">编辑</el-button>
					<el-button size="mini" type="danger" icon="el-icon-delete" @click="deleteBlog(scope.row.id)">临时删除</el-button>
				</template>
			</el-table-column>
		</el-table>

		<!--分页-->
		<el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="queryInfo.pageNum"
		               :page-sizes="[10, 20, 30, 50]" :page-size="queryInfo.pageSize" :total="total"
		               layout="total, sizes, prev, pager, next, jumper" background>
		</el-pagination>

		<!--编辑可见性状态对话框-->
		<el-dialog title="博客可见性" width="30%" :visible.sync="dialogVisible">
			<!--内容主体-->
			<el-form label-width="50px" @submit.native.prevent>
				<el-form-item>
					<el-radio-group v-model="radio">
						<el-radio :label="1">公开</el-radio>
						<el-radio :label="2">私密</el-radio>
						<el-radio :label="3">密码保护</el-radio>
					</el-radio-group>
				</el-form-item>
				<el-form-item label="密码" v-if="radio===3">
					<el-input v-model="visForm.password"></el-input>
				</el-form-item>
				<el-form-item v-if="radio!==2">
					<el-row>
						<el-col :span="6">
							<el-switch v-model="visForm.appreciation" active-text="赞赏"></el-switch>
						</el-col>
						<el-col :span="6" v-if="isAdminScope">
							<el-switch v-model="visForm.recommend" active-text="推荐"></el-switch>
						</el-col>
						<el-col :span="6">
							<el-switch v-model="visForm.commentEnabled" active-text="评论"></el-switch>
						</el-col>
						<el-col :span="6" v-if="isAdminScope">
							<el-switch v-model="visForm.top" active-text="置顶"></el-switch>
						</el-col>
					</el-row>
				</el-form-item>
			</el-form>
			<!--底部-->
			<span slot="footer">
				<el-button @click="dialogVisible=false">取 消</el-button>
				<el-button type="primary" @click="saveVisibility">保存</el-button>
			</span>
		</el-dialog>
	</div>
</template>

<script>
	import {
    // Admin scope APIs
    getDataByQuery,
    getAdminBlogs,
    deleteBlogById as adminDeleteBlogById, // renamed to avoid conflict
    updateTop,
    updateRecommend,
    updateVisibility as adminUpdateVisibility, // renamed
    getCategoryAndTag as adminGetCategoryAndTag, // renamed

    // User scope APIs (for public facing 'my articles' page, uses /user/blog/**)
    getCurrentUserBlogsByQuery, 
    deleteCurrentUserBlogById,
    updateCurrentUserBlogVisibility,
    getUserCategoryAndTag as publicGetUserCategoryAndTag, // renamed

    // New User Management Scope APIs (uses /api/user/management/**)
    getUserManagementBlogs,
    deleteUserManagementBlog,
    getUserManagementCategoriesAndTags,
    // updateUserManagementBlogVisibility will be handled by updateUserManagementBlog in WriteBlog.vue or by a new specific API if needed for visibility alone
	} from '@/api/blog';
	import Breadcrumb from "@/components/Breadcrumb";
	import { mapGetters } from 'vuex';

	export default {
		name: "BlogList",
		components: {Breadcrumb},
		data() {
			return {
				queryInfo: {
					title: '',
					categoryId: null,
					pageNum: 1,
					pageSize: 10
				},
				blogList: [],
				categoryList: [],
				total: 0,
				dialogVisible: false,
				blogId: 0, // For visibility editing
				radio: 1, // For visibility dialog: 1-public, 2-private, 3-password
				visForm: { // Form for visibility dialog
					appreciation: false,
					recommend: false, // Only admin can change recommend/top via this dialog if isAdminScope
					commentEnabled: false,
					top: false,
					published: false,
					password: '',
				},
				loading: false
			}
		},
		computed: {
			...mapGetters(['roles', 'userId']), // userId from Vuex store
			// isAdmin getter is already checking this.roles.includes('admin')
			// We use viewScope to differentiate contexts (admin full control, user CMS, user public view)
			viewScope() {
				return this.$route.meta.scope;
			},
      isAdminScope() { // Specifically for UI elements only admins should interact with
        return this.roles.includes('admin') && this.viewScope === 'all';
      }
		},
		created() {
			this.getInitialData();
		},
		watch: {
			// Watch for route changes if the same component is used for different scopes
			'$route': 'getInitialData' 
		},
		methods: {
      getInitialData(){
        this.queryInfo.pageNum = 1; // Reset pagination on scope change
        this.getData();
        this.fetchCategories();
      },
			getData() {
				this.loading = true;
				let apiCall;
				let processResponse;

				if (this.viewScope === 'currentUserCMS') {
					apiCall = getUserManagementBlogs(this.queryInfo);
					processResponse = (res) => {
						if (res.code === 200) {
							console.log('[BlogList.vue currentUserCMS] API Response data:', JSON.parse(JSON.stringify(res.data)));
							this.blogList = res.data.list;
							this.total = res.data.total;
							console.log('[BlogList.vue currentUserCMS] Assigned this.blogList:', JSON.parse(JSON.stringify(this.blogList)));
							if (this.blogList && this.blogList.some(item => item === null)) {
								console.error('[BlogList.vue currentUserCMS] CRITICAL: this.blogList contains null items!');
							}
						} else {
							this.msgError(res.msg);
							console.error('[BlogList.vue currentUserCMS] API Error:', res.msg);
						}
					};
				} else if (this.viewScope === 'all' && this.isAdminScope) {
					apiCall = getAdminBlogs(this.queryInfo);
					processResponse = (res) => {
						if (res.code === 200) {
							console.log('[BlogList.vue adminScope] API Response data.blogs:', JSON.parse(JSON.stringify(res.data.blogs)));
							this.blogList = res.data.blogs.list;
							this.total = res.data.blogs.total;
							console.log('[BlogList.vue adminScope] Assigned this.blogList:', JSON.parse(JSON.stringify(this.blogList)));
							if (this.blogList && this.blogList.some(item => item === null)) {
								console.error('[BlogList.vue adminScope] CRITICAL: this.blogList contains null items!');
							}
						} else {
							this.msgError(res.msg);
							console.error('[BlogList.vue adminScope] API Error:', res.msg);
						}
					};
				} else {
					// Fallback or other scopes if necessary
					this.msgError("未知视图范围，无法加载数据");
					this.loading = false;
					return;
				}

				apiCall.then(res => {
					processResponse(res);
					this.loading = false;
				}).catch(err => {
					console.error(`[BlogList.vue ${this.viewScope}] API Exception:`, err);
					this.msgError('数据加载失败: ' + (err.response?.data?.msg || err.message || '未知错误'));
					this.loading = false;
				});
			},
      fetchCategories(){
        if (this.viewScope === 'currentUserCMS') {
          getUserManagementCategoriesAndTags().then(res => {
            this.categoryList = res.data.categories || [];
          }).catch(err => console.error("Failed to fetch user CMS categories", err));
        } else if (this.viewScope === 'currentUser'){
          publicGetUserCategoryAndTag().then(res => { // publicGetUserCategoryAndTag is /user/blog/categoryAndTag
            this.categoryList = res.data.categories || [];
          }).catch(err => console.error("Failed to fetch public user categories", err));
        } else if (this.roles.includes('admin') && this.viewScope === 'all') {
          adminGetCategoryAndTag().then(res => { // adminGetCategoryAndTag is /admin/categoryAndTag
            this.categoryList = res.data.categories || [];
          }).catch(err => console.error("Failed to fetch admin categories", err));
        } else {
          this.categoryList = [];
        }
      },
			search() {
				this.queryInfo.pageNum = 1
				// pageSize is kept from user selection or default
				this.getData()
			},
			//以下方法仅管理员可操作，在模板中已用 v-if="isAdminScope" 控制
			blogTopChanged(row) {
        if (!this.isAdminScope) return this.msgWarning('无权操作');
				updateTop(row.id, row.top).then(res => {
					this.msgSuccess(res.msg);
				}).catch(() => row.top = !row.top ); // Revert on failure
			},
			blogRecommendChanged(row) {
        if (!this.isAdminScope) return this.msgWarning('无权操作');
				updateRecommend(row.id, row.recommend).then(res => {
					this.msgSuccess(res.msg);
				}).catch(() => row.recommend = !row.recommend); // Revert on failure
			},
			editBlogVisibility(row) { // Both admin and user (for their own blogs) can edit visibility
				this.visForm = {
					appreciation: row.appreciation,
					recommend: row.recommend, // Will be ignored by backend if user is not admin
					commentEnabled: row.commentEnabled,
					top: row.top, // Will be ignored by backend if user is not admin
					published: row.published,
					password: row.password || '',
				}
				this.blogId = row.id
				this.radio = this.visForm.published ? (this.visForm.password !== '' && this.visForm.password !== null ? 3 : 1) : 2
				this.dialogVisible = true
			},
			saveVisibility() {
				if (this.radio === 3 && (!this.visForm.password || this.visForm.password.trim() === '')) {
					return this.msgError("密码保护模式必须填写密码！")
				}
				if (this.radio === 2) { // Private
					this.visForm.published = false;
          // Private blogs typically lose these public-facing attributes
					// this.visForm.appreciation = false; // Keep user's choice for appreciation
					// this.visForm.commentEnabled = false; // Keep user's choice for comments
          // Recommendation and Top are admin-only concerns, their state is preserved from row, 
          // but backend will ultimately decide if non-admin can set them.
				} else { // Public or Password Protected
					this.visForm.published = true;
				}
				if (this.radio !== 3) { // Not password protected
					this.visForm.password = '';
				}

        // Construct the DTO for visibility update. 
        // For user scope, only basic visibility fields (published, password, appreciation, commentEnabled) are typically sent.
        // For admin scope, additional fields like recommend, top can be sent.
        const visibilityData = {
          published: this.visForm.published,
          password: this.visForm.password,
          appreciation: this.visForm.appreciation,
          commentEnabled: this.visForm.commentEnabled,
        };

        if (this.isAdminScope) {
          visibilityData.recommend = this.visForm.recommend;
          visibilityData.top = this.visForm.top;
        }

        let apiCall;
        if (this.viewScope === 'currentUserCMS') {
          // For user management, we might need a specific API or use the general update blog API
          // Let's assume for now the general updateUserManagementBlog can handle visibility partial updates
          // Or, we create a dedicated updateUserManagementBlogVisibility if backend supports it.
          // For simplicity, we'll call a general update or expect the backend to handle this via a DTO in `updateUserManagementBlog`.
          // However, the backend UserBlogManagementController.updateMyBlog expects a full Blog DTO.
          // So, we should fetch the full blog, apply changes, then send it for update OR
          // the backend needs a dedicated visibility endpoint for users: /api/user/management/blogs/{blogId}/visibility
          // Given current backend, direct visibility update for user-cms is not straightforward with current visForm.
          // This requires UserBlogManagementController to have a PUT /blogs/{blogId}/visibility endpoint.
          // FOR NOW: We will assume adminUpdateVisibility is used by admin, and for user, visibility changes are part of full edit in WriteBlog.vue.
          // OR, if we want to enable quick visibility change for user-cms here, we need that new backend endpoint.
          // Let's call adminUpdateVisibility if isAdminScope, and do nothing for user-cms for now, prompting to edit full blog.
          if (this.isAdminScope) { // Admin updating visibility for any blog
             apiCall = adminUpdateVisibility(this.blogId, visibilityData);
          } else {
            this.msgInfo("请通过编辑文章功能修改可见性。"); // Placeholder until specific user visibility API is confirmed/added
            this.dialogVisible = false;
            return;
          }
        } else if (this.viewScope === 'currentUser') { // Public facing, using /user/blog/{id}/visibility
          apiCall = updateCurrentUserBlogVisibility(this.blogId, visibilityData);
        } else if (this.isAdminScope) { // Admin scope (viewScope === 'all')
          apiCall = adminUpdateVisibility(this.blogId, visibilityData);
        } else {
          this.msgError("未知操作范围或无权限");
          this.dialogVisible = false;
          return;
        }

				apiCall.then(res => {
					this.msgSuccess(res.msg);
					this.dialogVisible = false;
					this.getData(); // Refresh list
				}).catch(err => {
          this.msgError("保存失败: " + (err.response?.data?.msg || err.message));
        });
			},
			deleteBlog(id) {
        console.log('[BlogList.vue] deleteBlog called with id:', id, 'Scope:', this.viewScope, 'IsAdmin:', this.roles.includes('admin'));
        let apiCall;
        // 如果是管理员 (拥有 'admin' 角色)
        if (this.roles.includes('admin')) {
          // 不论是在 'currentUserCMS' 还是 'all' 视图，管理员都使用 adminDeleteBlogById
          apiCall = adminDeleteBlogById(id);
        } else if (this.viewScope === 'currentUserCMS') { // 普通用户在自己的文章管理页面
          apiCall = deleteUserManagementBlog(id);
        } else if (this.viewScope === 'currentUser') { // 公开的"我的文章"页面 (非CMS)
          apiCall = deleteCurrentUserBlogById(id);
        } else {
          this.msgError("未知操作范围或无权限");
          return;
        }

				apiCall.then(res => {
					this.msgSuccess(res.msg);
					this.getData(); // Refresh list
				}).catch(err => {
          this.msgError("删除失败: " + (err.response?.data?.msg || err.message));
        });
			},
			//跳转到博客编辑页
			goBlogEditPage(id) {
				if (this.viewScope === 'currentUserCMS') {
					this.$router.push(`/my-blog/edit-article/${id}`)
				} else if (this.roles.includes('admin') && this.viewScope === 'all') {
					this.$router.push(`/admin/content-management/edit-article-all/${id}`)
				} else {
					this.msgError('无法确定编辑范围或无权限')
				}
			},
			//监听pageSize改变的事件
			handleSizeChange(newSize) {
				this.queryInfo.pageSize = newSize
				this.getData()
			},
			//监听pageNum改变的事件
			handleCurrentChange(newPage) {
				this.queryInfo.pageNum = newPage
				this.getData()
			}
		}
	}
</script>

<style scoped>
	.el-button + span {
		margin-left: 10px;
	}
</style>