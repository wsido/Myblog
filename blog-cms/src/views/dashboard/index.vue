<template>
	<div>
		<el-row class="panel-group" :gutter="50">
			<el-col :span="6">
				<el-card class="card-panel" body-style="padding: 0">
					<div class="card-panel-icon-wrapper">
						<SvgIcon icon-class="pv" class-name="card-panel-icon"/>
					</div>
					<div class="card-panel-description">
						<div class="card-panel-text">今日PV</div>
						<span class="card-panel-num">{{ pv }}</span>
					</div>
				</el-card>
			</el-col>

			<el-col :span="6">
				<el-card class="card-panel" body-style="padding: 0">
					<div class="card-panel-icon-wrapper">
						<SvgIcon icon-class="yonghu" class-name="card-panel-icon"/>
					</div>
					<div class="card-panel-description">
						<div class="card-panel-text">今日UV</div>
						<span class="card-panel-num">{{ uv }}</span>
					</div>
				</el-card>
			</el-col>

			<el-col :span="6">
				<el-card class="card-panel" body-style="padding: 0">
					<div class="card-panel-icon-wrapper">
						<SvgIcon icon-class="article" class-name="card-panel-icon"/>
					</div>
					<div class="card-panel-description">
						<div class="card-panel-text">文章数</div>
						<span class="card-panel-num">{{ blogCount }}</span>
					</div>
				</el-card>
			</el-col>

			<el-col :span="6">
				<el-card class="card-panel" body-style="padding: 0">
					<div class="card-panel-icon-wrapper">
						<SvgIcon icon-class="pinglun-blue" class-name="card-panel-icon"/>
					</div>
					<div class="card-panel-description">
						<div class="card-panel-text">评论数</div>
						<span class="card-panel-num">{{ commentCount }}</span>
					</div>
				</el-card>
			</el-col>
		</el-row>

		<el-row class="panel-group" :gutter="20">
			<el-col :span="12">
				<el-card>
					<div ref="categoryEcharts" style="height:500px;"></div>
				</el-card>
			</el-col>
			<el-col :span="12">
				<el-card>
					<div ref="tagEcharts" style="height:500px;"></div>
				</el-card>
			</el-col>
		</el-row>

		<el-card class="panel-group">
			<div ref="visitRecordEcharts" style="height:500px;"></div>
		</el-card>
	</div>
</template>

<script>
	import { getDashboard } from "@/api/dashboard";
import SvgIcon from "@/components/SvgIcon";
import echarts from 'echarts';

	export default {
		name: "Dashboard",
		components: {SvgIcon},
		data() {
			return {
				pv: 0,
				uv: 0,
				blogCount: 0,
				commentCount: 0,
				categoryEcharts: null,
				tagEcharts: null,
				visitRecordEcharts: null,
				categoryOption: {
					title: {
						text: '分类下文章数量',
						x: 'center'
					},
					tooltip: {
						trigger: 'item',
						formatter: '{a} <br/>{b} : {c} ({d}%)'
					},
					legend: {
						left: 'center',
						top: 'bottom',
						data: []
					},
					series: [
						{
							name: '文章数量',
							type: 'pie',
							radius: [30, 110],
							roseType: 'area',
							data: []
						}
					]
				},
				tagOption: {
					title: {
						text: '标签下文章数量',
						x: 'center'
					},
					tooltip: {
						trigger: 'item',
						formatter: '{a} <br/>{b} : {c} ({d}%)'
					},
					legend: {
						left: 'center',
						top: 'bottom',
						data: []
					},
					series: [
						{
							name: '文章数量',
							top: '-10%',
							type: 'pie',
							radius: [30, 110],
							roseType: 'area',
							data: []
						}
					]
				},
				visitRecordOption: {
					xAxis: {
						data: [],
						boundaryGap: false,
						axisTick: {
							show: false
						}
					},
					grid: {
						left: 10,
						right: 20,
						top: 30,
						bottom: 0,
						containLabel: true
					},
					tooltip: {
						trigger: 'axis',
						axisPointer: {
							type: 'cross'
						},
						padding: [5, 10]
					},
					yAxis: {
						axisTick: {
							show: false
						}
					},
					legend: {
						data: ['访问量(PV)', '独立访客(UV)']
					},
					series: [
						{
							name: '访问量(PV)',
							smooth: true,
							type: 'line',
							itemStyle: {
								normal: {
									color: '#FF005A',
									lineStyle: {
										color: '#FF005A',
										width: 2
									}
								}
							},
							data: [],
							animationDuration: 2800,
							animationEasing: 'cubicInOut'
						},
						{
							name: '独立访客(UV)',
							smooth: true,
							type: 'line',
							itemStyle: {
								normal: {
									color: '#3888fa',
									lineStyle: {
										color: '#3888fa',
										width: 2
									},
									areaStyle: {
										color: '#f3f8ff'
									}
								}
							},
							data: [],
							animationDuration: 2800,
							animationEasing: 'quadraticOut'
						}
					]
				},
			}
		},
		mounted() {
			this.getData()
		},
		methods: {
			getData() {
				getDashboard().then(res => {
					this.pv = res.data.pv
					this.uv = res.data.uv
					this.blogCount = res.data.blogCount
					this.commentCount = res.data.commentCount
					this.initCategoryEcharts(res.data.category)
					this.initTagEcharts(res.data.tag)
					this.initVisitRecordEcharts(res.data.visitRecord)
				})
			},
			initCategoryEcharts(categoryData) {
				if (categoryData && categoryData.legend && categoryData.series) {
					this.categoryOption.legend.data = categoryData.legend;
					this.categoryOption.series[0].data = categoryData.series;
				} else {
					this.categoryOption.legend.data = [];
					this.categoryOption.series[0].data = [];
					console.warn("Category data for ECharts is missing or in an unexpected format:", categoryData);
				}
				this.categoryEcharts = echarts.init(this.$refs.categoryEcharts, 'light')
				this.categoryEcharts.setOption(this.categoryOption)
			},
			initTagEcharts(tagData) {
				if (tagData && tagData.legend && tagData.series) {
					this.tagOption.legend.data = tagData.legend;
					this.tagOption.series[0].data = tagData.series;
				} else {
					this.tagOption.legend.data = [];
					this.tagOption.series[0].data = [];
					console.warn("Tag data for ECharts is missing or in an unexpected format:", tagData);
				}
				this.tagEcharts = echarts.init(this.$refs.tagEcharts, 'light')
				this.tagEcharts.setOption(this.tagOption)
			},
			initVisitRecordEcharts(visitRecord) {
				if (visitRecord && visitRecord.date && visitRecord.pv && visitRecord.uv) {
					this.visitRecordOption.xAxis.data = visitRecord.date
					this.visitRecordOption.series[0].data = visitRecord.pv
					this.visitRecordOption.series[1].data = visitRecord.uv
				}
				this.visitRecordEcharts = echarts.init(this.$refs.visitRecordEcharts)
				this.visitRecordEcharts.setOption(this.visitRecordOption)
			},
		}
	}
</script>

<style scoped>
	.panel-group {
		margin-bottom: 30px;
	}

	.panel-group .card-panel {
		height: 108px;
		position: relative;
		overflow: hidden;
	}

	.panel-group .card-panel .card-panel-icon-wrapper {
		float: left;
		margin: 14px 0 0 14px;
		padding: 16px;
	}

	.panel-group .card-panel .card-panel-icon {
		float: left;
		font-size: 48px;
	}

	.panel-group .card-panel .card-panel-description {
		float: right;
		font-weight: 700;
		margin: 26px 26px 26px 0;
	}

	.panel-group .card-panel .card-panel-description .card-panel-text {
		color: rgba(0, 0, 0, 0.45);
		font-size: 16px;
		margin-bottom: 12px;
	}

	.panel-group .card-panel .card-panel-description .card-panel-num {
		font-size: 20px;
	}
</style>