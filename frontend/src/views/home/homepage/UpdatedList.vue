<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { getLatestNovels } from "../../../api/novel";

interface Novel {
  id: number;
  title: string;
  latestChapterTitle: string;
  latestChapterId: string;
  authorId: string;
  authorName: string;
  created: string;
}

const novels = ref<Novel[]>([]);
const loading = ref(true);

const fetchNewUpdateNovels = async () => {
  const page = 1;
  const size = 10;
  try {
    const result = await getLatestNovels(page, size);
    console.log('New update novels:', result.data);
    novels.value = result.data;
  } catch (error) {
    console.error('Error fetching new update novels:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchNewUpdateNovels();
});

const isMobile = ref(false);

onMounted(() => {
  const checkMobile = () => {
    isMobile.value = window.innerWidth < 768;
  };
  checkMobile();
  window.addEventListener('resize', checkMobile);
});
</script>

<template>
  <div class="py-8">
    <a-typography-title level="4" class="mb-6" style="font-size: 20px">CẬP NHẬT MỚI NHẤT</a-typography-title>

    <a-skeleton :loading="loading" active>
      <template #default>
        <a-table v-if="!isMobile && novels.length > 0" :dataSource="novels" :pagination="false" rowKey="id" :showHeader="false">
          <a-table-column title="Title" dataIndex="title" key="title" />
          <a-table-column title="Author" dataIndex="authorName" key="authorName" />
          <a-table-column title="Latest Chapter" dataIndex="latestChapterTitle" key="latestChapterTitle" />
          <a-table-column title="Updated At" dataIndex="created" key="created" />
        </a-table>

        <div v-else-if="novels.length > 0">
          <div v-for="novel in novels" :key="novel.id" class="border-b hover:bg-[#E7F5EE] mb-4 p-4">
            <div class="text-sm font-semibold text-[#18A058] mb-2">
              <router-link :to="{ name: 'noveldetail', params: { id: novel.id } }">{{ novel.title }}</router-link>
            </div>

            <div class="flex justify-between mb-2">
              <div class="text-sm text-gray-700 italic">
                <router-link :to="{ name: 'account', params: { id: novel.authorId } }">{{ novel.authorName }}</router-link>
              </div>
              <div class="text-sm text-gray-700 hover:underline">
                <router-link :to="{ name: 'chapter', params: { novel: novel.id, chapter: novel.latestChapterId } }">
                  {{ novel.latestChapterTitle }}
                </router-link>
              </div>
            </div>

            <div class="flex justify-between">
              <div class="text-sm text-gray-700"></div>
              <div class="text-sm text-gray-500">{{ novel.created }}</div>
            </div>
          </div>
        </div>

        <p v-else class="text-gray-500 text-center">Không có tiểu thuyết nào được cập nhật.</p>
      </template>
    </a-skeleton>
  </div>
</template>

<style scoped>
@media screen and (max-width: 768px) {
  .text-start {
    text-align: left;
  }

  .w-full {
    width: 100%;
  }

  .px-4 {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .py-2 {
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
  }
}
</style>