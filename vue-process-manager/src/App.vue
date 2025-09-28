<template>
  <div class="page">
    <header>流程管理 Demo（Vue）</header>
    <main>
      <section class="card">
        <h3>创建/编辑流程</h3>
        <div class="row">
          <div class="col">
            <label>流程名称</label>
            <input v-model="editor.name" type="text" placeholder="例如：请假审批" />
          </div>
        </div>
        <div class="group">
          <div class="group-title">表单字段</div>
          <div>
            <div v-for="(f, i) in editor.formFields" :key="i" class="row item">
              <div class="col"><label>字段名称</label><input v-model="f.name" type="text" placeholder="如: 请假天数" /></div>
              <div class="col"><label>字段类型</label>
                <select v-model="f.type">
                  <option value="text">text</option>
                  <option value="number">number</option>
                  <option value="date">date</option>
                  <option value="textarea">textarea</option>
                  <option value="select">select</option>
                </select>
              </div>
              <div class="col"><label>是否必填</label>
                <select v-model="f.required">
                  <option :value="true">是</option>
                  <option :value="false">否</option>
                </select>
              </div>
              <button class="btn danger" @click="removeField(i)">删除</button>
            </div>
          </div>
          <button class="btn icon" @click="addField">➕ 添加字段</button>
        </div>

        <div class="group">
          <div class="group-title">审批步骤</div>
          <div>
            <div v-for="(s, i) in editor.steps" :key="i" class="row item">
              <div class="col"><label>审批名称</label><input v-model="s.taskName" type="text" placeholder="如: 直属领导审批" /></div>
              <div class="col"><label>审批人</label><input v-model="s.assignee" type="text" placeholder="如: userId" /></div>
              <button class="btn danger" @click="removeStep(i)">删除</button>
            </div>
          </div>
          <button class="btn icon" @click="addStep">➕ 添加审批步骤</button>
        </div>

        <div class="row actions">
          <button class="btn primary" @click="submit">{{ editingId ? '保存更新' : '提交创建' }}</button>
          <button class="btn" @click="resetEditor">重置</button>
        </div>
      </section>

      <section class="card">
        <h3>流程列表</h3>
        <table class="list">
          <thead>
          <tr><th>ID</th><th>名称</th><th>表单字段</th><th>步骤数</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="p in list" :key="p.id">
              <td>{{ p.id }}</td>
              <td>{{ p.name }}</td>
              <td>{{ (p.formFields||[]).map(f=>f.name).join(', ') }}</td>
              <td>{{ (p.steps||[]).length }}</td>
              <td>
                <button class="btn" @click="view(p.id)">查看</button>
                <button class="btn" @click="edit(p.id)">编辑</button>
                <button class="btn danger" @click="remove(p.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'

type FormField = { name: string; type: string; required: boolean }
type Step = { taskName: string; assignee: string }
type Proc = { id?: string; name: string; formFields: FormField[]; steps: Step[] }

const list = ref<Proc[]>([])
const editingId = ref<string | undefined>()
const editor = reactive<Proc>({ name: '', formFields: [], steps: [] })

const api = {
  list: async (): Promise<Proc[]> => (await fetch('/api/process-definitions')).json(),
  create: async (data: Proc): Promise<Proc> => (await fetch('/api/process-definitions', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(data)})).json(),
  get: async (id: string): Promise<Proc> => (await fetch(`/api/process-definitions/${id}`)).json(),
  update: async (id: string, data: Proc): Promise<Proc> => (await fetch(`/api/process-definitions/${id}`, { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify(data)})).json(),
  remove: async (id: string) => fetch(`/api/process-definitions/${id}`, { method:'DELETE' })
}

function addField() { editor.formFields.push({ name:'', type:'text', required:true }) }
function removeField(i: number) { editor.formFields.splice(i,1) }
function addStep() { editor.steps.push({ taskName:'', assignee:'' }) }
function removeStep(i: number) { editor.steps.splice(i,1) }

async function reload() { list.value = await api.list() }

async function submit() {
  const payload: Proc = { name: editor.name.trim(), formFields: editor.formFields.filter(f=>f.name), steps: editor.steps.filter(s=>s.taskName) }
  if (!editingId.value) {
    await api.create(payload)
  } else {
    await api.update(editingId.value, payload)
  }
  resetEditor(); await reload()
}

function resetEditor() {
  editor.name = ''; editor.formFields = []; editor.steps = []; editingId.value = undefined
}

async function view(id: string) {
  const d = await api.get(id)
  alert(JSON.stringify(d, null, 2))
}
async function edit(id: string) {
  const d = await api.get(id)
  editor.name = d.name || ''
  editor.formFields = (d.formFields || []).map((f:any)=>({ name: f.name||'', type: f.type||'text', required: !!f.required }))
  editor.steps = (d.steps || []).map((s:any)=>({ taskName: s.taskName||'', assignee: s.assignee||'' }))
  editingId.value = id
}
async function remove(id: string) { if (confirm('确认删除该流程？')) { await api.remove(id); await reload() } }

onMounted(reload)
</script>

<style scoped>
.page { font-family: -apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial; background: #f6f7f9; min-height: 100vh; }
header { background: #1f6feb; color: #fff; padding: 14px 20px; font-size: 18px; }
main { padding: 20px; max-width: 1100px; margin: 0 auto; }
.card { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,.08); border-radius: 8px; margin-bottom: 16px; padding: 12px 16px; }
.row { display: flex; gap: 12px; flex-wrap: wrap; }
.row.item { align-items: flex-end; margin-bottom: 6px; }
.col { flex: 1 1 240px; }
label { display: block; font-size: 12px; color: #555; margin-bottom: 6px; }
input[type="text"], select { width: 100%; padding: 8px 10px; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; }
.btn { display: inline-flex; align-items: center; gap: 6px; padding: 8px 12px; border-radius: 6px; border: 1px solid #d0d7de; background: #fff; cursor: pointer; }
.btn.primary { background: #1f6feb; color: #fff; border-color: #1f6feb; }
.btn.danger { background: #e5534b; color: #fff; border-color: #e5534b; }
.btn.icon { padding: 6px 9px; }
.group { border: 1px dashed #d0d7de; padding: 12px; border-radius: 8px; margin: 10px 0; background: #fafbfc; }
.group-title { font-weight: 600; font-size: 14px; margin-bottom: 8px; }
.list { width: 100%; border-collapse: collapse; }
.list th, .list td { border-bottom: 1px solid #eee; text-align: left; padding: 10px 8px; font-size: 14px; }
</style>


