<template>
  <div class="inventory-list">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="资产编号">
        <el-input v-model="searchForm.assetNo" placeholder="请输入资产编号"></el-input>
      </el-form-item>
      <el-form-item label="资产名称">
        <el-input v-model="searchForm.name" placeholder="请输入资产名称"></el-input>
      </el-form-item>
      <el-form-item label="资产类型">
        <el-select v-model="searchForm.type" placeholder="请选择资产类型" clearable>
          <el-option v-for="type in types" :key="type" :label="type" :value="type"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增资产</el-button>
      <el-button type="primary" @click="handleBatchDelete" :disabled="selectedItems.length === 0">批量删除</el-button>
      <el-button type="danger" @click="handleDeleteAll">全部删除</el-button>
      <el-button type="success" @click="handleExport">导出</el-button>
      <el-button type="warning" @click="handleImport">导入</el-button>
    </div>

    <!-- 资产列表表格 -->
    <el-table
      v-loading="loading"
      :data="inventoryItems"
      @selection-change="handleSelectionChange"
      border
      style="width: 100%">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="assetNo" label="资产编号" width="120"></el-table-column>
      <el-table-column prop="name" label="资产名称" width="150"></el-table-column>
      <el-table-column prop="type" label="资产类型" width="120"></el-table-column>
      <el-table-column prop="specifications" label="规格型号" width="150"></el-table-column>
      <el-table-column prop="purchaseDate" label="购入日期" width="120"></el-table-column>
      <el-table-column prop="purchasePrice" label="价格" width="120">
        <template slot-scope="scope">
          {{ scope.row.purchasePrice ? '¥' + scope.row.purchasePrice.toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="department" label="使用部门" width="120"></el-table-column>
      <el-table-column prop="assignee" label="使用人" width="120"></el-table-column>
      <el-table-column prop="location" label="存放位置" width="150"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          <el-button size="mini" type="warning" @click="handleCheck(scope.row)">盘点</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      inventoryItems: [],
      selectedItems: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      types: [],
      searchForm: {
        assetNo: '',
        name: '',
        type: '',
        department: '',
        status: ''
      }
    }
  },
  
  created() {
    this.fetchInventoryItems()
    this.fetchTypes()
  },

  methods: {
    async fetchInventoryItems() {
      try {
        this.loading = true
        const response = await this.$axios.get('/inventory/items', {
          params: {
            page: this.currentPage - 1,
            size: this.pageSize,
            ...this.searchForm
          }
        })
        this.inventoryItems = response.data.content
        this.total = response.data.totalElements
      } catch (error) {
        this.$message.error('获取资产列表失败：' + error.message)
      } finally {
        this.loading = false
      }
    },

    async fetchTypes() {
      try {
        const response = await this.$axios.get('/inventory/types')
        this.types = response.data
      } catch (error) {
        this.$message.error('获取资产类型失败：' + error.message)
      }
    },

    handleSelectionChange(val) {
      this.selectedItems = val
    },

    async handleDeleteAll() {
      try {
        await this.$confirm('确认删除所有资产记录吗？此操作不可恢复', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await this.$axios.post('/inventory/items/deleteAll')
        this.$message.success('删除成功')
        this.fetchInventoryItems()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败：' + error.message)
        }
      }
    },

    handleSearch() {
      this.currentPage = 1
      this.fetchInventoryItems()
    },

    resetSearch() {
      this.searchForm = {
        assetNo: '',
        name: '',
        type: '',
        department: '',
        status: ''
      }
      this.handleSearch()
    },

    handleSizeChange(val) {
      this.pageSize = val
      this.fetchInventoryItems()
    },

    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchInventoryItems()
    },

    getStatusType(status) {
      const statusMap = {
        '在用': 'success',
        '闲置': 'info',
        '维修': 'warning',
        '报废': 'danger'
      }
      return statusMap[status] || ''
    }
  }
}
</script>

<style scoped>
.inventory-list {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 