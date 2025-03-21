<script lang="ts" setup>
import {onMounted, ref, watch} from "vue";
import {getPosts} from "../../../api/post";
import {getPostCategoriesWithoutPagination} from "../../../api/postCategory";

// Logic for post list
const currentPage = ref(1);
const totalPages = ref(0);
const data = ref<any[]>([]);
const pageSize = 10;

const fetchPosts = async (page: number, categoryId: string) => {
  try {
    const result = await getPosts({page, size: pageSize, categoryId: categoryId || undefined});
    data.value = result.data;
    totalPages.value = result.totalPages;
    currentPage.value = result.currentPage;
  } catch (error) {
    console.error("Failed to fetch posts:", error);
  }
};

const selectedCategory = ref<string>("");
onMounted(() => {
  fetchPosts(currentPage.value, selectedCategory.value);
});
watch(selectedCategory, (newCategoryId) => {
  currentPage.value = 1;
  fetchPosts(currentPage.value, newCategoryId);
});

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    fetchPosts(page, selectedCategory.value);
  }
};

const categories = ref<any[]>([]);
const fetchCategories = async () => {
  try {
    categories.value = await getPostCategoriesWithoutPagination();
  } catch (error) {
    console.error("Failed to fetch post categories:", error);
  }
};

onMounted(() => {
  fetchCategories();
});
</script>
<template>
  <main class="max-w-7xl mx-auto bg-white">
    <section class="p-4">
      <header class="mb-6">
        <div class="flex justify-between items-center mb-2">
          <h1 class="text-xl font-semibold text-gray-800">Thảo luận</h1>
          <a-button type="default" class="flex items-center">
            <font-awesome-icon :icon="['fas', 'plus']" class="text-lg mr-2"/>
            <router-link :to="{ name: 'postcreateform' }">
              <span>Đăng bài</span>
            </router-link>
          </a-button>
        </div>
        <a-space>
          <a-select v-model:value="selectedCategory" placeholder="Tất cả" ref="select"
                    style="width: 240px"
          >
            <a-select-option value="">Tất cả chuyên mục</a-select-option>
            <a-select-option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}</a-select-option>
          </a-select>
        </a-space>
      </header>
      <div class="overflow-auto" style="max-height: 500px;">
        <a-table :data-source="data" :pagination="false" row-key="id">
          <a-table-column title="Chủ đề" data-index="title" key="title">
            <template #default="{ record }">
              <router-link
                  :to="{ name: 'postdetail', params: { id: record.id } }"
                  class="text-black font-medium hover:underline"
              >
                <i v-if="record.isImportant" class="fas fa-star text-yellow-400 mr-2"></i>
                {{ record.title }}
              </router-link>
            </template>
          </a-table-column>
          <a-table-column title="Chuyên mục" data-index="categoryName" key="categoryName"/>
          <a-table-column title="Bình luận" data-index="comments" key="comments"/>
          <a-table-column title="Lượt xem" data-index="views" key="views"/>
          <a-table-column title="Gần nhất" data-index="created" key="created"/>
          <a-table-column title="Người đăng cuối" data-index="userId" key="userId"/>
        </a-table>
      </div>
      <div class="flex justify-center mt-4">
        <a-pagination
            :current="currentPage"
            :pageSize="pageSize"
            :total="totalPages * pageSize"
            @change="goToPage"
        />
      </div>
    </section>
  </main>
</template>
