import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import pinia from './stores';

// 导入Font Awesome图标
import { library } from '@fortawesome/fontawesome-svg-core';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

// 导入全局样式
import './assets/styles/index.css';

// 添加所有Font Awesome图标
library.add(fas);

// 创建应用
const app = createApp(App);

// 注册插件
app.use(router);
app.use(pinia);

// 注册全局组件
app.component('font-awesome-icon', FontAwesomeIcon);

// 挂载应用
app.mount('#app');
