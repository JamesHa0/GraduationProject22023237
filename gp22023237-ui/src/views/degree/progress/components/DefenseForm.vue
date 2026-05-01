<template>
  <el-row :gutter="10">
    <el-col :span="12">
      <el-form-item label="答辩委员会主席" prop="reviewCommitteeChair">
        <el-input v-model="form.reviewCommitteeChair" placeholder="请输入答辩委员会主席" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="答辩委员" prop="reviewCommitteeMembers">
        <el-input v-model="form.reviewCommitteeMembers" placeholder="请输入答辩委员（多人用逗号分隔）" />
      </el-form-item>
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true }
})
</script>
