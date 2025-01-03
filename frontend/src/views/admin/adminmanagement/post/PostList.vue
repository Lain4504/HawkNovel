<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {deletePost, getPosts} from "../../../../api/post.ts";
import {useRouter} from "vue-router";
import DynamicDataTable from "../../../../components/admin/DynamicDataTable.vue";
const postColumns = [
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
const currentPage = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const postRows = ref<any[]>([]);
const router = useRouter();
const handleEdit = (row: any) => {
  router.push({name: 'postupdateform', params: {id: row.id}});
};
const viewPost = (row: any) => {
  router.push({name: 'postdetail', params: {id: row.id}});
};
const fetchPosts = async (page: number, size: number) => {
  try {
    const response = await getPosts({page, size});
    postRows.value = response.data;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error("Error fetching posts:", error);
  }
};

onMounted(() => {
  fetchPosts(currentPage.value, pageSize.value);
});


</script>

<template>
  <h3 class="text-2xl font-bold text-left py-2">Quản lý bài viết</h3>
  <DynamicDataTable
      :columns="postColumns"
      :currentPage="currentPage"
      :emits="['page-change', 'view', 'delete', 'edit']"
      :rows="postRows"
      :show-go-to-page="true"
      :totalPages="totalPages"
      :deleteApi="deletePost"
      :fetchApi="fetchPosts"
      @edit="handleEdit"
      @view="viewPost"
  />
</template>