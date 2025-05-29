<template>
	<div>
		<el-table :data="momentList">
			<el-table-column label="序号" type="index" width="50"></el-table-column>
			<el-table-column label="内容" prop="content" show-overflow-tooltip></el-table-column>
			<el-table-column label="发布状态" width="80">
				<template v-slot="scope">
					<el-switch v-model="scope.row.published" @change="momentPublishedChanged(scope.row)"></el-switch>
				</template>
			</el-table-column>
			<el-table-column label="点赞数" prop="likes" width="80"></el-table-column>
			<el-table-column label="创建时间" width="170">
				<template v-slot="scope">{{ scope.row.createTime | dateFormat }}</template>
			</el-table-column>
			<el-table-column label="操作" width="200">
				<template v-slot="scope">
					<el-button type="primary" icon="el-icon-edit" size="mini" @click="goEditMomentPage(scope.row.id)">编辑</el-button>
					<el-popconfirm title="确定删除吗？" icon="el-icon-delete" iconColor="red" @onConfirm="deleteMomentById(scope.row.id)">
						<el-button size="mini" type="danger" icon="el-icon-delete" slot="reference">删除</el-button>
					</el-popconfirm>
				</template>
			</el-table-column>
		</el-table>

		<!--分页-->
		<el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="queryInfo.pageNum"
		               :page-sizes="[10, 20, 30, 50]" :page-size="queryInfo.pageSize" :total="total"
		               layout="total, sizes, prev, pager, next, jumper" background>
		</el-pagination>
	</div>
</template>

<script>
	import Breadcrumb from "@/components/Breadcrumb";
	import {getMomentListByQuery, updatePublished, deleteMomentById, getCurrentUserMomentListByQuery, deleteCurrentUserMomentById} from "@/api/moment";
	import { mapGetters } from 'vuex';

	export default {
		name: "MomentList",
		components: {
			Breadcrumb
		},
		data() {
			return {
				queryInfo: {
					pageNum: 1,
					pageSize: 10
				},
				momentList: [],
				total: 0,
			}
		},
		computed: {
			...mapGetters(['roles']),
			isAdmin() {
				return this.roles && this.roles.includes('admin');
			},
			viewScope() {
				return this.$route.meta.scope;
			}
		},
		created() {
			this.getMomentList()
		},
		methods: {
			getMomentList() {
				// 根据用户角色和视图范围来决定使用哪个API
				const params = { ...this.queryInfo };
				
				if (this.viewScope === 'currentUser') {
					// 使用用户专属API获取当前用户的动态
					getCurrentUserMomentListByQuery(params).then(res => {
						this.momentList = res.data.list || res.data.blogs || res.data;
						this.total = res.data.total || (res.data.list ? res.data.list.length : res.data.length);
					}).catch(err => {
						// 如果专属API调用失败，尝试使用admin API并带上scope参数
						params.scope = 'currentUser';
						getMomentListByQuery(params).then(res => {
							this.momentList = res.data.list;
							this.total = res.data.total;
						});
					});
				} else {
					// 对管理员视图，可以加上额外参数以获取全部动态
					if (this.isAdmin && this.viewScope === 'all') {
						params.scope = 'all';
					}
					
					getMomentListByQuery(params).then(res => {
						this.momentList = res.data.list;
						this.total = res.data.total;
					});
				}
			},
			//监听 pageSize 改变事件
			handleSizeChange(newSize) {
				this.queryInfo.pageSize = newSize
				this.getMomentList()
			},
			//监听页码改变的事件
			handleCurrentChange(newPage) {
				this.queryInfo.pageNum = newPage
				this.getMomentList()
			},
			momentPublishedChanged(row) {
				updatePublished(row.id, row.published).then(res => {
					this.msgSuccess(res.msg)
				})
			},
			goEditMomentPage(id) {
				// 根据不同的路由来源修改跳转路径
				if (this.viewScope === 'currentUser') {
					this.$router.push(`/my-blog/moment/edit/${id}`);
				} else {
					this.$router.push(`/blog/moment/edit/${id}`);
				}
			},
			deleteMomentById(id) {
				// 根据视图范围决定使用哪个删除API
				const deleteFn = this.viewScope === 'currentUser' ? 
					deleteCurrentUserMomentById : 
					deleteMomentById;
				
				deleteFn(id).then(res => {
					this.msgSuccess(res.msg)
					this.getMomentList()
				}).catch(err => {
					// 如果专属API调用失败，尝试使用admin API
					if (this.viewScope === 'currentUser') {
						deleteMomentById(id).then(res => {
							this.msgSuccess(res.msg)
							this.getMomentList()
						});
					}
				});
			}
		}
	}
</script>

<style scoped>
	.el-button + span {
		margin-left: 10px;
	}
</style>