import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
//自定义css
import './assets/css/base.css'
//阿里icon
import './assets/css/icon/iconfont.css'
//typo.css
import "./assets/css/typo.css"
//semantic-ui
import 'semantic-ui-css/semantic.min.css'
//element-ui
import Element from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
//moment
import './util/dateTimeFormatUtils.js'
//v-viewer
import Viewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'
//directive
import './util/directive'
//懒加载
import VueLazyload from 'vue-lazyload'

console.log(
	'%c NBlog %c By wsido %c https://github.com/wsido/Myblog',
	'background:#35495e ; padding: 1px; border-radius: 3px 0 0 3px;  color: #fff',
	'background:#41b883 ; padding: 1px; border-radius: 0 3px 3px 0;  color: #000',
	'background:transparent'
)

Vue.use(Element)
Vue.use(Viewer)
Vue.use(VueLazyload, {
	preLoad: 1.2,
	loading: require('./assets/img/loading.gif'),
})

Vue.prototype.msgSuccess = function (msg) {
	this.$message.success(msg)
}

Vue.prototype.msgError = function (msg) {
	this.$message.error(msg)
}

Vue.prototype.msgInfo = function (msg) {
	this.$message.info(msg);
}

const cubic = value => Math.pow(value, 3);
const easeInOutCubic = value => value < 0.5 ? cubic(value * 2) / 2 : 1 - cubic((1 - value) * 2) / 2;
//滚动至页面顶部，使用 Element-ui 回到顶部 组件中的算法
Vue.prototype.scrollToTop = function () {
	const el = document.documentElement
	const beginTime = Date.now()
	const beginValue = el.scrollTop
	const rAF = window.requestAnimationFrame || (func => setTimeout(func, 16))
	const frameFunc = () => {
		const progress = (Date.now() - beginTime) / 500;
		if (progress < 1) {
			el.scrollTop = beginValue * (1 - easeInOutCubic(progress))
			rAF(frameFunc)
		} else {
			el.scrollTop = 0
		}
	}
	rAF(frameFunc)
}


Vue.config.productionTip = false

// 应用初始化时，尝试恢复用户登录状态
// store.state.userToken 已经在 state.js 中从 localStorage 初始化了
if (store.state.userToken) {
  store.dispatch('fetchUserInfo').catch(error => {
    console.error("App initialization: failed to fetch user info with token. Token might be invalid.", error);
    // fetchUserInfo action 内部会在失败时清除 token 和用户信息
  });
}

new Vue({
	router,
	store,
	render: h => h(App)
}).$mount('#app')
