<template>
  <el-row>
    <el-col :span="24">
      <el-form-item label="论文摘要" prop="abstractContent">
        <el-input v-model="form.abstractContent" type="textarea" :rows="4" placeholder="请输入论文摘要" />
      </el-form-item>
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true }
})
</script>
