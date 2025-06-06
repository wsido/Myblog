<template>
	<!-- 评论输入表单 -->
	<div class="form">
		<h3>
			发表评论
			<span v-if="isUserLoggedIn" class="logged-in-user">以 {{ loggedInUser.nickname }} ({{ loggedInUser.email }}) 的身份</span>
			<el-button class="m-small" size="mini" type="primary" @click="cancelReply" v-show="parentCommentId!==-1">取消回复</el-button>
		</h3>
		<el-form :inline="true" :model="commentForm" :rules="formRules" ref="formRef" size="small">
			<el-input :class="{'textarea':true}" type="textarea" :rows="5" v-model="commentForm.content" placeholder="评论千万条，友善第一条"
			          maxlength="250" show-word-limit :validate-event="false"></el-input>
			<div class="el-form-item el-form-item--small emoji">
				<img src="https://cdn.wsido.top/blog/img/paopao/1.png" @click="showEmojiBox">
				<div class="mask" v-show="emojiShow" @click="hideEmojiBox"></div>
				<div class="emoji-box" v-show="emojiShow">
					<div class="emoji-title">
						<span>{{ activeEmojiTab === 0 ? 'tv_小电视' : activeEmojiTab === 1 ? '阿鲁' : '泡泡' }}</span>
					</div>
					<div class="emoji-wrap" v-show="activeEmojiTab===0">
						<div class="emoji-list" v-for="(img,index) in tvMapper" :key="index" @click="insertEmoji(img.name)">
							<img :src="img.src" :title="img.name">
						</div>
					</div>
					<div class="emoji-wrap" v-show="activeEmojiTab===1">
						<div class="emoji-list" v-for="(img,index) in aruMapper" :key="index" @click="insertEmoji(img.name)">
							<img :src="img.src" :title="img.name">
						</div>
					</div>
					<div class="emoji-wrap" v-show="activeEmojiTab===2">
						<div class="emoji-list" v-for="(img,index) in paopaoMapper" :key="index" @click="insertEmoji(img.name)">
							<img :src="img.src" :title="img.name">
						</div>
					</div>
					<div class="emoji-tabs">
						<a class="tab-link" :class="{'on':activeEmojiTab===0}" @click="activeEmojiTab=0">
							<img src="https://cdn.wsido.top/blog/img/tv/1.png">
						</a>
						<a class="tab-link" :class="{'on':activeEmojiTab===1}" @click="activeEmojiTab=1">
							<img src="https://cdn.wsido.top/blog/img/aru/1.png">
						</a>
						<a class="tab-link" :class="{'on':activeEmojiTab===2}" @click="activeEmojiTab=2">
							<img src="https://cdn.wsido.top/blog/img/paopao/1.png">
						</a>
					</div>
				</div>
			</div>
			
			<el-form-item prop="nickname">
				<el-input v-model="commentForm.nickname" placeholder="昵称（必填）" :validate-event="false" :disabled="isUserLoggedIn">
					<i slot="prefix" class="el-input__icon el-icon-user"></i>
				</el-input>
			</el-form-item>
			<el-form-item prop="email">
				<el-popover ref="emailPopover" placement="bottom" trigger="focus" content="用于接收回复邮件"></el-popover>
				<el-input v-model="commentForm.email" placeholder="邮箱（必填）" :validate-event="false" v-popover:emailPopover :disabled="isUserLoggedIn">
					<i slot="prefix" class="el-input__icon el-icon-message"></i>
				</el-input>
			</el-form-item>
			
			<el-form-item label="订阅回复">
				<el-switch v-model="commentForm.notice"></el-switch>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" size="medium" v-throttle="[postForm,'click',3000]">发表评论</el-button>
			</el-form-item>
		</el-form>
	</div>
</template>

<script>
import { checkEmail, checkUrl } from "@/common/reg";
import aruMapper from '@/plugins/aruMapper.json';
import paopaoMapper from '@/plugins/paopaoMapper.json';
import tvMapper from '@/plugins/tvMapper.json';
import { SET_PARENT_COMMENT_ID, UPDATE_COMMENT_FORM_FIELD } from "@/store/mutations-types";
import { mapState, mapGetters } from 'vuex';

export default {
	name: "CommentForm",
	computed: {
		...mapState(['parentCommentId', 'commentForm', 'commentQuery', 'userToken', 'user']),
		...mapGetters(['isUserLoggedIn', 'loggedInUser']),
	},
	data() {
		const validateNickname = (rule, value, callback) => {
			if (this.isUserLoggedIn) return callback();
			if (!value) return callback(new Error('请输入评论昵称'));
			if (value.length > 18) return callback(new Error('昵称不可多于18个字符'));
			callback();
		};
		const validateEmailField = (rule, value, callback) => {
			if (this.isUserLoggedIn) return callback();
			if (!value) return callback(new Error('请输入评论邮箱'));
			checkEmail(rule, value, callback);
		};
		const validateWebsiteField = (rule, value, callback) => {
			if (value) return checkUrl(rule, value, callback);
			callback();
		};
		return {
			formRules: {
				nickname: [{ validator: validateNickname, trigger: 'blur' }],
				email: [{ validator: validateEmailField, trigger: 'blur' }],
			},
			emojiShow: false,
			activeEmojiTab: 0,
			tvMapper: [],
			aruMapper: [],
			paopaoMapper: [],
			textarea: null,
			start: 0,
			end: 0,
		}
	},
	watch: {
		loggedInUser: {
			immediate: true,
			handler(currentUser) {
				if (this.isUserLoggedIn && currentUser) {
					this.$store.commit(UPDATE_COMMENT_FORM_FIELD, { field: 'nickname', value: currentUser.nickname || '' });
					this.$store.commit(UPDATE_COMMENT_FORM_FIELD, { field: 'email', value: currentUser.email || '' });
				}
			}
		}
	},
	created() {
		this.tvMapper = tvMapper;
		this.aruMapper = aruMapper;
		this.paopaoMapper = paopaoMapper;
	},
	mounted() {
		this.textarea = document.querySelector('.el-form textarea');
	},
	methods: {
		cancelReply() {
			this.$store.commit(SET_PARENT_COMMENT_ID, -1);
		},
		showEmojiBox() {
			this.start = this.textarea.selectionStart;
			this.end = this.textarea.selectionEnd;
			this.textarea.focus();
			this.textarea.setSelectionRange(this.start, this.end);
			this.emojiShow = !this.emojiShow;
		},
		insertEmoji(name) {
			let str = this.commentForm.content;
			this.commentForm.content = str.substring(0, this.start) + name + str.substring(this.end);
			this.start += name.length;
			this.end = this.start;
			this.textarea.focus();
			this.$nextTick(() => {
				this.textarea.setSelectionRange(this.start, this.end);
			});
		},
		hideEmojiBox() {
			this.emojiShow = false;
			this.textarea.focus();
			this.textarea.setSelectionRange(this.start, this.end);
		},
		postForm() {
			let tokenToUse = '';
			if (this.isUserLoggedIn && this.userToken) {
				tokenToUse = this.userToken;
			} else {
				const adminToken = window.localStorage.getItem('adminToken');
				if (adminToken) {
					tokenToUse = adminToken;
				} else {
					const blogToken = window.localStorage.getItem(`blog${this.commentQuery.blogId}`);
					tokenToUse = blogToken || '';
				}
			}

			if (!this.commentForm.content || this.commentForm.content.trim() === '' || this.commentForm.content.length > 250) {
				return this.$notify({ title: '评论失败', message: '评论内容有误', type: 'warning' });
			}

			if (!this.isUserLoggedIn && !window.localStorage.getItem('adminToken')) {
				this.$refs.formRef.validate(valid => {
					if (!valid) {
						this.$notify({ title: '评论失败', message: '请正确填写评论', type: 'warning' });
					} else {
						this.$store.dispatch('submitCommentForm', tokenToUse);
					}
				});
			} else {
				this.$store.dispatch('submitCommentForm', tokenToUse);
			}
		}
	}
}
</script>

<style>
.form {
	background: #fff;
	position: relative;
}

.form h3 {
	margin: 5px;
	font-weight: 500 !important;
}

.form .m-small {
	margin-left: 5px;
	padding: 4px 5px;
}

.el-form .textarea {
	margin-top: 5px;
	margin-bottom: 15px;
}

.el-form textarea {
	padding: 6px 8px;
}

.el-form textarea, .el-form input {
	color: black;
}

.el-form .el-form-item__label {
	padding-right: 3px;
}

.emoji {
	margin-right: 5px;
	position: relative;
	user-select: none;
}

.emoji > img {
	cursor: pointer;
	transition: all 0.3s ease-in-out;
	-webkit-transition: all 0.3s ease-in-out;
	-moz-transition: all 0.3s ease-in-out;
	-o-transition: all 0.3s ease-in-out;
}

.emoji > img:hover {
	transform: rotate(360deg);
	-webkit-transform: rotate(360deg);
	-moz-transform: rotate(360deg);
	-o-transform: rotate(360deg);
}

.emoji-box {
	color: #222;
	overflow: visible;
	background: #fff;
	border: 1px solid #E5E9EF;
	box-shadow: 0 11px 12px 0 rgba(106, 115, 133, 0.3);
	border-radius: 8px;
	width: 340px;
	position: absolute;
	top: 40px;
	z-index: 100;
}

.emoji-box * {
	box-sizing: content-box;
}

.emoji-box .emoji-title {
	font-size: 12px;
	line-height: 16px;
	margin: 13px 15px 0;
	color: #757575;
}

.emoji-box .emoji-wrap {
	margin: 6px 11px 0 11px;
	height: 185px;
	overflow: auto;
	word-break: break-word;
}

.emoji-box .emoji-wrap .emoji-list {
	height: 33px;
	color: #111;
	border-radius: 4px;
	transition: background 0.2s;
	display: inline-block;
	outline: 0;
	cursor: pointer;
}

.emoji-box .emoji-wrap .emoji-list:hover {
	background-color: #ddd;
}

.emoji-box .emoji-wrap .emoji-list img {
	margin: 4px;
	width: 25px;
	height: 25px;
}

.emoji-box .emoji-tabs {
	position: relative;
	height: 36px;
	overflow: hidden;
	background-color: #f4f4f4;
	border-radius: 0 0 4px 4px;
}

.emoji-box .emoji-tabs .tab-link {
	cursor: pointer;
	float: left;
	padding: 7px 18px;
	width: 22px;
	height: 22px;
}

.emoji-box .emoji-tabs .tab-link.on {
	background-color: #fff;
}

.emoji-box .emoji-tabs .tab-link img {
	width: 22px;
}

.emoji-box .emoji-tabs .tab-link:hover {
	background: #e7e7e7;
}

.mask {
	pointer-events: auto;
	position: fixed;
	z-index: 99;
	top: 0;
	bottom: 0;
	left: 0;
	right: 0;
}

.logged-in-user {
	font-size: 0.9em;
	color: #555;
	margin-left: 10px;
	font-weight: normal;
}

.el-input.is-disabled .el-input__inner {
    background-color: #f5f7fa !important;
    border-color: #e4e7ed !important;
    color: #c0c4cc !important;
    cursor: not-allowed !important;
}
</style>
