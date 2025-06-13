<template>
	<div>
		<el-form :model="form" label-position="top">
			<el-form-item label="动态内容" prop="content">
				<mavon-editor v-model="form.content"/>
			</el-form-item>

			<el-form-item label="点赞数" prop="likes" style="width: 50%">
				<el-input v-model="form.likes" type="number" placeholder="可选，默认为 0"></el-input>
			</el-form-item>

			<el-form-item label="创建时间" prop="createTime">
				<el-date-picker v-model="form.createTime" type="datetime" placeholder="可选，默认此刻" :editable="false"></el-date-picker>
			</el-form-item>

			<el-form-item style="text-align: right;">
				<el-button type="info" @click="submit(false)">仅自己可见</el-button>
				<el-button type="primary" @click="submit(true)">发布动态</el-button>
			</el-form-item>
		</el-form>
	</div>
</template>

<script>
	import Breadcrumb from "@/components/Breadcrumb";
	import {
		getMomentById, saveMoment, updateMoment,
		saveCurrentUserMoment, updateCurrentUserMoment, getCurrentUserMomentById,
		getAdminMomentById
	} from "@/api/moment";

	export default {
		name: "WriteMoment",
		components: {Breadcrumb},
		data() {
			return {
				form: {
					content: '',
					createTime: null,
					likes: 0,
					published: false
				},
			}
		},
		created() {
			if (this.$route.params.id) {
				// Determine scope for fetching initial data
				if (this.$route.meta && this.$route.meta.scope === 'currentUserCMS') {
					this.getCurrentUserMoment(this.$route.params.id);
				} else if (this.$route.meta && this.$route.meta.scope === 'all') { // Admin editing a moment from all moments list
					this.getAdminMoment(this.$route.params.id);
				} else {
					// Fallback or other scopes - potentially public view if implemented
					// For now, assume this might be an admin viewing without 'all' scope or a misconfiguration.
					// Defaulting to getMoment might be problematic if it targets a non-admin endpoint.
					// Considering the current 404, this path needs careful review if used.
					// Let's log an error for unexpected scope here.
					console.error('WriteMoment created: Unknown or unhandled scope for fetching moment by ID:', this.$route.meta.scope);
					// this.getMoment(this.$route.params.id); // Original call that might lead to 404
				}
			}
		},
		methods: {
			getMoment(id) { // This is the general getMoment, potentially for public or non-scoped views
				getMomentById(id).then(res => {
					this.form = res.data
				}).catch(err => {
					console.error('Error fetching moment with getMomentById:', err);
					this.msgError("获取动态信息失败");
				});
			},
			getCurrentUserMoment(id) {
				getCurrentUserMomentById(id).then(res => {
					this.form = res.data
				}).catch(err => {
					console.error('Error fetching current user moment:', err);
					this.msgError("获取您的动态信息失败");
				});
			},
			getAdminMoment(id) { // New method for admin to fetch any moment
				getAdminMomentById(id).then(res => {
					this.form = res.data;
				}).catch(err => {
					console.error('Error fetching admin moment by ID:', err);
					this.msgError("获取动态信息失败 (管理员)");
				});
			},
			submit(published) {
				this.form.published = published
				const isCurrentUserScope = this.$route.meta && this.$route.meta.scope === 'currentUserCMS';

				if (this.$route.params.id) { // Existing moment, so update
					if (isCurrentUserScope) {
						updateCurrentUserMoment(this.form).then(res => {
							this.msgSuccess(res.msg || '更新成功');
							this.$router.push('/my-blog/moment/list');
						}).catch(err => {
							console.error("Error updating current user moment:", err);
							// this.msgError(err.message || '更新失败'); // Already handled by global request error handler
						});
					} else { // Admin scope
						updateMoment(this.form).then(res => {
							this.msgSuccess(res.msg || '更新成功');
							// Admin might go to a different list view, adjust if needed
							this.$router.push(this.$route.meta.adminReturnRoute || '/admin/content-management/moment-list-all');
						}).catch(err => {
							console.error("Error updating admin moment:", err);
						});
					}
				} else { // New moment, so save
					if (isCurrentUserScope) {
						saveCurrentUserMoment(this.form).then(res => {
							this.msgSuccess(res.msg || '保存成功');
							this.$router.push('/my-blog/moment/list');
						}).catch(err => {
							console.error("Error saving current user moment:", err);
						});
					} else { // Admin scope
						saveMoment(this.form).then(res => {
							this.msgSuccess(res.msg || '保存成功');
							this.$router.push(this.$route.meta.adminReturnRoute || '/admin/content-management/moment-list-all');
						}).catch(err => {
							console.error("Error saving admin moment:", err);
						});
					}
				}
			}
		}
	}
</script>

<style scoped>

</style>