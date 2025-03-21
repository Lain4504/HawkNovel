<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import DynamicDataTable from "../../../../components/admin/DynamicDataTable.vue";
import router from "../../../../router";
import {deleteAds, getAllAds} from "../../../../api/resource.ts";

// Define columns for the data table
const AdsColumns = [
  {field: "id", headerName: "ID", width: 70},
  {field: "title", headerName: "Tiêu đề", width: 200},
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
const adsRows = ref<any[]>([]);
const createPath = '/create-ads';

const handleEdit = (row: any) => {
  router.push({name: 'updateAds', params: {id: row.id}});
};
// Function to fetch post categories
const fetchAds = async (page: number, size: number) => {
  try {
    const response = await getAllAds(page, size);
    adsRows.value = response.data;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error('Error fetching post categories:', error);
  }
};
onMounted(() => {
  fetchAds(currentPage.value, pageSize.value);
});

</script>

<template>
  <h3 class="text-2xl font-bold text-left py-2">Quảng lý quảng cáo</h3>

  <DynamicDataTable
      :columns="AdsColumns"
      :createPath="createPath"
      :currentPage="currentPage"
      :emits="['page-change', 'delete', 'edit']"
      :rows="adsRows"
      :totalPages="totalPages"
      :deleteApi="deleteAds"
      :fetchApi="getAllAds"
      @edit="handleEdit"
  />
</template>