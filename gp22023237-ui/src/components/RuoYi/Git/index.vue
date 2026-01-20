<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script setup>
const url = ref('https://github.com/JamesHa0/GraduationProject22023237');

function goto() {
  window.open(url.value)
}
</script>
