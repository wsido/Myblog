<template>
	<div>
		<el-form :model="form" :rules="formRules" ref="formRef" label-position="top">
			<el-row :gutter="20">
				<el-col :span="12">
					<el-form-item label="文章标题" prop="title">
						<el-input v-model="form.title" placeholder="请输入标题"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="文章首图URL" prop="firstPicture">
						<el-input v-model="form.firstPicture" placeholder="文章首图，可留空由系统自动生成">
							<el-button slot="append" @click="generateFirstPicturePreview">生成预览</el-button>
						</el-input>
					</el-form-item>
					<el-image v-if="form.firstPicture" :src="form.firstPicture" fit="contain" style="width: 100%; height: 200px; border: 1px solid #EBEEF5; margin-top: 10px;"></el-image>
				</el-col>
			</el-row>

			<el-form-item label="文章描述" prop="description">
				<mavon-editor v-model="form.description"/>
			</el-form-item>

			<el-form-item label="文章正文" prop="content">
				<mavon-editor v-model="form.content"/>
			</el-form-item>

			<el-row :gutter="20">
				<el-col :span="12">
					<el-form-item label="分类" prop="cate">
						<el-select v-model="form.cate" placeholder="请选择分类（输入可添加新分类）" :allow-create="isAllowCreateCategoryTag" :filterable="true" style="width: 100%;">
							<el-option :label="item.name" :value="item.id" v-for="item in categoryList" :key="item.id"></el-option>
						</el-select>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="标签" prop="tagList">
						<el-select v-model="form.tagList" placeholder="请选择标签（输入可添加新标签）" :allow-create="isAllowCreateCategoryTag" :filterable="true" :multiple="true" style="width: 100%;">
							<el-option :label="item.name" :value="item.id" v-for="item in tagList" :key="item.id"></el-option>
						</el-select>
					</el-form-item>
				</el-col>
			</el-row>

			<el-row :gutter="20">
				<el-col :span="8">
					<el-form-item label="字数" prop="words">
						<el-input v-model="form.words" placeholder="请输入文章字数（自动计算阅读时长）" type="number"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="8">
					<el-form-item label="阅读时长(分钟)" prop="readTime">
						<el-input v-model="form.readTime" placeholder="请输入阅读时长（可选）默认 Math.round(字数 / 200)" type="number"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="8">
					<el-form-item label="浏览次数" prop="views">
						<el-input v-model="form.views" placeholder="请输入文章字数（可选）默认为 0" type="number"></el-input>
					</el-form-item>
				</el-col>
			</el-row>

			<el-form-item style="text-align: right;">
				<el-button type="primary" @click="openVisibilityDialog">保存</el-button>
			</el-form-item>
		</el-form>

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
					<el-input v-model="form.password"></el-input>
				</el-form-item>
				<el-form-item v-if="radio!==2">
					<el-row>
						<el-col :span="6">
							<el-switch v-model="form.appreciation" active-text="赞赏"></el-switch>
						</el-col>
						<el-col :span="6" v-if="isAdmin">
							<el-switch v-model="form.recommend" active-text="推荐"></el-switch>
						</el-col>
						<el-col :span="6">
							<el-switch v-model="form.commentEnabled" active-text="评论"></el-switch>
						</el-col>
						<el-col :span="6" v-if="isAdmin">
							<el-switch v-model="form.top" active-text="置顶"></el-switch>
						</el-col>
					</el-row>
				</el-form-item>
			</el-form>
			<!--底部-->
			<span slot="footer">
				<el-button @click="dialogVisible=false">取 消</el-button>
				<el-button type="primary" @click="submitForm">保存</el-button>
			</span>
		</el-dialog>
	</div>
</template>

<script>
	// import { getBlogById, getCategoryAndTag, saveBlog, saveCurrentUserBlog, updateBlog, updateCurrentUserBlog } from '@/api/blog';
	// Updated imports:
	import {
		// Admin scope APIs
		adminGetBlogById,
		getCategoryAndTag as adminGetCategoryAndTag,
		saveBlog as adminSaveBlog,
		updateBlog as adminUpdateBlog,

		// New User Management Scope APIs (uses /api/user/management/**)
		getUserManagementBlogDetail,
		getUserManagementCategoriesAndTags,
		createUserManagementBlog,
		updateUserManagementBlog,

		// New API for cover generation
		generateCover
		// public facing user APIs like getCurrentUserBlogById are not directly used in this CMS write form for now
	} from '@/api/blog';
	import Breadcrumb from "@/components/Breadcrumb";
	import { mapGetters, mapActions } from 'vuex';

	export default {
		name: "WriteBlog",
		components: {Breadcrumb},
		data() {
			return {
				categoryList: [],
				tagList: [],
				dialogVisible: false,
				radio: 1,
				form: {
					id: null,
					title: '',
					firstPicture: '',
					description: '',
					content: '',
					cate: null,
					tagList: [],
					words: null,
					readTime: null,
					views: 0,
					appreciation: false,
					recommend: false,
					commentEnabled: true,
					top: false,
					published: false,
					password: '',
				},
				formRules: {
					title: [{required: true, message: '请输入标题', trigger: 'blur'}],
					cate: [{required: true, message: '请选择分类', trigger: 'change'}],
					tagList: [{required: true, message: '请选择标签', trigger: 'change', type: 'array', min: 1}],
					words: [{required: true, message: '请输入文章字数', trigger: 'blur'}, {type: 'number', message: '字数必须为数字值'}],
					readTime: [{type: 'number', message: '阅读时长必须为数字值'}],
					views: [{type: 'number', message: '浏览次数必须为数字值'}]
				},
			}
		},
		computed: {
			...mapGetters(['roles', 'userId', 'isAdmin', 'isAdminAllScope', 'isCurrentUserCmsScope']),
			isEditing() {
				return !!this.$route.params.id;
			},
			viewScope() {
				return this.$route.meta.scope || 'all'; 
			},
			isAllowCreateCategoryTag(){
				return this.isAdmin;
			}
		},
		watch: {
			'form.words'(newValue) {
				const numValue = parseInt(newValue);
				// Only auto-calculate if not editing or readTime is not manually set (or zero and form has id meaning it was loaded)
				if (numValue && (!this.form.readTime || this.form.readTime === 0) && (!this.isEditing || !this.form.id) && this.form.readTime !== null) { 
					this.form.readTime = Math.max(1, Math.round(numValue / 200));
				}
			},
			'form.content'(newVal) {
				// Auto-calculate words only if not editing or words field is empty/zero.
				if ((!this.isEditing || !this.form.words) && newVal) {
					this.calculateWords(newVal);
				} else if (!newVal && (!this.isEditing || !this.form.id)) { // also reset if content is cleared and not editing a loaded blog
					this.form.words = 0;
				}
			}
		},
		created() {
			console.log('[WriteBlog.vue] Created hook. Route params:', this.$route.params, 'Scope:', this.viewScope, 'IsAdmin:', this.isAdmin);
			this.fetchInitialData();
			if (this.$route.params.id) {
				// Ensure ID from route is stored correctly, it might be a string initially
				this.form.id = parseInt(this.$route.params.id, 10);
			} else {
				this.form.id = null; // Explicitly null for new blogs
			}
		},
		methods: {
			...mapActions('user', ['getUserInfo']),
			...mapActions('blog', ['getCategoryAndTag', 'updateCategoryAndTag']),
			generateFirstPicturePreview() {
				if (!this.form.title) {
					this.$message.warning('请先填写文章标题');
					return;
				}
				generateCover(this.form.title).then(res => {
					if (res.code === 200) {
						this.form.firstPicture = res.data;
						this.$message.success('预览图已生成');
					} else {
						this.$message.error(res.msg);
					}
				}).catch(() => {
					this.$message.error('生成预览图失败，请检查后端服务');
				});
			},
			calculateWords(markdownContent) {
				if (markdownContent) {
					const text = markdownContent.replace(/[\\s#*`\\>【】\-]/g, ''); // Remove common markdown symbols and spaces for a rough count
					this.form.words = text.length;
				} else {
					this.form.words = 0;
				}
			},
			async fetchInitialData() {
				this.form.id = this.$route.params.id ? parseInt(this.$route.params.id, 10) : null;
				try {
					// Fetch Categories and Tags
					let categoriesTagsResponse;
					if (this.isAdmin) { // Admin always uses admin API for categories/tags
						console.log("WriteBlog: Admin fetching categories/tags using adminGetCategoryAndTag");
						categoriesTagsResponse = await adminGetCategoryAndTag();
					} else if (this.isCurrentUserCmsScope) { // User in their CMS scope
						console.log("WriteBlog: User fetching categories/tags using getUserManagementCategoriesAndTags");
						categoriesTagsResponse = await getUserManagementCategoriesAndTags();
					} else {
						console.warn("WriteBlog: Unhandled scope/role for categories/tags. ViewScope:", this.viewScope, "Roles:", this.roles);
						this.categoryList = [];
						this.tagList = [];
						// Potentially redirect or error if no valid case for categories/tags
					}

					if (categoriesTagsResponse && categoriesTagsResponse.data) {
						this.categoryList = categoriesTagsResponse.data.categories || [];
						this.tagList = categoriesTagsResponse.data.tags || [];
					} else if (!categoriesTagsResponse && (this.isAdmin || this.isCurrentUserCmsScope)) {
						// If response is undefined but we expected one, log it.
						console.warn("WriteBlog: Categories/tags response was undefined for expected scope. ViewScope:", this.viewScope);
					}

					// If editing, fetch blog details
					if (this.isEditing && this.form.id) {
						let blogDataResponse;
						if (this.isAdmin) { // Admin uses adminGetBlogById regardless of scope if they are admin
							console.log(`WriteBlog: Admin fetching blog details using adminGetBlogById for ID: ${this.form.id}`);
							blogDataResponse = await adminGetBlogById(this.form.id);
						} else if (this.isCurrentUserCmsScope) { // Non-admin user in their CMS scope
							console.log(`WriteBlog: User fetching blog details using getUserManagementBlogDetail for ID: ${this.form.id}`);
							blogDataResponse = await getUserManagementBlogDetail(this.form.id);
						} else {
							console.warn("WriteBlog: Unhandled scope/role for fetching blog details. ViewScope:", this.viewScope, "IsAdmin:", this.isAdmin);
							// Potentially handle error or redirect
						}

						if (blogDataResponse && blogDataResponse.data) {
							// Populate form with fetched data
							const blog = blogDataResponse.data;
							this.form.title = blog.title;
							this.form.firstPicture = blog.firstPicture;
							this.form.description = blog.description;
							this.form.content = blog.content;
							this.form.cate = blog.category ? blog.category.id : null; // Assuming category is an object with id
							this.form.tagList = blog.tags ? blog.tags.map(tag => tag.name) : []; // Assuming tags is an array of objects with id
							this.form.words = blog.words;
							this.form.readTime = blog.readTime;
							this.form.views = blog.views;
							this.form.appreciation = blog.appreciation;
							this.form.recommend = blog.recommend;
							this.form.commentEnabled = blog.commentEnabled;
							this.form.top = blog.top;
							this.form.published = blog.published;
							this.form.password = blog.password || '';

							// Set radio based on published status and password
							if (blog.published) {
								this.radio = (blog.password && blog.password !== '') ? 3 : 1;
							} else {
								this.radio = 2;
							}
						} else {
							this.msgError('获取博客详情失败');
							// Optionally redirect or handle error
						}
					}
				} catch (error) {
					console.error("Error fetching initial data for WriteBlog:", error);
					// this.msgError('加载数据失败，请重试');
				}
			},
			openVisibilityDialog() {
				this.$refs.formRef.validate(valid => {
					if (valid) {
						this.dialogVisible = true
					} else {
						return this.msgError('请填写必要的表单项')
					}
				})
			},
			submitForm() {
				this.form.published = this.radio === 1 || this.radio === 3;
				if (this.radio !== 3) {
					this.form.password = '';
				}
				this.$refs.formRef.validate(async (valid) => {
					if (valid) {
						// Map selected tag names back to IDs before submitting
						if (this.form.tagList && this.form.tagList.length > 0) {
							this.form.tagIds = this.tagList
								.filter(tag => this.form.tagList.includes(tag.name))
								.map(tag => tag.id);
						} else {
							this.form.tagIds = [];
						}

						try {
							let response;
							if (this.isEditing) {
								response = this.isAdminAllScope 
									? await adminUpdateBlog(this.form) 
									: await updateUserManagementBlog(this.form);
							} else {
								response = this.isAdmin 
									? await adminSaveBlog(this.form) 
									: await createUserManagementBlog(this.form);
							}
							
							this.$message.success('保存成功');
							// Update the form with data from backend to reflect auto-generated cover
							if (response.data) {
								this.form = { ...this.form, ...response.data };
							}
							this.dialogVisible = false;
							this.$router.back();
						} catch (error) {
							// Error handling is managed by request interceptor, but can add specific logic here if needed.
						}
					} else {
						this.$message.error('请填写必要字段');
						return false;
					}
				});
			},
			handleCheckChange(data, checked, indeterminate) {
				if (checked) {
					this.form.cate = data.id
				}
			},
			getBlog(id) {
				(this.isAdminAllScope ? getBlogById(id) : getUserManagementBlogById(id)).then(res => {
					this.form = {
						...this.form,
						password: res.data.password,
						user: res.data.user,
						category: res.data.category,
						tags: res.data.tags,
						cate: res.data.category ? res.data.category.id : null,
						tagList: res.data.tags.map(tag => tag.name),
						categoryId: res.data.category ? res.data.category.id : null,
						tagIds: res.data.tags.map(t => t.id)
					}
					// Update radio based on fetched data
					if (res.data.password && res.data.password.length > 0) {
						this.radio = 3;
					} else {
						this.radio = 1;
					}
				});
			}
		}
	}
</script>

<style scoped>

</style>