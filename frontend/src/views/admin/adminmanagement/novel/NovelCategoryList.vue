<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {deleteNovelCategory, getNovelCategories} from "../../../../api/novelCategory.ts";
import router from "../../../../router";
import DynamicDataTable from "../../../../components/admin/DynamicDataTable.vue";

const novelCategoryColumns = [
  {field: "id", headerName: "ID", width: 70},
  {field: "name", headerName: "Tiêu đề", width: 200},
  {field: "created", headerName: "Thời gian tạo", width: 200},
  {
    field: "actions",
    headerName: "Hành động",
    width: 200,
    isAction: true,
  },
];

const totalPages = ref(1);
const pageSize = ref(10);
const currentPage = ref(1);
const novelCategoryRows = ref<any[]>([]);
const createPath = '/create-novel-category';

const handleEdit = (row: any) => {
  router.push({name: 'updateNovelCategory', params: {id: row.id}});
};

const fetchNovelCategories = async (page: number, size: number) => {
  try {
    const response = await getNovelCategories(page, size);
    novelCategoryRows.value = response.data;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error('Error fetching novel categories:', error);
  }
};

onMounted(() => {
  fetchNovelCategories(currentPage.value, pageSize.value);
});
</script>

<template>
  <h3 class="text-2xl font-bold text-left py-2">Quản lý thể loại tiểu thuyết</h3>
  <DynamicDataTable
      :columns="novelCategoryColumns"
      :createPath="createPath"
      :currentPage="currentPage"
      :emits="['page-change', 'delete', 'edit']"
      :rows="novelCategoryRows"
      :totalPages="totalPages"
      :deleteApi="deleteNovelCategory"
      :fetchApi="fetchNovelCategories"
      @edit="handleEdit"
  />
</template>