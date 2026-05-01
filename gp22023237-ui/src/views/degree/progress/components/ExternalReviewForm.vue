<template>
  <el-row :gutter="10">
    <el-col :span="12">
      <el-form-item label="外审专家" prop="reviewerName">
        <el-input v-model="form.reviewerName" placeholder="请输入外审专家姓名" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="专家单位" prop="reviewerInstitution">
        <el-input v-model="form.reviewerInstitution" placeholder="请输入专家所在单位" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="评审领域" prop="reviewField">
        <el-input v-model="form.reviewField" placeholder="请输入评审领域/方向" />
      </el-form-item>
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true }
})
</script>
