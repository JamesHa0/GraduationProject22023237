<template>
    <div class="not-found-container">
        <div class="error-card">
            <div class="error-icon">
                <font-awesome-icon icon="exclamation-triangle" size="5x" />
            </div>
            <h1 class="error-code">404</h1>
            <p class="error-message">页面未找到</p>
            <p class="error-description">
                抱歉，您访问的页面不存在或已被移除
            </p>
            <button class="btn back-btn" @click="$router.push('/login')">
                <font-awesome-icon icon="arrow-left" /> 返回登录页
            </button>
        </div>
    </div>
</template>

<script setup>
// 404页面组件，当访问不存在的路由时显示
</script>

<style scoped>
.not-found-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    padding: 20px;
}

.error-card {
    text-align: center;
    padding: 40px;
    max-width: 500px;
    width: 100%;
}

.error-icon {
    color: #faad14;
    margin-bottom: 20px;
}

.error-code {
    font-size: 64px;
    font-weight: 700;
    margin: 0 0 10px 0;
    color: #1f2329;
}

.dark-mode .error-code {
    color: #e0e0e0;
}

.error-message {
    font-size: 24px;
    margin: 0 0 16px 0;
    color: #4e5969;
}

.dark-mode .error-message {
    color: #bbb;
}

.error-description {
    color: #86909c;
    margin-bottom: 30px;
    line-height: 1.6;
}

.back-btn {
    background-color: #409eff;
    color: white;
    padding: 10px 24px;
    font-size: 16px;
}
</style>