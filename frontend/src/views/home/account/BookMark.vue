<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { getBookmark } from "../../../api/user";
import store from "../../../store";

interface Novel {
  id: string;
  novelId: string;
  novelTitle: string;
  novelChapterId: string;
  novelChapterTitle: string;
  created: string;
  contentNote: string;
}

const currentPage = ref(1);
const totalPages = ref(0);
const pageSize = 10;
const novel = ref<Novel[]>([]);
const isLoading = ref(true);

const fetchNovel = async (page: number) => {
  isLoading.value = true;
  try {
    const response = await getBookmark(store.getters.getUserId, page, pageSize);
    novel.value = response.data.map((novel: any) => ({
      id: novel.id,
      novelId: novel.novelId,
      novelTitle: novel.novelTitle,
      novelChapterId: novel.novelChapterId,
      novelChapterTitle: novel.novelChapterTitle,
      created: novel.created,
      contentNote: novel.contentNote,
    }));
    currentPage.value = response.currentPage;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error("Error fetching novels:", error);
  } finally {
    isLoading.value = false;
  }
};

const handlePageChange = (page: number) => {
  fetchNovel(page);
};

onMounted(() => {
  fetchNovel(currentPage.value);
});

const truncatedContentNote = (contentNote: string) => {
  return contentNote.length > 500 ? contentNote.substring(0, 500) + '...' : contentNote;
};
</script>

<template>
  <a-layout class="max-w-7xl mx-auto">
    <a-layout-content>
      <section class="p-4 border rounded bg-white">
        <div class="mt-4 text-sm">
          <div class="space-y-4">
            <a-skeleton v-if="isLoading" active />
            <a-card
                v-for="(item, index) in novel"
                :key="index"
                v-else
                class="hover:shadow-md transition-shadow duration-200"
                :bordered="false"
                :bodyStyle="{ padding: '12px' }"
            >
              <div class="flex-1">
                <router-link
                    :to="{ name: 'noveldetail', params: { id: item.novelId } }"
                    class="hover:text-[#18A058]"
                >
                  <a-typography-title :level="5" class="!mb-2 line-clamp-2">
                    {{ item.novelTitle }}
                  </a-typography-title>
                </router-link>
                <a-typography-text class="text-gray-500 line-clamp-3">
                  <router-link :to="{ name: 'chapter', params: { novel: item.novelId, chapter: item.novelChapterId } }">
                    {{ item.novelChapterTitle }}
                  </router-link>
                </a-typography-text>
                <a-typography-text class="text-gray-500 line-clamp-3 italic bg-gray-50 p-2">
                  "{{ truncatedContentNote(item.contentNote) }}"
                </a-typography-text>
              </div>
            </a-card>
          </div>
        </div>
        <div class="flex justify-center mt-4">
          <a-pagination
              :current="currentPage"
              :pageSize="pageSize"
              :total="totalPages * pageSize"
              @change="handlePageChange"
          />
        </div>
      </section>
    </a-layout-content>
  </a-layout>
</template>