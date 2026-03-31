<template>
  <div class="page-container">
    <!-- 配置区域 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Setting /></el-icon>
          <span>转换设置</span>
        </div>
      </template>

      <el-form :model="form" label-width="90px" class="config-form">
        <el-form-item label="图片格式">
          <el-radio-group v-model="form.format">
            <el-radio value="png">PNG（无损）</el-radio>
            <el-radio value="jpeg">JPEG（较小）</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="图片 DPI">
          <el-slider
            v-model="form.dpi"
            :min="72"
            :max="300"
            :step="1"
            :marks="dpiMarks"
            show-input
            style="width: 380px"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 上传区域 -->
    <el-card class="upload-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Upload /></el-icon>
          <span>上传 PDF 文件</span>
        </div>
      </template>

      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".pdf"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        :file-list="fileList"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          <p>将 PDF 文件拖到此处，或 <em>点击上传</em></p>
          <p class="upload-tip">仅支持 .pdf 格式文件</p>
        </div>
      </el-upload>

      <div class="action-buttons">
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          :disabled="!selectedFile"
          @click="handleConvert"
        >
          <el-icon><View /></el-icon>
          预览转换结果
        </el-button>
        <el-button
          type="success"
          size="large"
          :loading="downloading"
          :disabled="!selectedFile"
          @click="handleDownload"
        >
          <el-icon><Download /></el-icon>
          下载 ZIP 压缩包
        </el-button>
        <el-button size="large" @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>

    <!-- 结果区域 -->
    <el-card v-if="images.length > 0" class="result-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Picture /></el-icon>
          <span>转换结果</span>
          <el-tag type="success" style="margin-left: 10px">共 {{ images.length }} 页</el-tag>
          <el-button
            type="primary"
            plain
            size="small"
            style="margin-left: auto"
            @click="handleDownload"
          >
            <el-icon><Download /></el-icon>
            下载全部
          </el-button>
        </div>
      </template>

      <div class="image-grid">
        <div
          v-for="(img, index) in images"
          :key="index"
          class="image-item"
        >
          <div class="image-wrapper">
            <img :src="img" :alt="`第 ${index + 1} 页`" @click="previewImage(img, index)" />
            <div class="image-overlay">
              <el-button
                circle
                type="primary"
                :icon="ZoomIn"
                @click="previewImage(img, index)"
              />
              <el-button
                circle
                type="success"
                :icon="Download"
                @click="downloadSingleImage(img, index)"
              />
            </div>
          </div>
          <p class="image-page">第 {{ index + 1 }} 页</p>
        </div>
      </div>
    </el-card>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="images"
      :initial-index="previewIndex"
      @close="previewVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ZoomIn, Download } from '@element-plus/icons-vue'
import { pdfToImages, pdfToImagesDownload } from '../api/pdf'

const uploadRef = ref(null)
const loading = ref(false)
const downloading = ref(false)
const selectedFile = ref(null)
const fileList = ref([])
const images = ref([])
const previewVisible = ref(false)
const previewIndex = ref(0)

const form = reactive({
  format: 'png',
  dpi: 150
})

const dpiMarks = {
  72: '72',
  150: '150',
  200: '200',
  300: '300'
}

function handleFileChange(file) {
  selectedFile.value = file.raw
  images.value = []
}

function handleExceed() {
  ElMessage.warning('每次只能上传一个 PDF 文件，请先删除已有文件')
}

async function handleConvert() {
  if (!selectedFile.value) return
  loading.value = true
  images.value = []
  try {
    const res = await pdfToImages(selectedFile.value, form.dpi, form.format)
    const data = res.data
    if (data.code === 200) {
      images.value = data.data.images
      ElMessage.success(`转换成功，共 ${data.data.totalPages} 页`)
    } else {
      ElMessage.error(data.message || '转换失败')
    }
  } catch (e) {
    ElMessage.error('转换失败，请检查网络或后端服务')
  } finally {
    loading.value = false
  }
}

async function handleDownload() {
  if (!selectedFile.value) return
  downloading.value = true
  try {
    const res = await pdfToImagesDownload(selectedFile.value, form.dpi, form.format)
    var match = res.headers['content-disposition'].match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    var filename = match[1].replace(/['"]/g, '');
    triggerDownload(res.data, filename)
    ElMessage.success('下载成功')
  } catch (e) {
    ElMessage.error('下载失败，请检查网络或后端服务')
  } finally {
    downloading.value = false
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

function downloadSingleImage(imgSrc, index) {
  const a = document.createElement('a')
  a.href = imgSrc
  a.download = `page_${String(index + 1).padStart(3, '0')}.${form.format}`
  a.click()
}

function previewImage(img, index) {
  previewIndex.value = index
  previewVisible.value = true
}

function handleReset() {
  uploadRef.value?.clearFiles()
  selectedFile.value = null
  fileList.value = []
  images.value = []
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

.config-form {
  padding: 8px 0;
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

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.image-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-wrapper {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.image-wrapper img {
  width: 100%;
  height: auto;
  display: block;
  transition: transform 0.3s;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-wrapper:hover img {
  transform: scale(1.03);
}

.image-wrapper:hover .image-overlay {
  opacity: 1;
}

.image-page {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
</style>
