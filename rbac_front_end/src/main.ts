// src/main.ts
import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue'; // 引入所有图标
import { createPinia } from 'pinia'; // 引入Pinia
import router from './router'; // 引入路由
import App from './App.vue';
import request from './utils/request';

const app = createApp(App);
const pinia = createPinia();

// 全局注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

// 注册插件
app.use(pinia);
app.use(router);
app.use(ElementPlus);

// 全局挂载 request 工具（可选，也可在组件内 import 使用）
app.config.globalProperties.$request = request;

app.mount('#app');