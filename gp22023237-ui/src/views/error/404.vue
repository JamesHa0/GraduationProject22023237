<template>
    <div class="wscn-http404-container">
        <div class="wscn-http404">
            <div class="pic-404">
                <svg class="face" viewBox="0 0 320 380" width="320px" height="380px"
                    aria-label="A 404 becomes a face, looks to the sides, and blinks. The 4s slide up, the 0 slides down, and then a mouth appears.">
                    <g fill="none" stroke="currentcolor" stroke-linecap="round" stroke-linejoin="round"
                        stroke-width="25">
                        <g class="face__eyes" transform="translate(0, 112.5)">
                            <g transform="translate(15, 0)">
                                <polyline class="face__eye-lid" points="37,0 0,120 75,120" />
                                <polyline class="face__pupil" points="55,120 55,155" stroke-dasharray="35 35" />
                            </g>
                            <g transform="translate(230, 0)">
                                <polyline class="face__eye-lid" points="37,0 0,120 75,120" />
                                <polyline class="face__pupil" points="55,120 55,155" stroke-dasharray="35 35" />
                            </g>
                        </g>
                        <rect class="face__nose" rx="4" ry="4" x="132.5" y="112.5" width="55" height="155" />
                        <g stroke-dasharray="102 102" transform="translate(65, 334)">
                            <path class="face__mouth-left" d="M 0 30 C 0 30 40 0 95 0" stroke-dashoffset="-102" />
                            <path class="face__mouth-right" d="M 95 0 C 150 0 190 30 190 30" stroke-dashoffset="102" />
                        </g>
                    </g>
                </svg>
            </div>
            <div class="bullshit">
                <div class="bullshit__oops">
                    404错误!
                </div>
                <div class="bullshit__headline">
                    {{ message }}
                </div>
                <div class="bullshit__info">
                    对不起，您正在寻找的页面不存在。尝试检查URL的错误，然后按浏览器上的刷新按钮或尝试在我们的应用程序中找到其他内容。
                </div>
                <router-link to="/index" class="bullshit__return-home">
                    返回首页
                </router-link>
            </div>
        </div>
    </div>
</template>

<script setup>
let message = computed(() => {
    return '找不到网页！'
})
</script>

<style lang="scss" scoped>
.wscn-http404-container {
    transform: translate(-50%, -50%);
    position: absolute;
    top: 40%;
    left: 50%;
}

.wscn-http404 {
    position: relative;
    width: 1200px;
    padding: 0 50px;
    overflow: hidden;

    .pic-404 {
        position: relative;
        float: left;
        width: 600px;
        overflow: hidden;
    }
}

.bullshit {
    position: relative;
    float: left;
    width: 300px;
    padding: 30px 0;
    overflow: hidden;

    &__oops {
        font-size: 32px;
        font-weight: bold;
        line-height: 40px;
        color: #1482f0;
        opacity: 0;
        margin-bottom: 20px;
        animation-name: slideUp;
        animation-duration: 0.5s;
        animation-fill-mode: forwards;
    }

    &__headline {
        font-size: 20px;
        line-height: 24px;
        color: #222;
        font-weight: bold;
        opacity: 0;
        margin-bottom: 10px;
        animation-name: slideUp;
        animation-duration: 0.5s;
        animation-delay: 0.1s;
        animation-fill-mode: forwards;
    }

    &__info {
        font-size: 13px;
        line-height: 21px;
        color: grey;
        opacity: 0;
        margin-bottom: 30px;
        animation-name: slideUp;
        animation-duration: 0.5s;
        animation-delay: 0.2s;
        animation-fill-mode: forwards;
    }

    &__return-home {
        display: block;
        float: left;
        width: 110px;
        height: 36px;
        background: #1482f0;
        border-radius: 100px;
        text-align: center;
        color: #ffffff;
        opacity: 0;
        font-size: 14px;
        line-height: 36px;
        cursor: pointer;
        animation-name: slideUp;
        animation-duration: 0.5s;
        animation-delay: 0.3s;
        animation-fill-mode: forwards;
    }

    @keyframes slideUp {
        0% {
            transform: translateY(60px);
            opacity: 0;
        }

        100% {
            transform: translateY(0);
            opacity: 1;
        }
    }
}


* {
    border: 0;
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

:root {
    --hue: 223;
    --sat: 10%;
    --light: hsl(var(--hue), var(--sat), 95%);
    --dark: hsl(var(--hue), var(--sat), 5%);
    --trans-dur: 0.3s;
    color-scheme: light dark;
    font-size: clamp(1rem, 0.95rem + 0.25vw, 1.25rem);
}

body {
    background-color: light-dark(var(--light), var(--dark));
    color: light-dark(var(--dark), var(--light));
    font: 1em/1.5 sans-serif;
    display: grid;
    place-items: center;
    height: 100vh;
    transition: background-color var(--trans-dur), color var(--trans-dur);
}

main {
    padding: 1.5em 0;
}

.face {
    display: block;
    width: 12em;
    height: auto;
}

.face__eyes,
.face__eye-lid,
.face__mouth-left,
.face__mouth-right,
.face__nose,
.face__pupil {
    animation: eyes 1s 0.3s cubic-bezier(0.65, 0, 0.35, 1) forwards;
}

.face__eye-lid,
.face__pupil {
    animation-duration: 4s;
    animation-delay: 1.3s;
    animation-iteration-count: infinite;
}

.face__eye-lid {
    animation-name: eye-lid;
}

.face__mouth-left,
.face__mouth-right {
    animation-timing-function: cubic-bezier(0.33, 1, 0.68, 1);
}

.face__mouth-left {
    animation-name: mouth-left;
}

.face__mouth-right {
    animation-name: mouth-right;
}

.face__nose {
    animation-name: nose;
}

.face__pupil {
    animation-name: pupil;
}

/* Animations */
@keyframes eye-lid {

    from,
    40%,
    45%,
    to {
        transform: translateY(0);
    }

    42.5% {
        transform: translateY(17.5px);
    }
}

@keyframes eyes {
    from {
        transform: translateY(112.5px);
    }

    to {
        transform: translateY(15px);
    }
}

@keyframes pupil {

    from,
    37.5%,
    40%,
    45%,
    87.5%,
    to {
        stroke-dashoffset: 0;
        transform: translate(0, 0);
    }

    12.5%,
    25%,
    62.5%,
    75% {
        stroke-dashoffset: 0;
        transform: translate(-35px, 0);
    }

    42.5% {
        stroke-dashoffset: 35;
        transform: translate(0, 17.5px);
    }
}

@keyframes mouth-left {

    from,
    50% {
        stroke-dashoffset: -102;
    }

    to {
        stroke-dashoffset: 0;
    }
}

@keyframes mouth-right {

    from,
    50% {
        stroke-dashoffset: 102;
    }

    to {
        stroke-dashoffset: 0;
    }
}

@keyframes nose {
    from {
        transform: translate(0, 0);
    }

    to {
        transform: translate(0, 22.5px);
    }
}
</style>
