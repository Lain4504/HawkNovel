<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import DynamicDataTable from "../../../../components/admin/DynamicDataTable.vue";
import {deletePostCategory, getPostCategories} from "../../../../api/postCategory.ts";
import router from "../../../../router";

// Define columns for the data table
const postCategoryColumns = [
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

// Define reactive variables for pagination
const currentPage = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const postCategoryRows = ref<any[]>([]);
const createPath = '/create-post-category';

const handleEdit = (row: any) => {
  router.push({name: 'updatePostCategory', params: {id: row.id}});
};
// Function to fetch post categories
const fetchPostCategories = async (page: number, size: number) => {
  try {
    const response = await getPostCategories(page, size);
    postCategoryRows.value = response.data;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error('Error fetching post categories:', error);
  }
};
onMounted(() => {
  fetchPostCategories(currentPage.value, pageSize.value);
});

</script>

<template>
  <h3 class="text-2xl font-bold text-left py-2">Quản lý thể loại bài viết</h3>

  <DynamicDataTable
      :columns="postCategoryColumns"
      :createPath="createPath"
      :currentPage="currentPage"
      :emits="['page-change', 'delete', 'edit']"
      :rows="postCategoryRows"
      :totalPages="totalPages"
      :deleteApi="deletePostCategory"
      :fetchApi="fetchPostCategories"
      @edit="handleEdit"
  />
</template>