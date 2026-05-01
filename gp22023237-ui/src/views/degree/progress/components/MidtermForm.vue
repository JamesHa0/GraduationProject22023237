<template>
  <el-row :gutter="10">
    <el-col :span="12">
      <el-form-item label="已完成工作" prop="completedWork">
        <el-input v-model="form.completedWork" type="textarea" :rows="3" placeholder="请输入已完成工作" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="未完成工作" prop="remainingWork">
        <el-input v-model="form.remainingWork" type="textarea" :rows="3" placeholder="请输入未完成工作" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row :gutter="10">
    <el-col :span="12">
      <el-form-item label="遇到的问题" prop="problems">
        <el-input v-model="form.problems" type="textarea" :rows="3" placeholder="请输入遇到的问题" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="下一步计划" prop="nextPlan">
        <el-input v-model="form.nextPlan" type="textarea" :rows="3" placeholder="请输入下一步计划" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="论文初稿完成度" prop="draftProgress">
        <el-input-number v-model="form.draftProgress" :min="0" :max="100" style="width: 100%" />
      </el-form-item>
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true }
})
</script>
