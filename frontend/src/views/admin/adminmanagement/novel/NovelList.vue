<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {deleteNovel, getNovels} from "../../../../api/novel.ts";
import router from "../../../../router";
import DynamicDataTable from "../../../../components/admin/DynamicDataTable.vue";

const novelColumns = [
  {field: "id", headerName: "ID", width: 70},
  {field: "title", headerName: "Tiêu đề", width: 200},
  {field: "authorName", headerName: "Tác giả", width: 200},
  {field: "created", headerName: "Thời gian tạo", width: 200},
  {field: "actions", headerName: "Hành động", width: 200, isAction: true}
];

const totalPages = ref(1);
const pageSize = ref(10);
const currentPage = ref(1);
const novelRows = ref<any[]>([]);

const handleEdit = (row: any) => {
  router.push({name: 'novelAuthorManagement', params: {id: row.id}});
};

const handleView = (row: any) => {
  router.push({name: 'noveldetail', params: {id: row.id}});
};
const fetchNovels = async (page: number, size: number) => {
  try {
    const response = await getNovels(page, size);
    novelRows.value = response.data;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error('Error fetching novels:', error);
  }
};

onMounted(() => {
  fetchNovels(currentPage.value, pageSize.value);
});

</script>

<template>
  <h3 class="text-2xl font-bold text-left py-2">Quảng lý tiểu thuyết</h3>
  <DynamicDataTable
      :columns="novelColumns"
      :currentPage="currentPage"
      :emits="['page-change', 'delete', 'view', 'edit']"
      :rows="novelRows"
      :totalPages="totalPages"
      :deleteApi="deleteNovel"
      :fetchApi="fetchNovels"
      @edit="handleEdit"
      @view="handleView"
  />
</template>