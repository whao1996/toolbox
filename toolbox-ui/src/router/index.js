import { createRouter, createWebHistory } from 'vue-router'
import PdfToImage from '../views/PdfToImage.vue'
import PdfToWord from '../views/PdfToWord.vue'
import WordToPdf from '../views/WordToPdf.vue'

const routes = [
  {
    path: '/',
    redirect: '/pdf-to-image'
  },
  {
    path: '/pdf-to-image',
    name: 'PdfToImage',
    component: PdfToImage,
    meta: { title: 'PDF 转图片' }
  },
  {
    path: '/pdf-to-word',
    name: 'PdfToWord',
    component: PdfToWord,
    meta: { title: 'PDF 转 Word' }
  },
  {
    path: '/word-to-pdf',
    name: 'WordToPdf',
    component: WordToPdf,
    meta: { title: 'Word 转 PDF' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
