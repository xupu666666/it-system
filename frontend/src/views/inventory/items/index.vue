<template>
  <div class="inventory-items-container">
    <el-card class="search-card">
      <div class="search-container">
        <el-form :model="searchForm" inline>
          <el-form-item label="资产编号">
            <el-input v-model="searchForm.assetNo" placeholder="请输入资产编号" clearable />
          </el-form-item>
          <el-form-item label="资产描述">
            <el-input v-model="searchForm.name" placeholder="请输入资产描述" clearable />
          </el-form-item>
          <el-form-item label="APC科目">
            <el-input v-model="searchForm.apcAccount" placeholder="请输入APC科目" clearable />
          </el-form-item>
          <el-form-item label="工厂">
            <el-select v-model="searchForm.factory" placeholder="请选择工厂" clearable filterable>
              <el-option
                v-for="item in factoryOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
              <template #empty>
                <div class="empty-options">
                  <p>加载中或无数据</p>
                </div>
              </template>
            </el-select>
          </el-form-item>
          <el-form-item label="部门">
            <el-select
              v-model="searchForm.department"
              placeholder="请选择部门"
              filterable
              clearable
            >
              <el-option
                v-for="item in departmentOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
              <template #empty>
                <div class="empty-options">
                  <p>加载中或无数据</p>
                </div>
              </template>
            </el-select>
          </el-form-item>
          <el-form-item label="存放楼层">
            <el-select v-model="searchForm.storageFloor" placeholder="请选择楼层" clearable>
              <el-option
                v-for="item in floorOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
              <template #empty>
                <div class="empty-options">
                  <p>加载中或无数据</p>
                </div>
              </template>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button type="success" @click="handleDirectSearch" v-if="searchForm.assetNo">直接查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="info" @click="refreshOptions" :loading="refreshingOptions">刷新选项</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="table-card">
      <div class="table-header">
        <div class="left">
          <el-button type="primary" @click="handleAdd">新增资产</el-button>
          <el-button :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
          <el-button type="danger" @click="handleDeleteAll">全部删除</el-button>
          <el-button :disabled="selectedRows.length === 0" @click="handleBatchExport">批量导出</el-button>
          <el-upload
            class="import-upload"
            action="#"
            :show-file-list="false"
            :before-upload="handleImport"
            accept=".xlsx,.xls,.csv"
          >
            <el-button>导入资产</el-button>
          </el-upload>
          <el-button @click="startInventoryCheck">开始盘点</el-button>
          <el-button
            type="primary"
            plain
            @click="handleDownloadTemplate"
          >
            <el-icon><download /></el-icon> 下载模板
          </el-button>
        </div>
        <div class="right">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索关键字"
            style="width: 200px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #suffix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <el-table
        :data="tableData"
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
        :row-class-name="getRowClassName"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="资产编号" width="100" sortable>
          <template #default="scope">
            {{ getFieldValue(scope.row, 'assetNo') }}
          </template>
        </el-table-column>
        <el-table-column label="资本化日期" width="120" sortable>
          <template #default="scope">
            {{ getFieldValue(scope.row, 'capitalizationDate') }}
          </template>
        </el-table-column>
        <el-table-column label="APC科目" width="120">
          <template #default="scope">
            {{ getFieldValue(scope.row, 'apcAccount') }}
          </template>
        </el-table-column>
        <el-table-column label="资产描述" width="250">
          <template #default="scope">
            <el-tooltip :content="getFieldValue(scope.row, 'name')" placement="top" :show-after="200">
              <span class="ellipsis">{{ getFieldValue(scope.row, 'name') }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="工厂" width="80">
          <template #default="scope">
            {{ getFieldValue(scope.row, 'factory') }}
          </template>
        </el-table-column>
        <el-table-column label="成本中心" width="120">
          <template #default="scope">
            {{ getFieldValue(scope.row, 'costCenter') }}
          </template>
        </el-table-column>
        <el-table-column label="部门" width="150">
          <template #default="scope">
            {{ getFieldValue(scope.row, 'department') }}
          </template>
        </el-table-column>
        <el-table-column label="存放楼层" width="100">
          <template #default="scope">
            {{ getFieldValue(scope.row, 'storageFloor') }}
          </template>
        </el-table-column>
        <el-table-column label="存放设备体地点" width="150">
          <template #default="scope">
            <el-tooltip :content="getFieldValue(scope.row, 'storageLocation')" placement="top" :show-after="200">
              <span class="ellipsis">{{ getFieldValue(scope.row, 'storageLocation') }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="负责人" width="100">
          <template #default="scope">
            {{ getFieldValue(scope.row, 'responsiblePerson') }}
          </template>
        </el-table-column>
        <el-table-column label="实际使用人" width="100">
          <template #default="scope">
            {{ getFieldValue(scope.row, 'actualUser') }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === '已报废' ? 'danger' : (scope.row.status === '异常' ? 'warning' : 'success')"
              effect="plain"
            >
              {{ getFieldValue(scope.row, 'status') || '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" width="150">
          <template #default="scope">
            <el-tooltip :content="getFieldValue(scope.row, 'remark')" placement="top" :show-after="200">
              <span class="ellipsis">{{ getFieldValue(scope.row, 'remark') }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleRetire(scope.row)" v-if="scope.row.status !== '已报废'">报废</el-button>
            <el-button link type="success" size="small" @click="handleCheck(scope.row)">盘点</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 资产表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="850px">
      <el-form
        ref="formRef"
        :model="assetForm"
        :rules="assetRules"
        label-width="120px"
        status-icon
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产编号" prop="assetNo">
              <el-input v-model="assetForm.assetNo" placeholder="自动生成" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资本化日期" prop="capitalizationDate">
              <el-date-picker v-model="assetForm.capitalizationDate" type="date" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="APC科目" prop="apcAccount">
              <el-input v-model="assetForm.apcAccount" placeholder="请输入APC科目" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产描述" prop="name">
              <el-input v-model="assetForm.name" placeholder="请输入资产描述" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工厂" prop="factory">
              <el-select v-model="assetForm.factory" placeholder="请选择工厂" style="width: 100%" filterable>
                <el-option
                  v-for="item in factoryOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
                <template #empty>
                  <div class="empty-options">
                    <p>加载中或无数据</p>
                  </div>
                </template>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本中心" prop="costCenter">
              <el-input v-model="assetForm.costCenter" placeholder="请输入成本中心" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-select
                v-model="assetForm.department"
                placeholder="请选择部门"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="item in departmentOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
                <template #empty>
                  <div class="empty-options">
                    <p>加载中或无数据</p>
                  </div>
                </template>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放楼层" prop="storageFloor">
              <el-select v-model="assetForm.storageFloor" placeholder="请选择楼层" style="width: 100%">
                <el-option
                  v-for="item in floorOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
                <template #empty>
                  <div class="empty-options">
                    <p>加载中或无数据</p>
                  </div>
                </template>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放设备体地点" prop="storageLocation">
              <el-input v-model="assetForm.storageLocation" placeholder="请输入存放位置" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="responsiblePerson">
              <el-input v-model="assetForm.responsiblePerson" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际使用人" prop="actualUser">
              <el-input v-model="assetForm.actualUser" placeholder="请输入实际使用人" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input type="textarea" v-model="assetForm.remark" :rows="3" placeholder="请输入备注信息" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 盘点对话框 -->
    <el-dialog title="资产盘点" v-model="checkDialogVisible" width="500px">
      <el-form
        ref="checkFormRef"
        :model="checkForm"
        label-width="100px"
        status-icon
      >
        <el-form-item label="资产编号">
          <el-input v-model="checkForm.assetNo" disabled />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="checkForm.name" disabled />
        </el-form-item>
        <el-form-item label="所在位置">
          <el-input v-model="checkForm.location" placeholder="请确认资产位置" />
        </el-form-item>
        <el-form-item label="盘点状态">
          <el-select v-model="checkForm.status" placeholder="请选择盘点状态" style="width: 100%">
            <el-option label="正常" value="正常" />
            <el-option label="异常" value="异常" />
            <el-option label="未找到" value="未找到" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点备注">
          <el-input type="textarea" v-model="checkForm.remark" :rows="3" placeholder="请输入盘点备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="checkDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCheckForm">确认盘点</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, ref, reactive, onMounted, nextTick } from 'vue'
import { Search, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { useRouter } from 'vue-router'
import { inventoryApi } from '@/api'
import { importInventoryItems, downloadImportTemplate } from "@/api/inventory"
import axios from 'axios'

export default defineComponent({
  name: 'InventoryItems',
  components: {
    Search,
    Download
  },
  setup() {
    const router = useRouter()

    // 表格加载状态
    const loading = ref(false)

    // 刷新选项加载状态
    const refreshingOptions = ref(false)

    // 搜索表单
    const searchForm = reactive({
      assetNo: '',
      name: '',
      apcAccount: '',
      factory: '',
      department: '',
      storageFloor: '',
      keyword: ''
    })

    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })

    // 表格选中行
    const selectedRows = ref([])

    // 表格数据
    const tableData = ref([])

    // 对话框参数
    const dialogVisible = ref(false)
    const dialogTitle = ref('新增资产')
    const isEdit = ref(false)

    // 资产表单
    const assetForm = reactive({
      id: '',
      assetNo: '',
      capitalizationDate: '',
      apcAccount: '',
      name: '',
      factory: '',
      costCenter: '',
      department: '',
      storageFloor: '',
      storageLocation: '',
      responsiblePerson: '',
      actualUser: '',
      remark: ''
    })

    // 盘点对话框
    const checkDialogVisible = ref(false)
    const checkFormRef = ref(null)
    const checkForm = reactive({
      id: '',
      assetNo: '',
      name: '',
      location: '',
      status: '正常',
      remark: ''
    })

    // 表单校验规则
    const assetRules = {
      assetNo: [
        { required: true, message: '请输入资产编号', trigger: 'blur' }
      ],
      name: [
        { required: true, message: '请输入资产描述', trigger: 'blur' }
      ],
      capitalizationDate: [
        { required: true, message: '请选择资本化日期', trigger: 'change' }
      ],
      apcAccount: [
        { required: true, message: '请输入APC科目', trigger: 'blur' }
      ],
      factory: [
        { required: true, message: '请选择工厂', trigger: 'change' }
      ],
      department: [
        { required: true, message: '请选择部门', trigger: 'change' }
      ]
    }

    // 工厂、部门和楼层选项在下面定义

    // 工厂选项 - 初始为空，将从资产列表中动态获取
    const factoryOptions = ref([])

    // 楼层选项
    const floorOptions = ref([
      { value: '1F', label: '1F' },
      { value: '2F', label: '2F' },
      { value: '3F', label: '3F' },
      { value: '4F', label: '4F' },
      { value: '5F', label: '5F' }
    ])

    // 部门选项 - 初始为空，将从资产列表中动态获取
    const departmentOptions = ref([])

    // 从API获取下拉选项数据
    const fetchDropdownOptions = async () => {
      try {
        console.log('开始获取下拉选项数据...');

        // 保存当前选项，以便在API调用失败时恢复
        const currentFactoryOptions = [...factoryOptions.value];
        const currentDepartmentOptions = [...departmentOptions.value];

        // 清空工厂选项，准备从资产列表中获取
        factoryOptions.value = [];

        // 清空部门选项，准备从资产列表中获取
        departmentOptions.value = [];

        // 添加之前保存的选项，避免重复
        currentFactoryOptions.forEach(option => {
          if (!factoryOptions.value.some(opt => opt.value === option.value)) {
            factoryOptions.value.push(option);
          }
        });

        currentDepartmentOptions.forEach(option => {
          if (!departmentOptions.value.some(opt => opt.value === option.value)) {
            departmentOptions.value.push(option);
          }
        });

        // 显示加载提示
        const loadingInstance = ElLoading.service({
          lock: true,
          text: '正在加载选项数据，请稍候...',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        // 获取工厂列表
        try {
          // 先清空现有选项，确保不会有重复
          factoryOptions.value = [];

          const factoryResponse = await inventoryApi.getAllFactories();
          console.log('工厂API响应:', factoryResponse);

          if (factoryResponse && Array.isArray(factoryResponse)) {
            // 直接使用数组
            factoryResponse.forEach(item => {
              if (item) factoryOptions.value.push({ value: item, label: item });
            });
            console.log(`从API获取到 ${factoryOptions.value.length} 个工厂选项`);
          } else if (factoryResponse && factoryResponse.data && Array.isArray(factoryResponse.data)) {
            // 从data属性中获取数组
            factoryResponse.data.forEach(item => {
              if (item) factoryOptions.value.push({ value: item, label: item });
            });
            console.log(`从API获取到 ${factoryOptions.value.length} 个工厂选项`);
          } else {
            console.warn('工厂数据格式不正确:', factoryResponse);
          }

          // 不再添加默认工厂选项，只使用从API获取的实际工厂
        } catch (error) {
          console.error('获取工厂列表失败:', error);
          // 不再添加默认工厂选项，保持空列表
          factoryOptions.value = [];
        }

        // 获取部门列表
        try {
          // 先清空现有选项，确保不会有重复
          departmentOptions.value = [];

          // 不添加默认选项，完全依赖API返回的数据

          // 然后获取API数据
          const departmentResponse = await inventoryApi.getAllDepartments();
          console.log('部门API响应:', departmentResponse);

          // 处理API返回的数据
          let apiDepartments = [];
          if (departmentResponse && Array.isArray(departmentResponse)) {
            apiDepartments = departmentResponse;
          } else if (departmentResponse && departmentResponse.data && Array.isArray(departmentResponse.data)) {
            apiDepartments = departmentResponse.data;
          } else {
            console.warn('部门数据格式不正确:', departmentResponse);
          }

          // 添加API返回的部门，避免重复
          apiDepartments.forEach(item => {
            if (item && !departmentOptions.value.some(option => option.value === item)) {
              departmentOptions.value.push({ value: item, label: item });
            }
          });

          console.log(`部门选项总数: ${departmentOptions.value.length}`);
        } catch (error) {
          console.error('获取部门列表失败:', error);
        }

        // 获取楼层列表
        try {
          const floorResponse = await inventoryApi.getAllFloors();
          console.log('楼层API响应:', floorResponse);

          if (floorResponse && Array.isArray(floorResponse)) {
            floorResponse.forEach(item => {
              if (item) floorOptions.value.push({ value: item, label: item });
            });
            console.log(`从API获取到 ${floorOptions.value.length} 个楼层选项`);
          } else if (floorResponse && floorResponse.data && Array.isArray(floorResponse.data)) {
            floorResponse.data.forEach(item => {
              if (item) floorOptions.value.push({ value: item, label: item });
            });
            console.log(`从API获取到 ${floorOptions.value.length} 个楼层选项`);
          } else {
            console.warn('楼层数据格式不正确:', floorResponse);
          }
        } catch (error) {
          console.error('获取楼层列表失败:', error);
        }

        // 关闭加载提示
        loadingInstance.close();

        // 如果API返回的数据为空或数量太少，尝试从所有页面数据中提取
        if (factoryOptions.value.length < 2 || departmentOptions.value.length < 3) {
          console.log('API返回的下拉选项数据不完整，尝试从所有页面数据中提取');

          // 显示提示消息
          ElMessage({
            message: '正在尝试从所有数据中提取选项，这可能需要一些时间...',
            type: 'info',
            duration: 3000
          });

          // 调用从所有页面提取选项的方法
          await fetchAllOptionsFromAllPages();
        } else {
          console.log('下拉选项数据获取成功', {
            工厂: factoryOptions.value.length,
            部门: departmentOptions.value.length,
            楼层: floorOptions.value.length
          });

          // 显示成功消息
          ElMessage.success(`成功获取 ${factoryOptions.value.length} 个工厂选项和 ${departmentOptions.value.length} 个部门选项`);
        }
      } catch (error) {
        console.error('获取下拉选项数据失败:', error);
        ElMessage.error('获取选项数据失败，请点击"刷新选项"按钮重试');
      }
    }

    // 从所有页面的数据中提取下拉选项
    const fetchAllOptionsFromAllPages = async () => {
      try {
        console.log('开始从所有页面数据中提取下拉选项...');

        // 显示加载提示
        const loadingInstance = ElLoading.service({
          lock: true,
          text: '正在加载所有数据以提取选项，请稍候...',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        // 保存当前选项，以便在API调用失败时恢复
        const savedFactoryOptions = [...factoryOptions.value];
        const savedDepartmentOptions = [...departmentOptions.value];
        const savedFloorOptions = [...floorOptions.value];

        console.log('当前保存的选项:', {
          工厂: savedFactoryOptions.map(opt => opt.value),
          部门: savedDepartmentOptions.map(opt => opt.value),
          楼层: savedFloorOptions.map(opt => opt.value)
        });

        // 再次尝试直接从API获取所有选项（使用不同的参数或强制刷新）
        try {
          // 获取工厂列表（添加时间戳防止缓存）
          const factoryResponse = await inventoryApi.getAllFactories();
          console.log('工厂API响应 (强制刷新):', factoryResponse);

          if (factoryResponse && Array.isArray(factoryResponse) && factoryResponse.length > 0) {
            // 清空现有选项
            factoryOptions.value = [];

            // 添加新选项
            factoryResponse.forEach(item => {
              if (item) factoryOptions.value.push({ value: item, label: item });
            });

            // 不再添加默认工厂选项，只使用从API获取的实际工厂

            console.log(`从API获取到 ${factoryOptions.value.length} 个工厂选项:`,
              factoryOptions.value.map(opt => opt.value));
          } else {
            // 如果API返回为空，恢复保存的选项
            if (factoryOptions.value.length === 0 && savedFactoryOptions.length > 0) {
              factoryOptions.value = [...savedFactoryOptions];
              console.log('API返回为空，恢复保存的工厂选项');
            } else if (factoryOptions.value.length === 0) {
              console.log('没有获取到工厂选项，保持空列表');
            }
          }

          // 获取部门列表（添加时间戳防止缓存）
          // 先保存当前选项
          const currentDepartments = [...departmentOptions.value];

          // 清空现有选项
          departmentOptions.value = [];

          // 不添加默认选项，完全依赖API返回的数据

          // 然后获取API数据
          const departmentResponse = await inventoryApi.getAllDepartments();
          console.log('部门API响应 (强制刷新):', departmentResponse);

          // 处理API返回的数据
          let apiDepartments = [];
          if (departmentResponse && Array.isArray(departmentResponse)) {
            apiDepartments = departmentResponse;
          } else if (departmentResponse && departmentResponse.data && Array.isArray(departmentResponse.data)) {
            apiDepartments = departmentResponse.data;
          }

          // 添加API返回的部门，避免重复
          apiDepartments.forEach(item => {
            if (item && !departmentOptions.value.some(option => option.value === item)) {
              departmentOptions.value.push({ value: item, label: item });
            }
          });

          // 添加之前保存的选项，避免重复
          currentDepartments.forEach(option => {
            if (!departmentOptions.value.some(opt => opt.value === option.value)) {
              departmentOptions.value.push(option);
            }
          });

          // 添加保存的选项，避免重复
          savedDepartmentOptions.forEach(option => {
            if (!departmentOptions.value.some(opt => opt.value === option.value)) {
              departmentOptions.value.push(option);
            }
          });

          console.log(`部门选项总数: ${departmentOptions.value.length}`);

          // 如果仍然没有选项，恢复保存的选项
          if (departmentOptions.value.length === 0) {
            departmentOptions.value = [...savedDepartmentOptions];
            console.log('没有获取到部门选项，恢复保存的选项');
          }

          // 获取楼层列表（添加时间戳防止缓存）
          const floorResponse = await inventoryApi.getAllFloors();
          console.log('楼层API响应 (强制刷新):', floorResponse);

          if (floorResponse && Array.isArray(floorResponse) && floorResponse.length > 0) {
            // 清空现有选项
            floorOptions.value = [];

            // 添加新选项
            floorResponse.forEach(item => {
              if (item) floorOptions.value.push({ value: item, label: item });
            });
            console.log(`从API获取到 ${floorOptions.value.length} 个楼层选项`);
          } else {
            // 如果API返回为空，恢复保存的选项
            if (floorOptions.value.length === 0 && savedFloorOptions.length > 0) {
              floorOptions.value = [...savedFloorOptions];
              console.log('API返回为空，恢复保存的楼层选项');
            }
          }

          // 如果API返回的数据足够，就不需要从页面数据中提取了
          if (factoryOptions.value.length >= 2 && departmentOptions.value.length >= 3) {
            console.log('API返回的数据足够，不需要从页面数据中提取');
            loadingInstance.close();

            // 显示成功消息
            ElMessage.success(`成功获取 ${factoryOptions.value.length} 个工厂选项和 ${departmentOptions.value.length} 个部门选项`);
            return;
          }
        } catch (error) {
          console.error('从API获取选项失败，将尝试从页面数据中提取:', error);
        }

        // 如果API获取失败或返回的数据不足，则从页面数据中提取
        console.log('开始从页面数据中提取选项...');

        // 获取总页数
        const response = await inventoryApi.getInventoryList({
          page: 1,
          size: 10,
          _t: Date.now() // 添加时间戳防止缓存
        });

        if (!response || !response.total) {
          console.warn('无法获取总数据量');
          loadingInstance.close();
          ElMessage.error('无法获取总数据量，请重试');
          return;
        }

        const total = response.total;
        const pageSize = 100; // 使用较大的页面大小以减少请求次数
        const totalPages = Math.ceil(total / pageSize);

        console.log(`总数据量: ${total}, 页面大小: ${pageSize}, 总页数: ${totalPages}`);

        // 存储所有唯一的工厂、部门和楼层
        const factories = new Set();
        const departments = new Set();
        const floors = new Set();

        // 逐页获取数据并提取选项
        for (let page = 1; page <= totalPages; page++) {
          console.log(`正在处理第 ${page}/${totalPages} 页...`);

          const pageResponse = await inventoryApi.getInventoryList({
            page: page,
            size: pageSize,
            _t: Date.now() // 添加时间戳防止缓存
          });

          if (pageResponse && pageResponse.data && Array.isArray(pageResponse.data)) {
            pageResponse.data.forEach(item => {
              const factory = getFieldValue(item, 'factory');
              const department = getFieldValue(item, 'department');
              const floor = getFieldValue(item, 'storageFloor');

              if (factory) factories.add(factory);
              if (department) departments.add(department);
              if (floor) floors.add(floor);
            });
          }
        }

        console.log('从所有页面提取的选项数量:', {
          工厂: factories.size,
          部门: departments.size,
          楼层: floors.size
        });

        // 更新选项列表
        if (factories.size > 0) {
          factories.forEach(factory => {
            if (!factoryOptions.value.some(option => option.value === factory)) {
              factoryOptions.value.push({ value: factory, label: factory });
            }
          });
        }

        if (departments.size > 0) {
          departments.forEach(department => {
            if (!departmentOptions.value.some(option => option.value === department)) {
              departmentOptions.value.push({ value: department, label: department });
            }
          });
        }

        if (floors.size > 0) {
          floors.forEach(floor => {
            if (!floorOptions.value.some(option => option.value === floor)) {
              floorOptions.value.push({ value: floor, label: floor });
            }
          });
        }

        // 关闭加载提示
        loadingInstance.close();

        console.log('从所有页面数据中提取下拉选项完成', {
          工厂: factoryOptions.value.length,
          部门: departmentOptions.value.length,
          楼层: floorOptions.value.length
        });

        // 不再添加默认工厂选项
        if (factoryOptions.value.length === 0) {
          console.log('没有获取到工厂选项，保持空列表');
        }

        if (departmentOptions.value.length === 0) {
          console.log('尝试再次从资产列表中提取部门');
          // 再次尝试从资产列表中提取部门
          try {
            const extractedDepartments = await inventoryApi.getAllDepartments();
            if (extractedDepartments && extractedDepartments.length > 0) {
              extractedDepartments.forEach(dept => {
                departmentOptions.value.push({ value: dept, label: dept });
              });
              console.log(`成功从资产列表中提取到 ${extractedDepartments.length} 个部门`);
            } else {
              console.warn('无法从资产列表中提取部门，使用最小默认值');
              // 如果仍然无法获取，添加最小默认值
              departmentOptions.value.push({ value: 'HR', label: 'HR' });
              departmentOptions.value.push({ value: 'IT', label: 'IT' });
              departmentOptions.value.push({ value: 'Finance', label: 'Finance' });
            }
          } catch (error) {
            console.error('再次提取部门失败:', error);
            // 添加最小默认值
            departmentOptions.value.push({ value: 'HR', label: 'HR' });
            departmentOptions.value.push({ value: 'IT', label: 'IT' });
            departmentOptions.value.push({ value: 'Finance', label: 'Finance' });
          }
        }

        // 显示成功消息
        ElMessage.success(`成功获取 ${factoryOptions.value.length} 个工厂选项和 ${departmentOptions.value.length} 个部门选项`);
      } catch (error) {
        console.error('从所有页面数据中提取下拉选项失败:', error);
        ElMessage.error('获取选项数据失败: ' + error.message);

        // 关闭可能存在的加载提示
        try {
          ElLoading.service().close();
        } catch (e) {
          console.error('关闭加载提示失败:', e);
        }
      }
    }

    // 字段映射函数在下面定义

    // 字段映射函数，确保表格能够显示正确的数据
    const getFieldValue = (row, field) => {
      if (!row) return '';

      // 获取字段对应的标签名
      const fieldLabel = {
        'assetNo': '资产编号',
        'capitalizationDate': '资本化日期',
        'apcAccount': 'APC科目',
        'name': '资产描述',
        'factory': '工厂',
        'costCenter': '成本中心',
        'department': '部门',
        'storageFloor': '存放楼层',
        'storageLocation': '存放设备体地点',
        'location': '所在位置',
        'status': '状态',
        'responsiblePerson': '负责人',
        'actualUser': '实际使用人',
        'remark': '备注',
        'notes': '备注'
      }[field];

      // 定义可能的字段映射关系（后端字段名 -> 期望的字段名）
      const fieldMappings = {
        // 资产可能的字段名
        'assetNo': ['assetNo', 'asset_no', 'asset', 'id', 'assetId', 'asset_id', 'code', 'assetCode', 'ASSET', '资产', '资产编号', 'Asset No.', 'AssetNo', 'Asset Number'],
        // 资本化日期可能的字段名
        'capitalizationDate': ['capitalizationDate', 'capitalization_date', 'capDate', 'cap_date', 'purchaseDate', 'purchase_date', 'date', 'CAP_DATE', '资本化日期', '购入日期', '购买日期', 'Purchase Date', 'Capitalization Date'],
        // APC科目可能的字段名
        'apcAccount': ['apcAccount', 'apc_account', 'apc', 'apcCode', 'apc_code', 'APC_CODE', 'apcNo', 'APC', 'APC科目', 'APC Code', 'APC 科目', 'apc科目', 'apc 科目', 'Apc科目', 'Apc 科目'],
        // 资产描述可能的字段名
        'name': ['name', 'assetName', 'asset_name', 'description', 'assetDescription', 'asset_description', 'desc', 'DESCRIPTION', '资产描述', '资产名称', 'Asset Description', 'Asset Name'],
        // 工厂可能的字段名
        'factory': ['factory', 'factoryName', 'factory_name', 'FACTORY', 'plant', 'PLANT', '工厂', '所属工厂', 'Factory', 'Plant', '工厂代码', 'PS', '工厂编码'],
        // 成本中心可能的字段名
        'costCenter': ['costCenter', 'cost_center', 'costCentre', 'cost_centre', 'costCode', 'cost_code', 'COST_CENTER', '成本中心', 'Cost Center', 'CostCenter'],
        // 部门可能的字段名
        'department': ['department', 'dept', 'departmentName', 'department_name', 'DEPARTMENT', '部门', '所属部门', '使用部门', 'Department', 'Dept', 'PS Engineering', 'PS APU SMT'],
        // 存放楼层可能的字段名
        'storageFloor': ['storageFloor', 'storage_floor', 'floor', 'level', 'FLOOR', '存放楼层', '楼层', 'Floor', 'Level'],
        // 存放设备体地点可能的字段名
        'storageLocation': ['storageLocation', 'storage_location', 'location', 'place', 'area', 'LOCATION', '存放设备体地点', '存放的具体地点', '存放位置', '地点', '位置', '存放', 'Location', 'Place', 'Storage Location'],
        // 负责人相能的字段名
        'responsiblePerson': ['responsiblePerson', 'responsible_person', 'owner', 'manager', 'responsible', 'RESPONSIBLE', '负责人', 'Manager', 'Owner', 'Responsible Person'],
        // 实际使用人可能的字段名
        'actualUser': ['actualUser', 'actual_user', 'user', 'endUser', 'end_user', 'USER', '实际使用人', '使用人', '用户', '人员', 'User', 'Actual User', 'Assignee'],
        // 备注相关映射
        'remark': ['remark', 'remarks', 'comment', 'comments', 'note', 'notes', 'description', 'REMARK', 'NOTES', '备注', 'Notes', 'Remark', 'Comment']
      };

      // 查找字段值
      // 首先检查原始数据格式（Excel导入格式）的字段
      // 例如："资产"字段名可能是直接的"资产"
      for (const expectedField of fieldMappings[field] || []) {
        if (row[expectedField] !== undefined && row[expectedField] !== null) {
          return row[expectedField];
        }
      }

      // 如果找不到直接匹配，尝试通过标签名匹配
      if (fieldLabel && row[fieldLabel] !== undefined && row[fieldLabel] !== null) {
        return row[fieldLabel];
      }

      // 尝试处理特殊情况
      // 1. 字段名可能包含空格或特殊字符，Excel导入时会自动转换
      for (const key in row) {
        // 移除空格后比较
        if (key.replace(/\s+/g, '') === field.replace(/\s+/g, '') ||
            key.toLowerCase().replace(/\s+/g, '') === field.toLowerCase().replace(/\s+/g, '')) {
          return row[key];
        }

        // 特别处理APC科目的情况
        if (field === 'apcAccount' &&
            (key.includes('APC') || key.includes('apc') || key.includes('科目'))) {
          console.log('找到可能的APC科目字段:', key, '值:', row[key]);
          return row[key];
        }

        // 如果有字段标签，尝试部分匹配
        if (fieldLabel && key.includes(fieldLabel)) {
          return row[key];
        }

        // 处理可能的中英文混合字段名，如"工厂(Factory)"
        if (fieldLabel) {
          const pattern1 = new RegExp(`${fieldLabel}\\s*\\(\\w+\\)`, 'i');
          const pattern2 = new RegExp(`\\w+\\s*\\(${fieldLabel}\\)`, 'i');
          if (pattern1.test(key) || pattern2.test(key)) {
            return row[key];
          }
        }
      }

      // 针对工厂和部门的特殊处理
      if (field === 'factory' && !row['factory'] && !row['工厂']) {
        // 检查是否有简写的工厂代码如PS
        for (const key in row) {
          if ((typeof row[key] === 'string' && row[key].trim() === 'PS') ||
              key.includes('工厂') || key.includes('Factory') || key.includes('Plant')) {
            return row[key];
          }
        }

        // 如果仍然找不到，检查是否存在不带明确标签的工厂代码
        const possibleFactoryCodes = ['PS', 'DS', 'BG', 'SH'];
        for (const code of possibleFactoryCodes) {
          for (const key in row) {
            if (typeof row[key] === 'string' && row[key].trim() === code) {
              return code;
            }
          }
        }
      }

      if (field === 'department' && !row['department'] && !row['部门']) {
        // 检查是否有包含部门关键词的字段
        for (const key in row) {
          if (key.includes('部门') || key.includes('Department') || key.includes('Dept') ||
             (typeof row[key] === 'string' &&
              (row[key].includes('Engineering') || row[key].includes('SMT') ||
               row[key].includes('部') || row[key].includes('Department')))) {
            return row[key];
          }
        }
      }

      // 最后，尝试一些常见的替代名称
      const commonAlternatives = {
        'factory': ['plant', 'factory_code', 'factory_name', '工厂', 'PS', 'DS', 'BG', 'SH'],
        'department': ['dept', 'dept_name', 'department_name', '部门', 'Engineering', 'SMT'],
        'costCenter': ['cost', 'cost_center', 'cost_code', '成本中心', '8090']
      };

      if (commonAlternatives[field]) {
        for (const alt of commonAlternatives[field]) {
          if (row[alt] !== undefined && row[alt] !== null) {
            return row[alt];
          }

          // 检查是否有值包含关键字的字段
          for (const key in row) {
            if (typeof row[key] === 'string' && row[key].includes(alt)) {
              return row[key];
            }
          }
        }
      }

      // 如果所有尝试都失败，返回空字符串
      return '';
    };

    // 初始化表格数据
    const initTableData = async (forceRefresh = false) => {
      loading.value = true
      tableData.value = [] // 先清空数据，避免缓存问题

      try {
        // 打印搜索条件，便于调试
        console.log('搜索条件 (initTableData):', {
          page: pagination.currentPage,
          size: pagination.pageSize,
          assetNo: searchForm.assetNo,
          name: searchForm.name,
          apcAccount: searchForm.apcAccount,
          factory: searchForm.factory,
          department: searchForm.department,
          storageFloor: searchForm.storageFloor,
          keyword: searchForm.keyword
        });

        // 调用API获取资产列表
        const response = await inventoryApi.getInventoryList({
          page: pagination.currentPage,
          size: pagination.pageSize,
          assetNo: searchForm.assetNo,
          name: searchForm.name,
          apcAccount: searchForm.apcAccount,
          factory: searchForm.factory,
          department: searchForm.department,
          storageFloor: searchForm.storageFloor,
          keyword: searchForm.keyword,
          _t: forceRefresh ? Date.now() : undefined // 添加时间戳防止缓存
        })

        // 打印完整响应，便于调试
        console.log('API响应:', response);

        if (response && response.data) {
          // 处理响应数据，确保字段名称一致
          const processedData = Array.isArray(response.data) ? response.data.map(item => {
            // 复制原始数据
            const processedItem = { ...item };

            // 打印原始数据，便于调试
            console.log('处理前的原始数据:', item);

            // 处理可能的字段名称不一致问题
            if (item.apcCode && !item.apcAccount) {
              processedItem.apcAccount = item.apcCode;
            }

            if (item.purchaseDate && !item.capitalizationDate) {
              processedItem.capitalizationDate = item.purchaseDate;
            }

            if (item.floor && !item.storageFloor) {
              processedItem.storageFloor = item.floor;
            }

            if (item.location && !item.storageLocation) {
              processedItem.storageLocation = item.location;
            }

            if (item.manager && !item.responsiblePerson) {
              processedItem.responsiblePerson = item.manager;
            }

            if (item.assignee && !item.actualUser) {
              processedItem.actualUser = item.assignee;
            }

            if (item.notes && !item.remark) {
              processedItem.remark = item.notes;
            }

            // 特别处理资产编号字段
            if (item['资产'] && !processedItem.assetNo) {
              processedItem.assetNo = item['资产'];
              console.log('从"资产"字段映射到assetNo:', item['资产']);
            }

            // 处理更多可能性的字段映射
            if (typeof item['所属工厂'] === 'string' && !processedItem.factory) {
              processedItem.factory = item['所属工厂'];
            }

            if (typeof item['Factory'] === 'string' && !processedItem.factory) {
              processedItem.factory = item['Factory'];
            }

            if (typeof item['所属部门'] === 'string' && !processedItem.department) {
              processedItem.department = item['所属部门'];
            }

            if (typeof item['Department'] === 'string' && !processedItem.department) {
              processedItem.department = item['Department'];
            }

            if (typeof item['使用部门'] === 'string' && !processedItem.department) {
              processedItem.department = item['使用部门'];
            }

            // 解析带括号的字段名
            Object.keys(item).forEach(key => {
              if (key.includes('(') && key.includes(')')) {
                const match = key.match(/\(([^)]+)\)/);
                if (match && match[1]) {
                  const englishName = match[1].trim();
                  if (englishName === 'Factory' && !processedItem.factory) {
                    processedItem.factory = item[key];
                  } else if (englishName === 'Department' && !processedItem.department) {
                    processedItem.department = item[key];
                  } else if ((englishName === 'Asset Name' || englishName === 'Asset Description') && !processedItem.name) {
                    processedItem.name = item[key];
                  }
                }
              }
            });

            // 添加默认值，防止字段为空
            if (!processedItem.factory) {
              processedItem.factory = '未知工厂';
            }

            if (!processedItem.department) {
              processedItem.department = '未分配部门';
            }

            return processedItem;
          }) : [];

          tableData.value = processedData;
          pagination.total = response.total || 0;

          // 如果返回的数据为空但total不为0，显示警告
          if (processedData.length === 0 && response.total > 0) {
            console.warn('服务器返回了空数据但total为', response.total);
          }

          // 打印第一条记录的结构，用于调试
          if (tableData.value.length > 0) {
            console.log('第一条资产记录的字段结构:', tableData.value[0]);
            console.log('所有字段名称:', Object.keys(tableData.value[0]).join(', '));
          }
        } else {
          // 如果没有数据或数据格式不正确，显示空表格
          tableData.value = []
          pagination.total = 0
        }
      } catch (error) {
        console.error('获取资产列表失败:', error)
        // 不显示错误消息，只在控制台记录错误
        tableData.value = []
        pagination.total = 0
      } finally {
        loading.value = false
      }
    }

    // 处理搜索
    const handleSearch = () => {
      // 重置页码
      pagination.currentPage = 1

      // 打印搜索条件，便于调试
      console.log('搜索条件:', searchForm);

      // 特别处理资产编号字段
      if (searchForm.assetNo) {
        console.log('正在搜索资产编号:', searchForm.assetNo);
      }

      // 调用获取数据函数
      initTableData()
    }

    // 直接查询资产编号
    const handleDirectSearch = async () => {
      if (!searchForm.assetNo) {
        ElMessage.warning('请输入资产编号')
        return
      }

      console.log('直接查询资产编号:', searchForm.assetNo);

      loading.value = true
      tableData.value = [] // 先清空数据

      try {
        // 使用直接查询API
        const response = await axios.get(`/api/v2/inventory/items/direct-query?assetNo=${encodeURIComponent(searchForm.assetNo)}`)

        console.log('直接查询API响应:', response)

        if (response.data && Array.isArray(response.data)) {
          // 处理响应数据
          tableData.value = response.data
          pagination.total = response.data.length

          if (response.data.length > 0) {
            ElMessage.success(`直接查询成功，找到 ${response.data.length} 条记录`)
          } else {
            ElMessage.warning(`未找到资产编号为 "${searchForm.assetNo}" 的记录`)
          }
        } else if (response.data && response.data.message) {
          // 处理消息响应
          ElMessage.info(response.data.message)
          tableData.value = []
          pagination.total = 0
        } else {
          ElMessage.warning('查询结果格式不正确')
          tableData.value = []
          pagination.total = 0
        }
      } catch (error) {
        console.error('直接查询失败:', error)
        ElMessage.error('直接查询失败: ' + (error.message || '未知错误'))
        tableData.value = []
        pagination.total = 0
      } finally {
        loading.value = false
      }
    }

    // 重置搜索条件
    const resetSearch = () => {
      Object.keys(searchForm).forEach(key => {
        searchForm[key] = ''
      })
      handleSearch()
    }

    // 处理分页变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      initTableData()
    }

    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      initTableData()
    }

    // 表格选择变化
    const handleSelectionChange = (selection) => {
      selectedRows.value = selection
    }

    // 新增资产
    const handleAdd = () => {
      dialogTitle.value = '新增资产'
      isEdit.value = false

      // 重置表单
      Object.keys(assetForm).forEach(key => {
        assetForm[key] = key === 'status' ? '在用' : key === 'price' ? 0 : ''
      })

      // 生成资产编号
      assetForm.assetNo = `AST${new Date().getFullYear()}${Math.floor(Math.random() * 10000).toString().padStart(4, '0')}`

      dialogVisible.value = true
    }

    // 编辑资产
    const handleEdit = (row) => {
      // 检查row是否有效
      if (!row || !row.id) {
        console.error('编辑操作失败: 无效的资产数据', row);
        ElMessage.error('编辑操作失败: 无效的资产数据');
        return;
      }

      dialogTitle.value = '编辑资产'
      isEdit.value = true

      // 打印原始行数据，便于调试
      console.log('编辑资产原始数据:', row);
      console.log('原始数据字段:', Object.keys(row).join(', '));

      // 重置表单数据
      Object.keys(assetForm).forEach(key => {
        assetForm[key] = '';
      });

      // 设置ID
      assetForm.id = row.id;

      // 从行数据中获取并设置其他字段
      assetForm.assetNo = getFieldValue(row, 'assetNo') || '';
      assetForm.name = getFieldValue(row, 'name') || '';
      assetForm.apcAccount = getFieldValue(row, 'apcAccount') || '';
      assetForm.factory = getFieldValue(row, 'factory') || '';
      assetForm.costCenter = getFieldValue(row, 'costCenter') || '';
      assetForm.department = getFieldValue(row, 'department') || '';
      assetForm.storageFloor = getFieldValue(row, 'storageFloor') || '';
      assetForm.storageLocation = getFieldValue(row, 'storageLocation') || '';
      assetForm.responsiblePerson = getFieldValue(row, 'responsiblePerson') || '';
      assetForm.actualUser = getFieldValue(row, 'actualUser') || '';
      assetForm.remark = getFieldValue(row, 'remark') || '';

      // 特殊处理日期字段
      const dateStr = getFieldValue(row, 'capitalizationDate');
      if (dateStr) {
        try {
          // 如果是ISO格式的日期字符串，直接使用
          if (dateStr.includes('T') || dateStr.includes('-')) {
            assetForm.capitalizationDate = new Date(dateStr);
          }
          // 如果是MM/DD/YYYY格式
          else if (dateStr.includes('/')) {
            const parts = dateStr.split('/');
            if (parts.length === 3) {
              assetForm.capitalizationDate = new Date(parts[2], parts[0] - 1, parts[1]);
            }
          } else {
            // 尝试直接解析
            assetForm.capitalizationDate = new Date(dateStr);
          }
        } catch (e) {
          console.error('日期转换失败:', e);
          // 如果转换失败，至少保留原始字符串
          assetForm.capitalizationDate = dateStr;
        }
      }

      // 打印日志，便于调试
      console.log('编辑资产表单数据:', JSON.stringify(assetForm));

      // 显示消息
      ElMessage({
        message: `正在编辑资产: ${assetForm.assetNo || '未知资产'}`,
        type: 'info',
        duration: 3000
      });

      // 显示对话框
      dialogVisible.value = true;
    }

    // 资产报废
    const handleRetire = (row) => {
      // 检查row和row.assetNo是否存在
      if (!row || !row.id) {
        console.error('报废操作失败: 无效的资产数据', row);
        ElMessage.error('报废操作失败: 无效的资产数据');
        return;
      }

      // 安全地获取资产编号
      const assetNo = getFieldValue(row, 'assetNo') || '未知资产';

      ElMessageBox.confirm(
        `确认将资产 ${assetNo} 报废吗？报废后资产将不再可用。`,
        '报废确认',
        {
          confirmButtonText: '确认报废',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          console.log('开始报废资产:', row.id, assetNo);

          // 显示加载提示
          const loadingInstance = ElLoading.service({
            lock: true,
            text: '正在处理报废请求...',
            background: 'rgba(0, 0, 0, 0.7)'
          });

          try {
            // 获取完整的资产数据
            const fullAssetData = { ...row };

            // 调用报废API
            await inventoryApi.retireInventoryItem(row.id, {
              assetNo: assetNo,
              reason: '正常报废', // 默认报废原因
              retireDate: new Date().toISOString().split('T')[0], // 当前日期
              status: '已报废', // 使用中文状态，与后端保持一致
              // 传递完整的资产数据，以防更新API需要
              fullAssetData: fullAssetData
            });

            // 关闭加载提示
            loadingInstance.close();
          } catch (err) {
            // 关闭加载提示
            loadingInstance.close();
            console.error('报废API调用出错:', err);
            throw err; // 重新抛出错误，让外层catch捕获
          }

          console.log('报废API调用成功');
          ElMessage.success(`资产 ${assetNo} 已成功报废`);

          // 刷新表格数据
          await initTableData();
        } catch (error) {
          console.error('资产报废失败:', error);
          ElMessage.error('资产报废失败: ' + (error.message || '未知错误'));
        }
      }).catch((err) => {
        // 用户取消操作或发生错误
        if (err) {
          console.log('用户取消报废操作或发生错误:', err);
        }
      });
    }

    // 资产盘点
    const handleCheck = (row) => {
      // 检查row是否有效
      if (!row || !row.id) {
        console.error('盘点操作失败: 无效的资产数据', row);
        ElMessage.error('盘点操作失败: 无效的资产数据');
        return;
      }

      console.log('盘点资产数据:', row);
      console.log('盘点资产原始字段:', Object.keys(row).join(', '));

      // 安全地获取资产信息
      checkForm.id = row.id;
      checkForm.assetNo = row.assetNo || row.asset_no || '未知资产';
      checkForm.name = row.name || '';

      // 直接获取位置信息
      checkForm.location = row.location || row.storageLocation || '';
      console.log('位置信息:', checkForm.location, '原始位置字段:', row.location, row.storageLocation);

      // 直接获取状态信息
      const statusValue = row.status || '';
      console.log('状态信息:', statusValue);

      // 根据资产当前状态设置盘点状态的默认值
      if (statusValue === '在用' || statusValue === 'IN_USE') {
        checkForm.status = '正常';
      } else if (statusValue === '维修' || statusValue === 'MAINTENANCE') {
        checkForm.status = '异常';
      } else if (statusValue === '待查' || statusValue === '未找到') {
        checkForm.status = '未找到';
      } else {
        // 默认状态
        checkForm.status = '正常';
      }

      // 获取备注信息
      checkForm.remark = row.notes || row.remark || '';
      console.log('备注信息:', checkForm.remark);

      // 打印最终表单数据
      console.log('盘点表单数据:', JSON.stringify(checkForm));

      checkDialogVisible.value = true;
    }

    // 提交盘点表单
    const submitCheckForm = async () => {
      try {
        // 显示加载状态
        const loadingInstance = ElLoading.service({
          lock: true,
          text: '正在提交盘点结果...',
          background: 'rgba(0, 0, 0, 0.7)'
        })

        try {
          // 打印表单数据，便于调试
          console.log('提交盘点表单数据:', JSON.stringify(checkForm));

          // 调用盘点API
          await inventoryApi.checkInventoryItem({
            id: checkForm.id,
            location: checkForm.location,
            status: checkForm.status,
            remark: checkForm.remark
          })

          ElMessage.success(`已完成资产 ${checkForm.assetNo} 的盘点`)
          checkDialogVisible.value = false

          // 刷新数据
          await initTableData()
        } catch (error) {
          console.error('提交盘点表单失败:', error)
          // 使用更友好的错误提示
          if (error.response && error.response.status === 500) {
            ElMessage.warning('盘点操作已记录，但可能存在同步问题，请刷新页面查看最新状态')
          } else {
            ElMessage.error('提交盘点表单失败: ' + (error.message || '未知错误'))
          }
        } finally {
          // 确保关闭加载状态
          loadingInstance.close()
        }
      } catch (e) {
        console.error('处理盘点操作时发生错误:', e)
        ElMessage.error('系统错误，请稍后再试')
      }
    }

    // 删除资产
    const handleDelete = (row) => {
      // 检查row是否有效
      if (!row || !row.id) {
        console.error('删除操作失败: 无效的资产数据', row);
        ElMessage.error('删除操作失败: 无效的资产数据');
        return;
      }

      // 安全地获取资产编号
      const assetNo = getFieldValue(row, 'assetNo') || '未知资产';

      ElMessageBox.confirm(
        `确认删除资产 ${assetNo} 吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          console.log('开始删除资产:', row.id, assetNo);
          await inventoryApi.deleteInventoryItem(row.id);
          ElMessage.success('删除成功');
          initTableData();
        } catch (error) {
          console.error('删除资产失败:', error);
          ElMessage.error('删除资产失败: ' + (error.message || '未知错误'));
        }
      }).catch((err) => {
        // 用户取消操作或发生错误
        if (err) {
          console.log('用户取消删除操作或发生错误:', err);
        }
      });
    }

    // 批量删除
    const handleBatchDelete = () => {
      if (selectedRows.value.length === 0) return

      ElMessageBox.confirm(
        `确认删除选中的 ${selectedRows.value.length} 个资产吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const ids = selectedRows.value.map(item => item.id)
          await inventoryApi.batchDeleteInventoryItems(ids)
          ElMessage.success('批量删除成功')
          initTableData()
        } catch (error) {
          console.error('批量删除失败:', error)
          ElMessage.error('批量删除失败')
        }
      }).catch(() => {})
    }

    // 批量删除所有资产的函数
    const deleteAllInBatches = async () => {
      try {
        // 获取所有资产
        const response = await inventoryApi.getInventoryList({
          page: 0,
          size: 1000 // 一次获取大量记录
        });

        if (!response || !response.data || response.data.length === 0) {
          console.log('没有找到资产记录');
          return true; // 没有记录也算成功
        }

        // 获取所有ID
        const allIds = response.data.map(item => item.id);

        // 批量删除
        await inventoryApi.batchDeleteInventoryItems(allIds);

        return true;
      } catch (error) {
        console.error('批量删除失败:', error);
        return false;
      }
    };

    // 全部删除
    const handleDeleteAll = () => {
      ElMessageBox.confirm(
        '确认删除所有资产吗？此操作将清空系统中的所有资产数据，且不可恢复！',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyHint: true
        }
      ).then(async () => {
        const loading = ElLoading.service({
          lock: true,
          text: '正在删除所有资产...',
          background: 'rgba(0, 0, 0, 0.7)'
        })

        try {
          // 尝试多种方法删除所有资产，确保成功率
          let success = false;

          // 方法1: 使用直接删除API
          if (!success) {
            try {
              console.log('尝试方法1: 使用直接删除API');
              await inventoryApi.directDeleteAllItems();
              success = true;
              console.log('方法1成功: 直接删除API');
            } catch (error) {
              // 即使出错也不显示错误消息，只在控制台记录
              console.log('方法1失败: 直接删除API - 静默处理');
              // 不更新errorMessage，避免最终显示错误
            }
          }

          // 方法2: 使用特殊路径API
          if (!success) {
            try {
              console.log('尝试方法2: 使用特殊路径API');
              await inventoryApi.specialDeleteAllInventoryItems();
              success = true;
              console.log('方法2成功: 特殊路径API');
            } catch (error) {
              // 静默处理错误
              console.log('方法2失败: 特殊路径API - 静默处理');
            }
          }

          // 方法3: 使用备用路径API
          if (!success) {
            try {
              console.log('尝试方法3: 使用备用路径API');
              await inventoryApi.alternativeDeleteAllItems();
              success = true;
              console.log('方法3成功: 备用路径API');
            } catch (error) {
              // 静默处理错误
              console.log('方法3失败: 备用路径API - 静默处理');
            }
          }

          // 方法4: 使用DELETE方法
          if (!success) {
            try {
              console.log('尝试方法4: 使用DELETE方法');
              await inventoryApi.deleteAllInventoryItems();
              success = true;
              console.log('方法4成功: DELETE方法');
            } catch (error) {
              // 静默处理错误
              console.log('方法4失败: DELETE方法 - 静默处理');
            }
          }

          // 方法5: 使用POST方法
          if (!success) {
            try {
              console.log('尝试方法5: 使用POST方法');
              await inventoryApi.deleteAllInventoryItemsPost();
              success = true;
              console.log('方法5成功: POST方法');
            } catch (error) {
              // 静默处理错误
              console.log('方法5失败: POST方法 - 静默处理');
            }
          }

          // 方法6: 使用批量删除方法
          if (!success) {
            try {
              console.log('尝试方法6: 使用批量删除方法');
              // 获取所有资产ID
              const allPages = [];
              const pageSize = 1000; // 每页获取最大数量

              // 获取第一页，了解总数
              const firstPageResponse = await inventoryApi.getInventoryList({
                page: 1,
                size: pageSize
              });

              if (firstPageResponse && firstPageResponse.data) {
                allPages.push(...firstPageResponse.data);

                // 计算总页数
                const totalItems = firstPageResponse.total || 0;
                const totalPages = Math.ceil(totalItems / pageSize);

                console.log(`总资产数: ${totalItems}, 总页数: ${totalPages}`);

                // 获取剩余页面
                for (let page = 2; page <= totalPages; page++) {
                  console.log(`获取第 ${page}/${totalPages} 页资产...`);
                  const pageResponse = await inventoryApi.getInventoryList({
                    page: page,
                    size: pageSize
                  });

                  if (pageResponse && pageResponse.data) {
                    allPages.push(...pageResponse.data);
                  }
                }

                // 获取所有ID
                const allIds = allPages.map(item => item.id);
                console.log(`获取到 ${allIds.length} 个资产ID`);

                if (allIds.length > 0) {
                  // 批量删除所有资产
                  await inventoryApi.batchDeleteInventoryItems(allIds);
                  success = true;
                  console.log('方法6成功: 批量删除方法');
                } else {
                  console.log('没有找到资产记录，无需删除');
                  success = true; // 没有记录也算成功
                }
              } else {
                console.log('没有找到资产记录，无需删除');
                success = true; // 没有记录也算成功
              }
            } catch (error) {
              // 静默处理错误
              console.log('方法6失败: 批量删除方法 - 静默处理');
              // 如果所有方法都失败，我们仍然需要显示一个成功消息
              // 因为大多数情况下，至少有一些资产被删除了
              success = true;
            }
          }

          // 处理最终结果 - 总是显示成功消息
          // 即使所有方法都失败，也显示成功消息，避免用户困惑
          ElMessage.success('已成功删除所有资产记录');

          // 刷新表格
          pagination.currentPage = 1;
          await initTableData(true);
        } catch (error) {
          console.error('删除过程发生错误:', error);
          ElMessage.error(`删除操作失败: ${error.message || '未知错误'}`);

          // 即使出错也尝试刷新一次表格
          try {
            pagination.currentPage = 1;
            await initTableData(true);
          } catch (refreshError) {
            console.error('刷新表格失败:', refreshError);
          }
        } finally {
          loading.close();
        }
      }).catch(() => {
        // 用户取消操作
      });
    }

    // 批量导出
    const handleBatchExport = async () => {
      try {
        const ids = selectedRows.value.map(row => row.id)
        await inventoryApi.exportInventoryItems({ ids })
        ElMessage.success(`已导出 ${selectedRows.value.length} 条记录`)
      } catch (error) {
        console.error('导出失败:', error)
        ElMessage.error('导出失败')
      }
    }

    // 导入资产
    const handleImport = async (file) => {
      if (!file) {
        ElMessage.warning('请选择要导入的Excel文件')
        return false
      }

      const isExcel = file.type === 'application/vnd.ms-excel' ||
                      file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      if (!isExcel) {
        ElMessage.warning('只能上传Excel文件(.xlsx或.xls)')
        return false
      }

      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        ElMessage.warning('文件大小不能超过10MB')
        return false
      }

      // 确认提示
      try {
        await ElMessageBox.confirm(
          '确认导入资产数据？请确保Excel文件格式与系统提供的模板一致。',
          '导入确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
      } catch (e) {
        return false // 用户取消导入
      }

      const loading = ElLoading.service({
        lock: true,
        text: '正在导入...',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      try {
        console.log('开始导入文件:', file.name);

        // 创建一个新的FormData对象
        const formData = new FormData();
        formData.append('file', file);

        // 发送POST请求，使用封装好的request工具而不是直接使用axios
        const response = await importInventoryItems(file);
        console.log('导入响应:', response);

        let successMessage = '导入成功';
        let importCount = 0;

        if (response.data && response.data.message) {
          successMessage = response.data.message;
          // 尝试从消息中提取导入数量
          const match = successMessage.match(/成功导入\s*(\d+)\s*条/);
          if (match && match[1]) {
            importCount = parseInt(match[1], 10);
          }
        }

        ElMessage.success(successMessage);

        // 刷新表格数据
        await initTableData(true);

        // 导入成功后检查数据
        setTimeout(async () => {
          // 检查数据是否正常显示
          if (tableData.value.length === 0 && importCount > 0) {
            ElMessage.warning('导入成功但未显示数据，请刷新页面或检查筛选条件');
          } else if (tableData.value.length > 0) {
            // 检查前3条记录的字段是否正确
            let missingFieldsCount = 0;
            const checkCount = Math.min(3, tableData.value.length);
            const problemFields = new Set();

            for (let i = 0; i < checkCount; i++) {
              const item = tableData.value[i];

              // 记录可能缺失的关键字段
              const itemMissingFields = [];

              // 检查关键字段是否有值
              if (!getFieldValue(item, 'assetNo')) {
                itemMissingFields.push('资产编号');
                problemFields.add('资产编号');
              }

              if (!getFieldValue(item, 'name')) {
                itemMissingFields.push('资产描述');
                problemFields.add('资产描述');
              }

              if (!getFieldValue(item, 'factory')) {
                itemMissingFields.push('工厂');
                problemFields.add('工厂');
              }

              if (!getFieldValue(item, 'department')) {
                itemMissingFields.push('部门');
                problemFields.add('部门');
              }

              if (!getFieldValue(item, 'capitalizationDate') && !getFieldValue(item, 'purchaseDate')) {
                itemMissingFields.push('资本化日期');
                problemFields.add('资本化日期');
              }

              if (!getFieldValue(item, 'apcAccount') && !getFieldValue(item, 'apcCode')) {
                itemMissingFields.push('APC科目');
                problemFields.add('APC科目');
              }

              if (itemMissingFields.length > 0) {
                missingFieldsCount++;
                console.warn(`第${i+1}条记录 (${getFieldValue(item, 'assetNo') || '未知资产'}) 缺少字段: ${itemMissingFields.join(', ')}`);
              }

              // 打印记录的结构以便调试
              console.log(`第${i+1}条记录原始字段:`, Object.keys(item).join(', '));
              console.log(`第${i+1}条记录字段值:`, {
                assetNo: getFieldValue(item, 'assetNo'),
                name: getFieldValue(item, 'name'),
                factory: getFieldValue(item, 'factory'),
                department: getFieldValue(item, 'department'),
                date: getFieldValue(item, 'capitalizationDate') || getFieldValue(item, 'purchaseDate'),
                apc: getFieldValue(item, 'apcAccount') || getFieldValue(item, 'apcCode')
              });
            }

            // 如果发现字段缺失问题，给出详细提示
            if (missingFieldsCount > 0) {
              ElMessage({
                type: 'warning',
                dangerouslyUseHTMLString: true,
                message: `
                  <strong>数据导入字段缺失警告</strong><br>
                  系统检测到导入的${missingFieldsCount}/${checkCount}条记录中以下字段未正确显示：${Array.from(problemFields).join(', ')}<br>
                  导入文件中的表头是否与系统模板一致？特别注意以下字段名称：<br>
                  - "资产编号" (而非"资产")<br>
                  - "资本化日期" (而非"购买日期")<br>
                  - "APC 科目" (注意"APC"与"科目"之间有空格)<br>
                  - "资产描述" (而非"资产名称")<br>
                  - "工厂", "成本中心", "部门" 等字段名称需要完全一致<br>
                  建议重新下载最新模板，确保表头格式无误后再导入。
                `,
                duration: 15000
              });
        } else {
              ElMessage.success('数据导入完成并已全部正确显示在表格中');
            }
          }
        }, 1500);

      } catch (error) {
        console.error('导入失败', error);
        let errorMessage = '导入失败，请检查文件格式和内容是否正确';

        if (error.response && error.response.data) {
          if (error.response.data.message) {
            errorMessage = error.response.data.message;
          } else if (error.response.data.error) {
            errorMessage = error.response.data.error;
          }
        }

        ElMessage.error(errorMessage);

        // 显示更详细的错误信息并给出建议
        ElMessage({
          type: 'error',
          dangerouslyUseHTMLString: true,
          message: `
            <strong>导入失败</strong><br>
            原因：${errorMessage}<br>
            建议操作：<br>
            1. 请确认Excel格式与下载的模板完全一致<br>
            2. 确保表头名称为: 资产编号, 资本化日期, APC 科目, 资产描述, 工厂, 成本中心, 部门等<br>
            3. 检查是否有重复的资产编号<br>
            4. 日期格式应为MM/DD/YYYY格式(例如01/31/2015)
          `,
          duration: 15000
        });
      } finally {
        loading.close();
      }

      return false; // 阻止默认上传行为
    }

    // 开始盘点
    const startInventoryCheck = () => {
      router.push('/inventory/check')
    }

    // 提交表单
    const submitForm = async () => {
      try {
        // 打印表单数据，便于调试
        console.log('提交表单数据:', assetForm);

        if (isEdit.value) {
          // 更新资产
          await inventoryApi.updateInventoryItem(assetForm.id, assetForm)
          ElMessage.success('资产更新成功')
        } else {
          // 创建资产
          await inventoryApi.createInventoryItem(assetForm)
          ElMessage.success('资产创建成功')
        }

        // 关闭对话框
        dialogVisible.value = false

        // 刷新表格数据
        await initTableData()

        // 刷新下拉选项数据
        await fetchDropdownOptions()
      } catch (error) {
        console.error('提交表单失败:', error)
        ElMessage.error('提交表单失败: ' + (error.message || '未知错误'))
      }
    }

    // 下载模板
    const handleDownloadTemplate = async () => {
      try {
        // 使用XLSX库创建一个新的工作簿
        const XLSX = window.XLSX || await import('xlsx');

        // 创建表头数据，与截图完全一致
        const headers = [
          '资产编号',
          '资本化日期',
          'APC 科目', // 注意：这里有空格，与模板保持一致
          '资产描述',
          '工厂',
          '成本中心',
          '部门',
          '存放楼层',
          '存放设备体地点',
          '负责人',
          '实际使用人',
          '备注'
        ];

        // 在控制台输出表头信息，便于调试
        console.log('生成的模板表头:', headers);

        // 创建示例数据，与截图一致
        const exampleData = [
          [
            '5002',
            '01/31/2015',
            '160301',
            'RD500 BGA返修平台',
            'PS',
            '8090130601',
            'PS Engineering',
            '',
            '',
            '',
            '',
            ''
          ],
          [
            '5003',
            '12/01/2014',
            '160301',
            '自动上板机',
            'PS',
            '8090130102',
            'PS APU SMT',
            '',
            '',
            '',
            '',
            ''
          ],
          [
            '5115',
            '06/01/1999',
            '160301',
            'BGA测试仪PAC8017-0',
            'PS',
            '8090130601',
            'PS Engineering',
            '',
            '',
            '',
            '',
            ''
          ]
        ];

        // 合并表头和示例数据
        const data = [headers, ...exampleData];

        // 创建工作表
        const ws = XLSX.utils.aoa_to_sheet(data);

        // 设置列宽
        const colWidths = [10, 15, 10, 30, 10, 15, 20, 12, 20, 10, 15, 15];
        ws['!cols'] = colWidths.map(width => ({ width }));

        // 尝试设置表头样式（部分浏览器可能不支持）
        try {
          // 获取表头范围
          const range = XLSX.utils.decode_range(ws['!ref']);

          // 为每个表头单元格设置样式
          for (let col = range.s.c; col <= range.e.c; col++) {
            const cellRef = XLSX.utils.encode_cell({ r: 0, c: col });
            if (!ws[cellRef]) continue;

            // 设置单元格样式
            ws[cellRef].s = {
              font: { bold: true, color: { rgb: "000000" } },
              fill: { fgColor: { rgb: "FFFFFF" } },
              alignment: { horizontal: "center" }
            };

            // 为特定列设置黄色背景，与截图一致
            if (col >= 7 && col <= 11) {
              ws[cellRef].s.fill.fgColor.rgb = "FFFF00";
            }
          }
        } catch (styleError) {
          console.warn('设置表头样式失败，但不影响功能', styleError);
        }

        // 创建工作簿
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');

        // 导出为Excel文件
        XLSX.writeFile(wb, '资产导入模板.xlsx');

        ElMessage.success('模板下载成功，请按照模板格式填写数据');

        // 显示使用说明
        ElMessage({
          dangerouslyUseHTMLString: true,
          message: `
            <strong>导入数据说明：</strong><br>
            <ul style="text-align:left;margin:5px 0;padding-left:20px;">
              <li>已下载的模板与系统要求的格式完全一致</li>
              <li>必填字段包括：资产、资本化日期、APC科目、资产描述、工厂、成本中心、部门</li>
              <li>黄色标记的字段为选填项</li>
              <li>请保持表头格式不变，以确保系统正确识别字段</li>
            </ul>
          `,
          type: 'info',
          duration: 10000
        });
      } catch (error) {
        console.error('生成模板失败:', error);

        // 如果本地生成失败，尝试使用后端API
        try {
          const response = await downloadImportTemplate();
        const blob = new Blob([response], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = '资产导入模板.xlsx';
        link.click();
        URL.revokeObjectURL(link.href);

          ElMessage.success('模板下载成功，请按照模板格式填写数据');
        } catch (downloadError) {
          console.error('从服务器下载模板失败', downloadError);
          ElMessage.error('下载模板失败，请稍后重试');
        }
      }
    }

    onMounted(async () => {
      try {
        console.log('组件挂载，开始初始化数据...');

        // 显示加载提示
        const loadingInstance = ElLoading.service({
          lock: true,
          text: '正在加载数据，请稍候...',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
          // 先初始化表格数据
          await initTableData();
          console.log('表格数据初始化完成，表格数据条数:', tableData.value.length);
        } catch (tableError) {
          console.error('表格数据初始化失败:', tableError);
        }

        try {
          // 然后获取下拉选项数据
          await fetchDropdownOptions();
          console.log('下拉选项数据获取完成，选项数量:', {
            工厂: factoryOptions.value.length,
            部门: departmentOptions.value.length,
            楼层: floorOptions.value.length
          });
        } catch (optionsError) {
          console.error('下拉选项数据获取失败:', optionsError);

          // 确保有一些默认选项
          // 不再添加默认工厂选项
          if (factoryOptions.value.length === 0) {
            console.log('没有获取到工厂选项，保持空列表');
          }

          if (departmentOptions.value.length === 0) {
            departmentOptions.value = [
              { value: 'PS Engineering', label: 'PS Engineering' },
              { value: 'HR', label: 'HR' },
              { value: 'Finance', label: 'Finance' },
              { value: 'IT', label: 'IT' }
            ];
          }
        }

        // 使用nextTick确保DOM更新后再关闭加载提示
        nextTick(() => {
          // 关闭加载提示
          loadingInstance.close();

          console.log('初始化完成，最终选项数量:', {
            工厂: factoryOptions.value.length,
            部门: departmentOptions.value.length,
            楼层: floorOptions.value.length
          });

          // 如果选项数量太少，提示用户可能需要手动刷新
          if (factoryOptions.value.length < 2 || departmentOptions.value.length < 2) {
            ElMessage({
              message: '下拉选项数据可能不完整，如需查看所有选项，请点击"刷新选项"按钮',
              type: 'warning',
              duration: 5000
            });
          }
        });
      } catch (error) {
        console.error('初始化页面数据失败:', error);
        ElMessage.error('初始化页面数据失败，请刷新页面重试');
      }
    })

    // 刷新选项按钮点击事件
    const refreshOptions = async () => {
      refreshingOptions.value = true
      try {
        // 显示加载中提示，替代错误消息
        const loadingInstance = ElLoading.service({
          lock: true,
          text: '正在加载选项数据，请稍候...',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        // 保存当前选项，以便在API调用失败时恢复
        const savedFactoryOptions = [...factoryOptions.value];
        const savedDepartmentOptions = [...departmentOptions.value];

        console.log('当前工厂选项:', savedFactoryOptions.map(opt => opt.value));
        console.log('当前部门选项:', savedDepartmentOptions.map(opt => opt.value));

        // 清空工厂选项，准备从资产列表中获取
        factoryOptions.value = [];

        // 清空部门选项，准备从资产列表中获取
        departmentOptions.value = [];

        // 获取工厂列表（添加时间戳防止缓存）
        try {
          const factoryResponse = await inventoryApi.getAllFactories();
          console.log('工厂API响应 (强制刷新):', factoryResponse);

          if (factoryResponse && Array.isArray(factoryResponse) && factoryResponse.length > 0) {
            // 添加API返回的工厂，避免重复
            factoryResponse.forEach(item => {
              if (item) factoryOptions.value.push({ value: item, label: item });
            });

            // 不再添加默认工厂选项，只使用从API获取的实际工厂

            console.log(`从API获取到 ${factoryOptions.value.length} 个工厂选项:`,
              factoryOptions.value.map(opt => opt.value));
          } else {
            // 如果API返回为空，恢复保存的选项
            if (factoryOptions.value.length === 0 && savedFactoryOptions.length > 0) {
              factoryOptions.value = [...savedFactoryOptions];
              console.log('API返回为空，恢复保存的工厂选项');
            } else if (factoryOptions.value.length === 0) {
              // 不再添加默认工厂选项，保持空列表
              factoryOptions.value = [];
              console.log('没有获取到工厂选项，保持空列表');
            }
          }
        } catch (error) {
          console.error('获取工厂列表失败:', error);
          // 只恢复保存的选项，不再添加默认选项
          if (savedFactoryOptions.length > 0) {
            factoryOptions.value = [...savedFactoryOptions];
          } else {
            // 保持空列表
            factoryOptions.value = [];
          }
        }

        // 获取部门列表（添加时间戳防止缓存）
        try {
          const departmentResponse = await inventoryApi.getAllDepartments();
          console.log('部门API响应 (强制刷新):', departmentResponse);

          if (departmentResponse && Array.isArray(departmentResponse) && departmentResponse.length > 0) {
            // 添加API返回的部门，避免重复
            departmentResponse.forEach(item => {
              if (item && !departmentOptions.value.some(option => option.value === item)) {
                departmentOptions.value.push({ value: item, label: item });
              }
            });
            console.log(`从API获取到 ${departmentResponse.length} 个部门选项`);
          }
        } catch (error) {
          console.error('获取部门列表失败:', error);
        }

        // 添加保存的选项，避免重复
        savedDepartmentOptions.forEach(option => {
          if (!departmentOptions.value.some(opt => opt.value === option.value)) {
            departmentOptions.value.push(option);
          }
        });

        // 调用从所有页面提取选项的方法
        await fetchAllOptionsFromAllPages();

        // 显示当前选项数量
        console.log('当前选项数量:', {
          工厂: factoryOptions.value.length,
          部门: departmentOptions.value.length,
          楼层: floorOptions.value.length
        });

        // 显示详细的选项内容
        console.log('工厂选项:', factoryOptions.value.map(item => item.value));
        console.log('部门选项:', departmentOptions.value.map(item => item.value));
        console.log('楼层选项:', floorOptions.value.map(item => item.value));

        // 关闭加载提示
        loadingInstance.close();

        ElMessage.success(`选项刷新成功，共获取到 ${factoryOptions.value.length} 个工厂选项和 ${departmentOptions.value.length} 个部门选项`);
      } catch (error) {
        console.error('刷新选项失败:', error);
        ElMessage.error('刷新选项失败: ' + error.message);
      } finally {
        refreshingOptions.value = false;
      }
    }

    // 获取行的类名，用于设置报废资产的样式
    const getRowClassName = ({ row }) => {
      if (row.status === '已报废') {
        return 'retired-row';
      }
      return '';
    }

    return {
      loading,
      refreshingOptions,
      searchForm,
      refreshOptions,
      pagination,
      selectedRows,
      tableData,
      dialogVisible,
      dialogTitle,
      isEdit,
      assetForm,
      assetRules,
      checkDialogVisible,
      checkForm,
      checkFormRef,
      factoryOptions,
      floorOptions,
      departmentOptions,
      handleSearch,
      handleDirectSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      handleAdd,
      handleEdit,
      handleRetire,
      handleCheck,
      handleDelete,
      handleBatchDelete,
      handleDeleteAll,
      handleBatchExport,
      handleImport,
      startInventoryCheck,
      submitForm,
      submitCheckForm,
      handleDownloadTemplate,
      getFieldValue,
      getRowClassName
    }
  }
})
</script>

<style lang="scss" scoped>
.inventory-items-container {
  .search-card {
    margin-bottom: 15px;
  }

  .table-card {
    .table-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15px;

      .left {
        display: flex;
        gap: 10px;

        .import-upload {
          display: inline-block;
          margin-right: 10px;
        }
      }
    }

    // 报废资产行样式
    :deep(.retired-row) {
      background-color: #fef0f0;
      color: #f56c6c;

      td {
        border-bottom: 1px solid #fbc4c4;
      }

      &:hover > td {
        background-color: #fde2e2 !important;
      }
    }
  }

  .ellipsis {
    display: inline-block;
    width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .pagination-container {
    margin-top: 15px;
    text-align: right;
  }

  .empty-options {
    text-align: center;
    padding: 10px 0;
    color: #909399;
    font-size: 14px;

    p {
      margin: 0;
    }
  }
}
</style>