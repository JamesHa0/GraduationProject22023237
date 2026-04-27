<template>
  <div class="app-container">
    <el-card class="mb20" shadow="never">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="学期">
          <el-select v-model="queryParams.semester" placeholder="选择学期" style="width:200px" clearable>
            <el-option v-for="item in semesterOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师">
          <el-select v-model="queryParams.teacherId" placeholder="选择教师" style="width:150px" clearable filterable>
            <el-option v-for="t in teacherList" :key="t.id" :label="t.teacherName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="queryParams.classId" placeholder="选择班级" style="width:150px" clearable filterable>
            <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float:right">
          <el-button type="warning" plain icon="Upload" @click="handleImport">批量导入</el-button>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增排课</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-size:16px;font-weight:bold">排课管理</span>
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="table">课表视图</el-radio-button>
            <el-radio-button value="list">列表视图</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 课表视图 -->
      <div v-if="viewMode==='table'" class="schedule-grid-container" v-loading="loading">
        <el-empty v-if="timeSlots.length === 0" description="暂无节次配置，请先在字典管理中添加节次（dict_type=sys_time_slot）" />
        <el-empty v-else-if="!loading && scheduleList.length === 0" description="暂无排课数据" />
        <table v-else class="schedule-grid">
          <thead><tr><th class="time-hd">时间</th><th v-for="d in 7" :key="d" class="day-hd">{{dayNames[d]}}</th></tr></thead>
          <tbody>
            <tr v-for="slot in timeSlots" :key="slot.dictCode">
              <td class="time-cell"><div class="slot-name">{{slot.dictLabel}}</div><div class="slot-time">{{slot.dictValue}}</div></td>
              <td v-for="d in 7" :key="d" class="sched-cell"
                :class="{'conflict-cell':hasConflict(d,slot.dictCode), 'empty-cell':getItems(d,slot.dictCode).length === 0}"
                @click.self="handleCellClick(d, slot)">
                <template v-if="getItems(d,slot.dictCode).length > 0">
                  <el-tooltip v-for="item in getItems(d,slot.dictCode)" :key="item.id" placement="top" :show-after="300">
                    <template #content>
                      <div style="line-height:1.8">
                        <div><b>{{item.courseName}}</b></div>
                        <div>教师：{{item.teacherName}}</div>
                        <div>班级：{{item.className}}</div>
                        <div v-if="item.classroom">教室：{{item.classroom}}</div>
                        <div>时间：{{item.startSectionValue}} ~ {{item.endSectionValue}}</div>
                      </div>
                    </template>
                    <div class="course-card"
                      :style="{background:cardColor(item.id), borderLeftColor: getConflictColor(d,slot.dictCode)}"
                      @click="handleEdit(item)">
                      <div class="card-c">{{item.courseName}}</div>
                      <div class="card-i">{{item.teacherName}}</div>
                      <div class="card-i">{{item.className}}</div>
                      <div class="card-i" v-if="item.classroom">{{item.classroom}}</div>
                    </div>
                  </el-tooltip>
                </template>
                <div v-else class="empty-hint" @click="handleCellClick(d, slot)">
                  <el-icon><Plus /></el-icon>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 列表视图 -->
      <div v-else>
        <el-table v-loading="loading" :data="scheduleList">
          <el-table-column label="课程" prop="courseName" min-width="120" />
          <el-table-column label="教师" prop="teacherName" width="100" />
          <el-table-column label="班级" prop="className" width="100" />
          <el-table-column label="星期" width="70"><template #default="s">{{dayNames[s.row.dayOfWeek]}}</template></el-table-column>
          <el-table-column label="节次" prop="sectionDisplay" width="110" />
          <el-table-column label="时间" width="160">
            <template #default="s">{{s.row.startSectionValue}} ~ {{s.row.endSectionValue}}</template>
          </el-table-column>
          <el-table-column label="教室" prop="classroom" width="90" />
          <el-table-column label="操作" width="200">
            <template #default="s">
              <el-button link type="primary" icon="Edit" @click="handleEdit(s.row)" />
              <el-button link type="primary" icon="DocumentCopy" @click="handleCopy(s.row)" />
              <el-button link type="primary" icon="Delete" @click="handleDelete(s.row)" />
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogOpen" width="680px" append-to-body>
      <el-form :model="form" :rules="formRules" ref="scheduleRef" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="授课教师" prop="teacherId">
              <el-select v-model="form.teacherId" placeholder="选择教师" style="width:100%" filterable @change="onTeacherChange">
                <el-option v-for="t in teacherList" :key="t.id" :label="t.teacherName" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程" prop="courseId">
              <el-select v-model="form.courseId" placeholder="选择课程" style="width:100%" filterable>
                <el-option-group v-if="teacherCourseList.length > 0" label="推荐课程（该教师已排课）">
                  <el-option v-for="c in teacherCourseList" :key="'t-'+c.id" :label="c.name" :value="c.id" />
                </el-option-group>
                <el-option-group label="全部课程">
                  <el-option v-for="c in courseList" :key="c.id" :label="c.name" :value="c.id" />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班级" prop="classIds">
              <el-select v-model="form.classIds" placeholder="选择班级（可多选）" style="width:100%" multiple filterable>
                <el-option v-for="cl in classList" :key="cl.id" :label="cl.className" :value="cl.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教室">
              <el-input v-model="form.classroom" placeholder="可选，如A101" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="星期" prop="dayOfWeek">
              <el-select v-model="form.dayOfWeek" placeholder="选择" style="width:100%">
                <el-option v-for="d in 7" :key="d" :label="dayNames[d]" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开始节次" prop="startSection">
              <el-select v-model="form.startSection" placeholder="开始" style="width:100%">
                <el-option v-for="s in timeSlots" :key="s.dictCode" :label="s.dictLabel" :value="s.dictCode" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束节次" prop="endSection">
              <el-select v-model="form.endSection" placeholder="结束" style="width:100%">
                <el-option v-for="s in timeSlots" :key="s.dictCode" :label="s.dictLabel" :value="s.dictCode" :disabled="form.startSection != null && s.dictCode < form.startSection" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="学期" prop="semester"><el-input v-model="form.semester" placeholder="如2024-2025-2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学年" prop="year"><el-date-picker v-model="form.year" type="year" placeholder="选择" value-format="YYYY" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <!-- 实时冲突检测提示 -->
        <el-alert v-if="conflictChecking" title="检测中..." type="info" show-icon :closable="false" style="margin-bottom:10px" />
        <el-alert v-else-if="conflictDetails.length > 0" type="error" show-icon :closable="false" style="margin-bottom:10px">
          <template #title>
            <span>检测到时间冲突：</span>
          </template>
          <template #default>
            <ul style="margin:4px 0 0 0;padding-left:18px">
              <li v-for="(d,i) in conflictDetails" :key="i">{{d.message}}</li>
            </ul>
          </template>
        </el-alert>
        <el-alert v-else-if="conflictPassed" title="未检测到时间冲突" type="success" show-icon :closable="false" style="margin-bottom:10px" />
      </el-form>
      <template #footer>
        <el-button @click="dialogOpen=false">取 消</el-button>
        <el-button v-if="!form.id" type="success" plain @click="submitForm(true)" :loading="submitLoading" :disabled="conflictDetails.length > 0">保存并继续</el-button>
        <el-button type="primary" @click="submitForm(false)" :loading="submitLoading" :disabled="conflictDetails.length > 0">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 复制弹窗 -->
    <el-dialog title="复制排课" v-model="copyOpen" width="500px" append-to-body>
      <el-form :model="copyForm" label-width="80px">
        <el-form-item label="复制到班级">
          <el-select v-model="copyForm.newClassId" placeholder="选择班级" style="width:100%" filterable>
            <el-option v-for="cl in classList" :key="cl.id" :label="cl.className" :value="cl.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="copyForm.newDayOfWeek" placeholder="选择" style="width:100%">
            <el-option v-for="d in 7" :key="d" :label="dayNames[d]" :value="d" />
          </el-select>
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="开始节次">
              <el-select v-model="copyForm.newStartSection" placeholder="开始" style="width:100%">
                <el-option v-for="s in timeSlots" :key="s.dictCode" :label="s.dictLabel" :value="s.dictCode" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束节次">
              <el-select v-model="copyForm.newEndSection" placeholder="结束" style="width:100%">
                <el-option v-for="s in timeSlots" :key="s.dictCode" :label="s.dictLabel" :value="s.dictCode" :disabled="copyForm.newStartSection != null && s.dictCode < copyForm.newStartSection" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="copyOpen=false">取 消</el-button>
        <el-button type="primary" @click="submitCopy">确认复制</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog title="批量导入排课" v-model="importOpen" width="680px" append-to-body>
      <el-alert title="导入提示" type="info" :closable="false" show-icon style="margin-bottom: 12px;">
        <template #default>
          <div>请先下载模板（当前版本：{{ templateVersion }}），按"排课模板"Sheet填写，参考"填写说明"Sheet中的字段规则与错误码。</div>
        </template>
      </el-alert>
      <!-- 课程选择器 -->
      <el-card shadow="never" class="mb12">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span style="font-size:14px;font-weight:600">智能模板（可选）</span>
            <el-tag v-if="importCourseIds.length > 0" type="success" size="small">已选 {{importCourseIds.length}} 门课程</el-tag>
          </div>
        </template>
        <div style="margin-bottom:8px;color:#909399;font-size:12px">选择课程后下载模板，将自动预填教师工号、课程编号等信息，减少手动编辑量</div>
        <el-select v-model="importCourseIds" placeholder="选择课程（可多选）" style="width:100%" multiple filterable>
          <el-option v-for="c in courseList" :key="c.id" :label="`${c.name}（${c.courseNo || '-'}）`" :value="c.id" />
        </el-select>
        <div style="margin-top:8px">
          <el-button type="primary" size="small" @click="downloadTemplate">
            {{ importCourseIds.length > 0 ? `下载含${importCourseIds.length}门课程数据的模板` : '下载空模板' }}
          </el-button>
        </div>
      </el-card>
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 20px;">
        <template #default>
          <div>关键规则：教师工号、课程编号、班级名称必须在系统中已存在；星期填1-7或中文；开始节次和结束节次须与系统节次名称一致且结束>=开始；同一教师+班级+时间段不可重复。</div>
        </template>
      </el-alert>
      <el-upload ref="uploadRef" :auto-upload="false" :limit="1" :on-change="handleFileChange" :on-exceed="handleExceed" accept=".xlsx,.xls" drag>
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xls/.xlsx，单文件不超过10MB</div>
        </template>
      </el-upload>
      <div v-if="importResult" style="margin-top: 20px;">
        <el-divider content-position="left">导入结果</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总记录数">{{ importResult.total }}</el-descriptions-item>
          <el-descriptions-item label="成功数量"><span style="color: #67C23A;">{{ importResult.successCount }}</span></el-descriptions-item>
          <el-descriptions-item label="失败数量"><span style="color: #F56C6C;">{{ importResult.failCount }}</span></el-descriptions-item>
        </el-descriptions>
        <div v-if="importResult.failDetails && importResult.failDetails.length > 0" style="margin-top: 15px;">
          <el-alert title="失败详情（请按建议修复后重试）" type="warning" :closable="false">
            <ul style="margin: 0; padding-left: 20px; max-height: 220px; overflow-y: auto;">
              <li v-for="(item, index) in importResult.failDetails" :key="index" style="margin-bottom: 8px;">
                第{{ item.row }}行（{{ [item.teacherNo, item.courseName, item.className].filter(v => v).join(' / ') || '-' }}）
                <span> - {{ item.reason }}</span>
                <span v-if="item.errorCode">（{{ item.errorCode }}）</span>
                <div v-if="item.field || item.suggestion" style="color: #909399; margin-top: 2px;">
                  <span v-if="item.field">字段：{{ item.field }}；</span>
                  <span v-if="item.suggestion">建议：{{ item.suggestion }}</span>
                </div>
              </li>
            </ul>
          </el-alert>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitImport" :loading="importLoading" :disabled="!uploadFile">确 定</el-button>
        <el-button @click="cancelImport">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ScheduleManage">
import { listSchedule, addSchedule, updateSchedule, delSchedule, copySchedule, listTimeSlots, importSchedule, downloadImportTemplate, checkConflict } from '@/api/course/schedule'
import { listAllClass } from '@/api/course/class'
import { listCourse, listTeachers } from '@/api/course/course'
import { UploadFilled, Plus } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance()

const viewMode = ref('table')
watch(viewMode, () => { getList() })
const loading = ref(false)
const scheduleList = ref([])
const total = ref(0)
const teacherList = ref([])
const classList = ref([])
const courseList = ref([])
const timeSlots = ref([])
const submitLoading = ref(false)
const copyOpen = ref(false)
const copyForm = ref({ id: null, newClassId: null, newDayOfWeek: null, newStartSection: null, newEndSection: null })
const templateVersion = ref('v2.0')
const importOpen = ref(false)
const importLoading = ref(false)
const uploadFile = ref(null)
const uploadRef = ref()
const importResult = ref(null)
const importCourseIds = ref([])
const dayNames = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }
const colors = ['#E6F7FF','#F6FFED','#FFF7E6','#FFF1F0','#F9F0FF','#E0F7FA','#E8F5E9','#FFF3E0','#FCE4EC','#F3E5F5','#E8EAF6','#E0F2F1','#FBE9E7','#E1F5FE','#F1F8E9']

const semesterOptions = ref([])
const queryParams = ref({ pageNum: 1, pageSize: 100, semester: '', teacherId: null, classId: null, year: null })
const dialogOpen = ref(false)
const dialogTitle = ref('')
const form = ref({ id: null, teacherId: null, courseId: null, classIds: [], dayOfWeek: null, startSection: null, endSection: null, classroom: '', semester: '', year: null })
const formRules = {
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  classIds: [{ required: true, message: '请选择班级', trigger: 'change' }],
  dayOfWeek: [{ required: true, message: '请选择星期', trigger: 'change' }],
  startSection: [{ required: true, message: '请选择开始节次', trigger: 'change' }],
  endSection: [{ required: true, message: '请选择结束节次', trigger: 'change' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
  year: [{ required: true, message: '请选择学年', trigger: 'change' }]
}

// ========== 实时冲突检测 ==========
const conflictDetails = ref([])
const conflictChecking = ref(false)
const conflictPassed = ref(false)
let conflictTimer = null

function checkFormConflict() {
  const f = form.value
  if (!f.teacherId && !f.classIds?.length && !f.classroom) { conflictDetails.value = []; conflictPassed.value = false; return }
  if (!f.dayOfWeek || !f.startSection || !f.endSection || !f.semester || !f.year) { conflictDetails.value = []; conflictPassed.value = false; return }

  conflictChecking.value = true
  conflictPassed.value = false
  const classId = f.classIds?.length ? f.classIds[0] : null
  checkConflict({
    teacherId: f.teacherId,
    classId: classId,
    classroom: f.classroom || undefined,
    dayOfWeek: f.dayOfWeek,
    startSection: f.startSection,
    endSection: f.endSection,
    semester: f.semester,
    year: f.year,
    excludeId: f.id
  }).then(res => {
    const data = res.data || {}
    conflictDetails.value = data.conflictDetails || []
    conflictPassed.value = !data.hasConflict && data.conflicts?.length === 0
  }).catch(() => {
    conflictDetails.value = []
    conflictPassed.value = false
  }).finally(() => {
    conflictChecking.value = false
  })
}

function debouncedConflictCheck() {
  clearTimeout(conflictTimer)
  conflictTimer = setTimeout(() => { checkFormConflict() }, 400)
}

// watch表单关键字段变更
watch(() => [form.value.teacherId, form.value.classIds, form.value.classroom, form.value.dayOfWeek, form.value.startSection, form.value.endSection], () => {
  debouncedConflictCheck()
}, { deep: true })

// ========== 教师-课程联动 ==========
const teacherCourseList = ref([])

function onTeacherChange(teacherId) {
  form.value.courseId = null
  if (!teacherId) { teacherCourseList.value = []; return }
  // 从已有排课数据中找出该教师的课程
  const teacherScheduleCourses = scheduleList.value
    .filter(s => s.teacherId === teacherId)
    .map(s => s.courseId)
  const uniqueIds = [...new Set(teacherScheduleCourses)]
  teacherCourseList.value = courseList.value.filter(c => uniqueIds.includes(c.id))
}

// ========== 学期生成 ==========
function genSemesters() {
  const opts = [], now = new Date(), cy = now.getFullYear()
  for (let y = cy - 2; y <= cy + 1; y++) {
    opts.push({ label: `${y}-${y+1}学年第一学期`, value: `${y}-${y+1}-1` })
    opts.push({ label: `${y}-${y+1}学年第二学期`, value: `${y}-${y+1}-2` })
  }
  semesterOptions.value = opts
  if (!queryParams.value.semester) {
    const m = now.getMonth() + 1, suf = m >= 2 && m <= 7 ? '2' : '1', sy = suf === '2' ? cy - 1 : cy
    queryParams.value.semester = `${sy}-${sy+1}-${suf}`
  }
}

// ========== 列表获取 ==========
function getList() {
  loading.value = true
  const params = viewMode.value === 'table'
    ? { ...queryParams.value, pageNum: 1, pageSize: 9999 }
    : queryParams.value
  listSchedule(params).then(res => {
    scheduleList.value = res.data.rows || []
    total.value = res.data.total || 0
    loading.value = false
  }).catch(err => {
    loading.value = false
    console.error('获取排课列表失败:', err)
  })
}

// ========== 课表网格 ==========
function getItems(dow, sectionCode) {
  return scheduleList.value.filter(i => i.dayOfWeek == dow && i.startSection <= sectionCode && i.endSection >= sectionCode)
}
function hasConflict(dow, sectionCode) { return getItems(dow, sectionCode).length > 1 }
function cardColor(id) { return colors[Number(id) % colors.length] }
function getConflictColor(dow, sectionCode) {
  const items = getItems(dow, sectionCode)
  if (items.length <= 1) return ''
  return '#F56C6C'
}

// ========== 空闲时段点击新增 ==========
function handleCellClick(dayOfWeek, slot) {
  if (getItems(dayOfWeek, slot.dictCode).length > 0) return
  form.value = {
    id: null, teacherId: null, courseId: null, classIds: [],
    dayOfWeek: dayOfWeek, startSection: slot.dictCode, endSection: slot.dictCode,
    classroom: '', semester: queryParams.value.semester, year: null
  }
  teacherCourseList.value = []
  conflictDetails.value = []
  conflictPassed.value = false
  dialogTitle.value = '新增排课'
  dialogOpen.value = true
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value = { pageNum:1, pageSize:100, semester:queryParams.value.semester, teacherId:null, classId:null, year:null }; getList() }

function handleAdd() {
  form.value = { id:null, teacherId:null, courseId:null, classIds:[], dayOfWeek:null, startSection:null, endSection:null, classroom:'', semester:queryParams.value.semester, year:null }
  teacherCourseList.value = []
  conflictDetails.value = []
  conflictPassed.value = false
  dialogTitle.value = '新增排课'
  dialogOpen.value = true
}

function handleEdit(row) {
  form.value = { id:row.id, teacherId:row.teacherId, courseId:row.courseId, classIds:row.classId?[row.classId]:[], dayOfWeek:row.dayOfWeek, startSection:row.startSection, endSection:row.endSection, classroom:row.classroom, semester:row.semester, year:row.year ? String(row.year) : null }
  onTeacherChange(row.teacherId)
  conflictDetails.value = []
  conflictPassed.value = false
  dialogTitle.value = '编辑排课'
  dialogOpen.value = true
}

function submitForm(continueAdd) {
  proxy.$refs['scheduleRef'].validate(valid => {
    if (!valid) return
    if (form.value.endSection < form.value.startSection) {
      proxy.$modal.msgWarning('结束节次不能早于开始节次'); return
    }
    if (conflictDetails.value.length > 0) {
      proxy.$modal.msgWarning('存在时间冲突，请调整后再提交'); return
    }
    submitLoading.value = true
    const submitData = {...form.value, year: form.value.year ? parseInt(form.value.year) : null}
    const apiCall = form.value.id ? updateSchedule({...submitData, classId: form.value.classIds[0]}) : addSchedule(submitData)
    apiCall.then(res => {
      const msg = typeof res.data === 'string' ? res.data : (res.msg || '')
      if (msg.includes('成功')) {
        proxy.$modal.msgSuccess(msg)
        getList()
        if (continueAdd && !form.value.id) {
          // 保留教师/课程/学期/学年，清空时间和班级
          const { teacherId, courseId, semester, year } = form.value
          form.value = { id:null, teacherId, courseId, classIds:[], dayOfWeek:null, startSection:null, endSection:null, classroom:'', semester, year }
          conflictDetails.value = []
          conflictPassed.value = false
        } else {
          dialogOpen.value = false
        }
      } else { conflictDetails.value = [{ type: 'submit', message: msg || '操作失败' }] }
    }).catch(() => { conflictDetails.value = [{ type: 'submit', message: '操作失败' }] }).finally(() => { submitLoading.value = false })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除该排课记录？').then(() => delSchedule(row.id)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

function handleCopy(row) {
  copyForm.value = { id: row.id, newClassId: null, newDayOfWeek: Number(row.dayOfWeek), newStartSection: row.startSection, newEndSection: row.endSection }
  copyOpen.value = true
}

function submitCopy() {
  if (!copyForm.value.newClassId || !copyForm.value.newDayOfWeek || !copyForm.value.newStartSection || !copyForm.value.newEndSection) {
    proxy.$modal.msgWarning('请填写完整'); return
  }
  if (copyForm.value.newEndSection < copyForm.value.newStartSection) {
    proxy.$modal.msgWarning('结束节次不能早于开始节次'); return
  }
  copySchedule(copyForm.value).then(res => {
    const msg = typeof res.data === 'string' ? res.data : ''
    if (msg.includes('成功')) { proxy.$modal.msgSuccess(msg); copyOpen.value = false; getList() }
    else { proxy.$modal.msgError(msg || '复制失败') }
  })
}

// ========== 批量导入 ==========
function handleImport() {
  resetImport()
  importCourseIds.value = []
  importOpen.value = true
}

function resetImport() {
  uploadFile.value = null
  importResult.value = null
  if (uploadRef.value) uploadRef.value.clearFiles()
}

function cancelImport() {
  importOpen.value = false
  resetImport()
}

function handleFileChange(file) {
  uploadFile.value = file.raw
}

function handleExceed() {
  proxy.$modal.msgWarning('只能上传一个文件，请先移除已选文件')
  uploadRef.value?.clearFiles()
  uploadFile.value = null
}

function downloadTemplate() {
  downloadImportTemplate(importCourseIds.value).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '排课导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    proxy.$modal.msgSuccess('模板下载成功')
  }).catch(() => {
    proxy.$modal.msgError('模板下载失败')
  })
}

function submitImport() {
  if (!uploadFile.value) {
    proxy.$modal.msgWarning('请选择要上传的文件'); return
  }
  importLoading.value = true
  const formData = new FormData()
  formData.append('file', uploadFile.value)
  importSchedule(formData).then(res => {
    importResult.value = res.data
    if (importResult.value.failCount > 0) {
      proxy.$modal.msgWarning(`导入完成：成功 ${importResult.value.successCount} 条，失败 ${importResult.value.failCount} 条`)
    }
    if (importResult.value.successCount > 0) {
      getList()
    }
  }).catch(() => {
    proxy.$modal.msgError('导入失败，请检查模板和数据后重试')
  }).finally(() => {
    importLoading.value = false
  })
}

// ========== 初始化 ==========
genSemesters()
listTeachers().then(res => { teacherList.value = res.data || [] }).catch(err => { console.error('获取教师列表失败:', err) })
listAllClass().then(res => { classList.value = res.data || [] }).catch(err => { console.error('获取班级列表失败:', err) })
listCourse({ pageSize: 9999 }).then(res => { courseList.value = res.data.rows || res.data || [] }).catch(err => { console.error('获取课程列表失败:', err) })
listTimeSlots().then(res => {
  timeSlots.value = res.data || []
}).catch(err => { console.error('获取节次列表失败:', err) })
getList()
</script>

<style scoped>
.mb20{margin-bottom:20px}
.mb12{margin-bottom:12px}
.schedule-grid-container{overflow-x:auto}
.schedule-grid{width:100%;border-collapse:collapse;table-layout:fixed}
.schedule-grid th,.schedule-grid td{border:1px solid #e4e7ed;text-align:center;vertical-align:top;padding:4px}
.time-hd{width:100px;background:#f5f7fa;font-weight:bold}
.day-hd{background:#f5f7fa;font-weight:bold;font-size:14px}
.time-cell{background:#fafafa;padding:8px 4px}
.slot-name{font-weight:bold;font-size:13px;color:#303133}
.slot-time{font-size:11px;color:#909399;margin-top:2px}
.sched-cell{min-height:80px;height:80px;position:relative;cursor:pointer}
.sched-cell.empty-cell:hover{background:#f0f7ff}
.conflict-cell{background:rgba(245,108,108,0.08)!important;border:2px solid #f56c6c!important}
.course-card{padding:4px 6px;border-radius:4px;margin:2px 0;cursor:pointer;transition:box-shadow .2s;border-left:3px solid transparent}
.course-card:hover{box-shadow:0 2px 8px rgba(0,0,0,0.15)}
.card-c{font-weight:bold;font-size:12px;color:#303133;margin-bottom:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.card-i{font-size:11px;color:#606266;line-height:1.4}
.empty-hint{display:flex;align-items:center;justify-content:center;height:100%;min-height:70px;color:#c0c4cc;font-size:16px;opacity:0;transition:opacity .2s}
.sched-cell:hover .empty-hint{opacity:1}
</style>
