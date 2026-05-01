<template>
  <el-row :gutter="10">
    <el-col :span="12">
      <el-form-item label="研究背景" prop="background">
        <el-input v-model="form.background" type="textarea" :rows="3" placeholder="请输入研究背景" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="研究现状" prop="researchStatus">
        <el-input v-model="form.researchStatus" type="textarea" :rows="3" placeholder="请输入研究现状" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row :gutter="10">
    <el-col :span="12">
      <el-form-item label="研究内容" prop="researchContent">
        <el-input v-model="form.researchContent" type="textarea" :rows="3" placeholder="请输入研究内容" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="研究方法" prop="researchMethod">
        <el-input v-model="form.researchMethod" type="textarea" :rows="3" placeholder="请输入研究方法" />
      </el-form-item>
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true }
})
</script>
