<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="28"><Document /></el-icon>
        <span>PDF 工具箱</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1e2a3a"
        text-color="#c0cfe0"
        active-text-color="#409EFF"
        class="sidebar-menu"
      >
        <el-menu-item index="/pdf-to-image">
          <el-icon><Picture /></el-icon>
          <span>PDF 转图片</span>
        </el-menu-item>
        <el-menu-item index="/pdf-to-word">
          <el-icon><Files /></el-icon>
          <span>PDF 转 Word</span>
        </el-menu-item>
        <el-menu-item index="/word-to-pdf">
          <el-icon><Document /></el-icon>
          <span>Word 转 PDF</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="header-title">{{ pageTitle }}</span>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || 'PDF 工具箱')
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f0f2f5;
}

.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #1e2a3a;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px;
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid #2d3e52;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  margin-top: 8px;
}

.sidebar-menu .el-menu-item {
  height: 52px;
  margin: 4px 8px;
  border-radius: 8px;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #1a4a7a !important;
}

.header {
  background-color: #ffffff;
  border-bottom: 1px solid #e8edf2;
  display: flex;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a2332;
}

.main-content {
  background-color: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}
</style>
