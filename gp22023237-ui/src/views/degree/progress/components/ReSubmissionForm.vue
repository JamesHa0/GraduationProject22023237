<template>
  <el-row>
    <el-col :span="24">
      <el-form-item label="修改说明" prop="modificationDescription">
        <el-input v-model="form.modificationDescription" type="textarea" :rows="3" placeholder="请输入修改说明" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="修改详情" prop="modificationDetails">
        <el-input v-model="form.modificationDetails" type="textarea" :rows="4" placeholder="请详细描述修改内容" />
      </el-form-item>
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true }
})
</script>
