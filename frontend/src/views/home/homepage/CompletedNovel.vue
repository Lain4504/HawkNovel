<script lang="ts" setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { getNovelsByStatus } from "../../../api/novel";

interface Novel {
  id: number;
  title: string;
  imageUrl: string;
}

const currentPage = ref(1); // Start from page 1
const itemsPerPage = ref(6);
const totalPages = ref(3); // Limit to 3 pages
const novels = ref<Novel[]>([]);
const loading = ref(true);

const getCompleteNovels = async (page: number, size: number) => {
  try {
    const status = 'COMPLETED';
    const response = await getNovelsByStatus(status, page, size);
    console.log("Complete novels:", response);
    novels.value = response.data.map((novel: any) => ({
      id: novel.id,
      title: novel.title,
      imageUrl: novel.image.path,
    }));
    totalPages.value = Math.min(response.totalPages, 3); // Limit to 3 pages
  } catch (error) {
    console.error("Error fetching novels:", error);
  } finally {
    loading.value = false;
  }
};

const paginatedNovels = computed(() => novels.value);

const nextPage = async () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    await getCompleteNovels(currentPage.value, itemsPerPage.value);
  }
};

const prevPage = async () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    await getCompleteNovels(currentPage.value, itemsPerPage.value);
  }
};

onMounted(async () => {
  itemsPerPage.value = getItemsPerPage();
  await getCompleteNovels(currentPage.value, itemsPerPage.value);
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
});

const getItemsPerPage = () => {
  const width = window.innerWidth;
  if (width >= 1024) return 6;
  if (width >= 768) return 4;
  if (width >= 640) return 3;
  return 2;
};

const handleResize = () => {
  const newItemsPerPage = getItemsPerPage();
  if (newItemsPerPage !== itemsPerPage.value) {
    itemsPerPage.value = newItemsPerPage;
    getCompleteNovels(currentPage.value, newItemsPerPage);
  }
};
</script>

<template>
  <div class="w-full max-w-7xl mx-auto px-4 bg-white my-5">
    <div class="flex items-center justify-between mb-6">
      <a-typography-title level={4} style="color: #18A058; font-size: 20px">MỚI HOÀN THÀNH</a-typography-title>
      <div class="flex items-center gap-4">
        <span class="text-sm text-gray-600">{{ currentPage }}/{{ totalPages }}</span>
        <div class="flex gap-2">
          <a-button
            :disabled="currentPage === 1"
            class="text-[#18A058] hover:text-[#18A058] disabled:opacity-50 disabled:cursor-not-allowed w-8 h-8 flex items-center justify-center"
            @click="prevPage"
          >
            «
          </a-button>
          <a-button
            :disabled="currentPage === totalPages"
            class="text-[#18A058] hover:text-[#18A058] disabled:opacity-50 disabled:cursor-not-allowed w-8 h-8 flex items-center justify-center"
            @click="nextPage"
          >
            »
          </a-button>
        </div>
      </div>
    </div>

    <a-skeleton :loading="loading" active>
      <template #default>
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
          <a-card
            v-for="novel in paginatedNovels"
            :key="novel.id"
            class="hover:shadow-md transition-shadow duration-200"
            :bordered="false"
            :bodyStyle="{ padding: '0px' }"
          >
            <div class="relative aspect-[3/4] mb-2 overflow-hidden">
              <img
                :alt="novel.title"
                :src="novel.imageUrl"
                class="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
                loading="lazy"
              />
            </div>
            <a-typography-title level={5} style="font-size: 16px; margin-left: 10px" >
              <router-link :to="{ name: 'noveldetail', params: { id: novel.id } }">
                {{ novel.title }}
              </router-link>
            </a-typography-title>
          </a-card>
        </div>
      </template>
    </a-skeleton>
  </div>
</template>