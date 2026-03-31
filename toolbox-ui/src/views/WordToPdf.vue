<template>
  <div class="page-container">
    <!-- 说明卡片 -->
    <el-card class="info-card" shadow="never">
      <el-alert
        title="转换说明"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          上传 Word 文件（.docx 或 .doc）后，系统将自动转换为 PDF 文档。转换完成后将自动下载。
          <br />
          <strong>注意：</strong>复杂排版（如特殊字体、复杂表格等）可能无法完全保留格式。
        </template>
      </el-alert>
    </el-card>

    <!-- 上传区域 -->
    <el-card class="upload-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Upload /></el-icon>
          <span>上传 Word 文件</span>
        </div>
      </template>

      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".docx,.doc"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        :file-list="fileList"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          <p>将 Word 文件拖到此处，或 <em>点击上传</em></p>
          <p class="upload-tip">支持 .docx 和 .doc 格式文件</p>
        </div>
      </el-upload>

      <!-- 文件信息 -->
      <div v-if="selectedFile" class="file-info">
        <el-icon color="#67C23A"><Document /></el-icon>
        <div class="file-meta">
          <span class="file-name">{{ selectedFile.name }}</span>
          <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
        </div>
      </div>

      <div class="action-buttons">
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          :disabled="!selectedFile"
          @click="handleConvert"
        >
          <el-icon><Download /></el-icon>
          开始转换并下载
        </el-button>
        <el-button size="large" @click="handleReset" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>

    <!-- 转换状态 -->
    <el-card v-if="loading || status" class="status-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>转换状态</span>
        </div>
      </template>

      <div class="status-content">
        <div v-if="loading" class="status-loading">
          <el-icon class="is-loading" size="40" color="#409EFF">
            <Loading />
          </el-icon>
          <div class="status-text">
            <p class="status-main">正在转换，请稍候...</p>
            <p class="status-sub">Word 文件较大时转换可能需要较长时间</p>
          </div>
        </div>

        <div v-else-if="status === 'success'" class="status-result status-success">
          <el-icon size="48" color="#67C23A"><CircleCheckFilled /></el-icon>
          <div class="status-text">
            <p class="status-main">转换成功！</p>
            <p class="status-sub">PDF 文件已开始下载，请检查浏览器下载列表</p>
          </div>
        </div>

        <div v-else-if="status === 'error'" class="status-result status-error">
          <el-icon size="48" color="#F56C6C"><CircleCloseFilled /></el-icon>
          <div class="status-text">
            <p class="status-main">转换失败</p>
            <p class="status-sub">{{ errorMsg }}</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 使用说明 -->
    <el-card class="guide-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>使用步骤</span>
        </div>
      </template>
      <el-steps :active="stepActive" align-center finish-status="success">
        <el-step title="上传 Word" description="点击或拖拽 Word 文件到上传区域" />
        <el-step title="开始转换" description="点击【开始转换并下载】按钮" />
        <el-step title="下载 PDF" description="转换完成后自动下载 .pdf 文件" />
      </el-steps>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { wordToPdf } from '../api/pdf'

const uploadRef = ref(null)
const loading = ref(false)
const selectedFile = ref(null)
const fileList = ref([])
const status = ref('')  // '' | 'success' | 'error'
const errorMsg = ref('')

const stepActive = computed(() => {
  if (status.value === 'success') return 3
  if (selectedFile.value) return 1
  return 0
})

function handleFileChange(file) {
  selectedFile.value = file.raw
  status.value = ''
  errorMsg.value = ''
}

function handleExceed() {
  ElMessage.warning('每次只能上传一个 Word 文件，请先删除已有文件')
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

async function handleConvert() {
  if (!selectedFile.value) return
  loading.value = true
  status.value = ''
  errorMsg.value = ''
  try {
    const res = await wordToPdf(selectedFile.value)

    // 检查是否为 JSON 错误响应
    if (res.data.type === 'application/json') {
      const text = await res.data.text()
      const json = JSON.parse(text)
      status.value = 'error'
      errorMsg.value = json.message || '转换失败'
      ElMessage.error(errorMsg.value)
      return
    }

    const fileName = selectedFile.value.name.replace(/\.(docx|doc)$/i, '.pdf')
    triggerDownload(res.data, fileName)
    status.value = 'success'
    ElMessage.success('转换成功，文件已下载')
  } catch (e) {
    status.value = 'error'
    errorMsg.value = e.message || '请求失败，请检查网络或后端服务'
    ElMessage.error(errorMsg.value)
  } finally {
    loading.value = false
  }
}

function triggerDownload(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  a.click()
  URL.revokeObjectURL(url)
}

function handleReset() {
  uploadRef.value?.clearFiles()
  selectedFile.value = null
  fileList.value = []
  status.value = ''
  errorMsg.value = ''
}
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: #1a2332;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload) {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed #d0d7de;
  border-radius: 12px;
  background: #f8fafc;
  transition: all 0.3s;
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: #409EFF;
  background: #f0f7ff;
}

.upload-icon {
  font-size: 52px;
  color: #409EFF;
  margin-bottom: 12px;
}

.upload-text p {
  font-size: 14px;
  color: #606266;
  margin: 4px 0;
}

.upload-text em {
  color: #409EFF;
  font-style: normal;
}

.upload-tip {
  color: #909399 !important;
  font-size: 12px !important;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 12px 16px;
  background: #f0f9eb;
  border-radius: 8px;
  border: 1px solid #d1edc4;
}

.file-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a2332;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.status-content {
  padding: 12px 0;
}

.status-loading,
.status-result {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  border-radius: 10px;
}

.status-loading {
  background: #ecf5ff;
}

.status-success {
  background: #f0f9eb;
}

.status-error {
  background: #fef0f0;
}

.status-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-main {
  font-size: 16px;
  font-weight: 600;
  color: #1a2332;
}

.status-sub {
  font-size: 13px;
  color: #606266;
}

.guide-card :deep(.el-steps) {
  padding: 10px 0;
}
</style>
