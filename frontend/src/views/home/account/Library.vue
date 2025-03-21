<script lang="ts" setup>
import { onMounted, ref } from "vue";
import { getMyFollowedNovels } from "../../../api/user";
import store from "../../../store";

// Define the data structure for the novels
interface Novel {
  novelId: string;
  novelName: string;
  author: string;
  chapterCount: number | null;
  image: string;
}

const novels = ref<Novel[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const isLoading = ref(true);

const fetchFollowNovels = async (page: number, size: number) => {
  isLoading.value = true;
  try {
    const userId = store.getters.getUserId;
    const result = await getMyFollowedNovels(userId, page, size);
    novels.value = result.data;
    totalElements.value = result.totalElements;
  } catch (error) {
    console.error("Failed to fetch followed novels:", error);
  } finally {
    isLoading.value = false;
  }
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchFollowNovels(page, pageSize.value);
};

onMounted(() => {
  fetchFollowNovels(currentPage.value, pageSize.value);
});

const breadcrumbItems = [
  { title: 'Home', path: '/' },
  { title: 'Account', path: '/templates' },
  { title: 'Library' }
];
</script>

<template>
  <a-layout class="container max-w-7xl mx-auto flex flex-col bg-white">
    <a-layout-content class="flex-1">
      <a-breadcrumb class="mb-6">
        <a-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index">
          <router-link v-if="item.path" :to="item.path">{{ item.title }}</router-link>
          <span v-else>{{ item.title }}</span>
        </a-breadcrumb-item>
      </a-breadcrumb>
      <div class="flex justify-between items-center my-2">
        <h4 class="text-lg font-semibold">TIỂU THUYẾT ĐÁNH DẤU CỦA BẠN</h4>
      </div>
      <a-card class="my-2">
        <div class="mt-4 text-sm">
          <div class="space-y-4">
            <a-skeleton v-if="isLoading" active />
            <a-list v-else :dataSource="novels" itemLayout="horizontal">
              <template #renderItem="{ item }: { item: Novel }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #avatar>
                      <img :src="item.image" alt="Book Image" class="w-14 h-20 object-cover"/>
                    </template>
                    <template #title>
                      <h5 class="font-semibold truncate">{{ item.novelName }}</h5>
                    </template>
                    <template #description>
                      <p class="text-gray-500">{{ item.author }}</p>
                    </template>
                  </a-list-item-meta>
                  <div class="text-gray-500 flex-shrink-0">
                    {{ item.chapterCount }} chương
                  </div>
                </a-list-item>
              </template>
            </a-list>
          </div>
        </div>
        <div class="flex justify-center mt-6">
          <a-pagination
              :current="currentPage"
              :pageSize="pageSize"
              :showTotal="(total: number) => `Total ${total} items`"
              :total="totalElements"
              showQuickJumper
              @change="handlePageChange"
          />
        </div>
      </a-card>
    </a-layout-content>
  </a-layout>
</template>

<style scoped>

</style>