<template>
	<div id="app">
		<router-view/>
	</div>
</template>

<script>
	export default {
		name: 'App',
		mounted() {
			// 全局处理CDN资源加载错误
			window.addEventListener('error', (e) => {
				// 仅处理图片、CSS等资源加载错误，不处理JS错误
				if (e.target && (e.target.nodeName === 'IMG' || e.target.nodeName === 'LINK')) {
					console.warn('资源加载失败:', e.target.src || e.target.href)
					
					// 处理特定路径的图片
					if (e.target.nodeName === 'IMG') {
						e.target.onerror = null // 防止循环触发
						
						// 如果是用户头像或作者图片加载失败，使用默认头像
						if (e.target.src.includes('author.jpg') || e.target.src.includes('/user/')) {
							e.target.src = '/img/avatar.jpg' // 使用本地默认头像
						} 
						// 如果是其他CDN图片加载失败
						else if (e.target.src.includes('cdn.wsido.top')) {
							// 可以设置某种默认图片
							e.target.src = '/img/avatar.jpg'
						}
					}
					// 阻止错误传播到控制台
					e.preventDefault()
				}
			}, true)
		}
	}
</script>

<style>

</style>
