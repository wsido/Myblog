<template>
	<div>
		<!--搜索-->
		<el-row>
			<el-col :span="6">
				<el-select v-model="pageId" placeholder="请选择页面" :filterable="true" :clearable="true" @change="search" size="small" style="min-width: 400px">
					<el-option :label="item.title" :value="item.id" v-for="item in blogList" :key="item.id"></el-option>
				</el-select>
			</el-col>
		</el-row>

		<el-table :data="commentList" row-key="id" :tree-props="{children: 'replyComments'}" :indent="0">
			<el-table-column label="评论ID" prop="id"></el-table-column>
			<el-table-column label="头像" width="70">
				<template v-slot="scope">
					<el-avatar shape="square" :size="50" fit="contain" :src="scope.row.avatar"></el-avatar>
				</template>
			</el-table-column>
			<el-table-column label="昵称" prop="nickname">
				<template v-slot="scope">
					{{ scope.row.nickname }}
					<el-tag v-if="scope.row.adminComment" size="mini" effect="dark" style="margin-left: 5px">我</el-tag>
				</template>
			</el-table-column>
			<el-table-column label="邮箱" prop="email" show-overflow-tooltip></el-table-column>
			<el-table-column label="网站" prop="website" show-overflow-tooltip></el-table-column>
			<el-table-column label="IP" prop="ip" width="130"></el-table-column>
			<el-table-column label="评论内容" prop="content" show-overflow-tooltip></el-table-column>
			<el-table-column label="QQ" prop="qq" width="115"></el-table-column>
			<el-table-column label="所在页面" show-overflow-tooltip>
				<template v-slot="scope">
					<el-link type="success" :href="`/blog/${scope.row.blog.id}`" target="_blank" v-if="scope.row.page===0">{{ scope.row.blog.title }}</el-link>
					<el-link type="success" :href="'/about'" target="_blank" v-else-if="scope.row.page===1">关于我</el-link>
					<el-link type="success" :href="'/friends'" target="_blank" v-else-if="scope.row.page===2">友人帐</el-link>
				</template>
			</el-table-column>
			<el-table-column label="发表时间" width="170">
				<template v-slot="scope">{{ scope.row.createTime | dateFormat }}</template>
			</el-table-column>
			<el-table-column label="是否公开" width="80">
				<template v-slot="scope">
					<el-switch v-model="scope.row.published" @change="commentPublishedChanged(scope.row)"></el-switch>
				</template>
			</el-table-column>
			<el-table-column label="邮件提醒" width="80">
				<template v-slot="scope">
					<el-switch v-model="scope.row.notice" @change="commentNoticeChanged(scope.row)"></el-switch>
				</template>
			</el-table-column>
			<el-table-column label="操作" width="200">
				<template v-slot="scope">
					<el-button type="primary" icon="el-icon-edit" size="mini" @click="showEditDialog(scope.row)">编辑</el-button>
					<el-button type="danger" icon="el-icon-delete" size="mini" @click="deleteCommentById(scope.row.id)">删除</el-button>
				</template>
			</el-table-column>
		</el-table>

		<!--分页-->
		<el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="queryInfo.pageNum"
		               :page-sizes="[10, 20, 30, 50]" :page-size="queryInfo.pageSize" :total="total"
		               layout="total, sizes, prev, pager, next, jumper" background>
		</el-pagination>

		<!--编辑评论对话框-->
		<el-dialog title="编辑评论" width="50%" :visible.sync="editDialogVisible" :close-on-click-modal="false" @close="editDialogClosed">
			<!--内容主体-->
			<el-form :model="editForm" :rules="formRules" ref="editFormRef" label-width="80px">
				<el-form-item label="昵称" prop="nickname">
					<el-input v-model="editForm.nickname"></el-input>
				</el-form-item>
				<el-form-item label="头像" prop="avatar">
					<el-input v-model="editForm.avatar"></el-input>
				</el-form-item>
				<el-form-item label="邮箱" prop="email">
					<el-input v-model="editForm.email"></el-input>
				</el-form-item>
				<el-form-item label="网站" prop="website">
					<el-input v-model="editForm.website"></el-input>
				</el-form-item>
				<el-form-item label="IP" prop="ip">
					<el-input v-model="editForm.ip"></el-input>
				</el-form-item>
				<el-form-item label="评论内容" prop="content">
					<el-input v-model="editForm.content" type="textarea" maxlength="250" :rows="5" show-word-limit></el-input>
				</el-form-item>
			</el-form>
			<!--底部-->
			<span slot="footer">
				<el-button @click="editDialogVisible=false">取 消</el-button>
				<el-button type="primary" @click="editComment">确 定</el-button>
			</span>
		</el-dialog>
	</div>
</template>

<script>
	import { 
		deleteCommentById, 
		editComment, 
		getBlogList, 
		getCommentListByQuery, 
		updateNotice, 
		updatePublished,
		getCurrentUserCommentListByQuery,
		deleteCurrentUserCommentById,
		editCurrentUserComment,
		updateCurrentUserCommentPublished,
		updateCurrentUserCommentNotice 
	} from '@/api/comment';
import Breadcrumb from "@/components/Breadcrumb";
import { checkEmail } from "@/util/reg";
import { mapGetters } from 'vuex';

	export default {
		name: "CommentList",
		components: {
			Breadcrumb
		},
		data() {
			return {
				pageId: null,
				queryInfo: {
					page: null,
					blogId: null,
					pageNum: 1,
					pageSize: 10
				},
				total: 0,
				commentList: [],
				blogList: [],
				editDialogVisible: false,
				editForm: {
					id: null,
					nickname: '',
					avatar: '',
					email: '',
					website: null,
					ip: '',
					content: ''
				},
				formRules: {
					nickname: [{required: true, message: '请输入评论昵称', trigger: 'blur'}],
					avatar: [{required: true, message: '请输入评论头像', trigger: 'blur'}],
					email: [
						{required: true, message: '请输入评论邮箱', trigger: 'blur'},
						{validator: checkEmail, trigger: 'blur'}
					],
					ip: [
						{required: true, message: '请输入评论ip', trigger: 'blur'},
						// {validator: checkIpv4, trigger: 'blur'}
					],
					content: [
						{required: true, message: '请输入评论内容', trigger: 'blur'},
						{max: 255, message: '评论内容不可多于255个字符', trigger: 'blur'}
					],
				}
			}
		},
		computed: {
			...mapGetters(['roles', 'userId']),
			isAdmin() {
				return this.roles && this.roles.includes('admin');
			},
			viewScope() {
				return this.$route.meta.scope;
			}
		},
		created() {
			this.getCommentList()
			this.getBlogList()
		},
		methods: {
			getCommentList() {
				const params = { ...this.queryInfo }
				if (this.viewScope === 'currentUserCMS') {
					getCurrentUserCommentListByQuery(params).then(res => {
						if (res.code === 200 && res.data) {
							this.commentList = res.data.list; // 确保从PageInfo获取列表
							this.total = res.data.total;   // 确保从PageInfo获取总数
						} else {
							this.commentList = [];
							this.total = 0;
							// this.msgError(res.msg || '获取我的评论列表失败'); // 可以选择性地提示用户
						}
					}).catch(err => {
						console.error("Error fetching current user comments:", err);
						this.commentList = [];
						this.total = 0;
						// this.msgError('获取我的评论列表失败，请检查网络或联系管理员');
					});
				} else {
					// 对管理员视图，可以加上额外参数以获取全部评论
					if (this.isAdmin && this.viewScope === 'all') {
						params.scope = 'all';
					}
					
					getCommentListByQuery(params).then(res => {
						if (res.code === 200 && res.data && res.data.comments) {
							this.commentList = res.data.comments.list; // 正确路径
							this.total = res.data.allComment; // 正确路径
						} else {
							this.commentList = [];
							this.total = 0;
							// this.msgError(res.msg || '获取评论列表失败');
						}
					}).catch(err => {
						console.error("Error fetching comments for admin view:", err);
						this.commentList = [];
						this.total = 0;
						// this.msgError('获取评论列表失败，请检查网络或联系管理员');
					});
				}
			},
			getBlogList() {
				getBlogList().then(res => {
					this.blogList = res.data
					this.blogList.unshift({id: -2, title: '友人帐'})
					this.blogList.unshift({id: -1, title: '关于我'})
				})
			},
			search() {
				if (this.pageId === '') {
					this.queryInfo.page = null
					this.queryInfo.blogId = null
				} else if (this.pageId === -1) {
					this.queryInfo.page = 1
					this.queryInfo.blogId = null
				} else if (this.pageId === -2) {
					this.queryInfo.page = 2
					this.queryInfo.blogId = null
				} else {
					this.queryInfo.page = 0
					this.queryInfo.blogId = this.pageId
				}
				this.queryInfo.pageNum = 1
				this.queryInfo.pageSize = 10
				this.getCommentList()
			},
			//监听 pageSize 改变事件
			handleSizeChange(newSize) {
				this.queryInfo.pageSize = newSize
				this.getCommentList()
			},
			//监听页码改变事件
			handleCurrentChange(newPage) {
				this.queryInfo.pageNum = newPage
				this.getCommentList()
			},
			//切换评论公开状态（如果切换成隐藏，则该评论的所有子评论都修改为同样的隐藏状态）
			commentPublishedChanged(row) {
				// 根据视图范围决定使用哪个更新API
				const updateFn = this.viewScope === 'currentUser' ? 
					updateCurrentUserCommentPublished : 
					updatePublished;
					
				if (row.published) {
					updateFn(row.id, row.published).then(res => {
						this.msgSuccess(res.msg)
					}).catch(err => {
						// 如果专属API调用失败，尝试使用admin API
						if (this.viewScope === 'currentUser') {
							updatePublished(row.id, row.published).then(res => {
								this.msgSuccess(res.msg)
							});
						}
					});
				} else {
					//切换成隐藏状态
					let replyCommentList = []
					replyCommentList.push(row)
					this.getAllReplyCommentList(row, replyCommentList)

					updateFn(row.id, row.published).then(res => {
						this.msgSuccess(res.msg)
						replyCommentList.forEach(comment => {
							comment.published = row.published
						})
					}).catch(err => {
						// 如果专属API调用失败，尝试使用admin API
						if (this.viewScope === 'currentUser') {
							updatePublished(row.id, row.published).then(res => {
								this.msgSuccess(res.msg)
								replyCommentList.forEach(comment => {
									comment.published = row.published
								})
							});
						}
					});
				}
			},
			//递归展开所有子评论
			getAllReplyCommentList(comment, replyCommentList) {
				comment.replyComments.forEach(replyComment => {
					replyCommentList.push(replyComment)
					this.getAllReplyCommentList(replyComment, replyCommentList)
				})
			},
			//切换评论邮件提醒状态
			commentNoticeChanged(row) {
				// 根据视图范围决定使用哪个更新API
				const updateFn = this.viewScope === 'currentUser' ? 
					updateCurrentUserCommentNotice : 
					updateNotice;
					
				updateFn(row.id, row.notice).then(res => {
					this.msgSuccess(res.msg);
				}).catch(err => {
					// 如果专属API调用失败，尝试使用admin API
					if (this.viewScope === 'currentUser') {
						updateNotice(row.id, row.notice).then(res => {
							this.msgSuccess(res.msg);
						});
					}
				});
			},
			deleteCommentById(id) {
				this.$confirm('此操作将永久删除该评论<strong style="color: red">及其所有子评论</strong>，是否删除?', '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning',
					dangerouslyUseHTMLString: true
				}).then(() => {
					const deleteFn = this.viewScope === 'currentUserCMS' ? 
						deleteCurrentUserCommentById :
						deleteCommentById;
						
					deleteFn(id).then(res => {
						if (res.code === 200) {
							this.msgSuccess(res.msg || '删除成功');
						} else {
							this.msgError(res.msg || '删除失败');
						}
						this.getCommentList();
					}).catch(err => {
						console.error("Error deleting comment:", err);
						// No fallback to admin API for currentUserCMS scope
						// For admin scope (else part of deleteFn determination), an error here is just an error.
						if (err.response && err.response.data && err.response.data.msg) {
							this.msgError(err.response.data.msg);
						} else if (err.message) {
							this.msgError(err.message);
						} else {
							this.msgError('删除评论失败，请重试');
						}
						// Optionally, refresh the list even on error if state might be inconsistent
						// this.getCommentList(); 
					});
				}).catch(() => {
					this.$message({
						type: 'info',
						message: '已取消删除'
					});
				});
			},
			showEditDialog(row) {
				this.editForm = {...row}
				this.editDialogVisible = true
			},
			editDialogClosed() {
				this.editForm = {}
				this.$refs.editFormRef.resetFields()
			},
			editComment() {
				this.$refs.editFormRef.validate(valid => {
					if (valid) {
						const form = {
							id: this.editForm.id,
							nickname: this.editForm.nickname,
							avatar: this.editForm.avatar,
							email: this.editForm.email,
							website: this.editForm.website,
							ip: this.editForm.ip,
							content: this.editForm.content,
						}
						
						// 根据视图范围决定使用哪个编辑API
						const editFn = this.viewScope === 'currentUser' ? 
							editCurrentUserComment : 
							editComment;
							
						editFn(form).then(res => {
							this.msgSuccess(res.msg)
							this.editDialogVisible = false
							this.getCommentList()
						}).catch(err => {
							// 如果专属API调用失败，尝试使用admin API
							if (this.viewScope === 'currentUser') {
								editComment(form).then(res => {
									this.msgSuccess(res.msg)
									this.editDialogVisible = false
									this.getCommentList()
								});
							}
						});
					}
				})
			}
		}
	}
</script>

<style scoped>

</style>