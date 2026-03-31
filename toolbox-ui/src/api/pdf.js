import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 120000
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

/**
 * PDF 转图片（返回 Base64）
 * @param {File} file PDF 文件
 * @param {number} dpi 图片 DPI
 * @param {string} format 图片格式 png/jpeg
 */
export function pdfToImages(file, dpi = 150, format = 'png') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dpi', dpi)
  formData.append('format', format)
  return request.post('/pdf/to-images', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * PDF 转图片（下载 ZIP 压缩包）
 * @param {File} file PDF 文件
 * @param {number} dpi 图片 DPI
 * @param {string} format 图片格式 png/jpeg
 */
export function pdfToImagesDownload(file, dpi = 150, format = 'png') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dpi', dpi)
  formData.append('format', format)
  return request.post('/pdf/to-images/download', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    responseType: 'blob'
  })
}

/**
 * PDF 转 Word（下载 docx 文件）
 * @param {File} file PDF 文件
 */
export function pdfToWord(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/pdf/to-word', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    responseType: 'blob'
  })
}

/**
 * Word 转 PDF（下载 pdf 文件）
 * @param {File} file Word 文件
 */
export function wordToPdf(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/pdf/word-to-pdf', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    responseType: 'blob'
  })
}
