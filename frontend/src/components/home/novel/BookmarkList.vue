<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {getBookmarkByChapter} from "../../../api/user.ts";
import store from "../../../store";

const props = defineProps<{
  chapter: string;
  novel: string;
}>();

interface Bookmark {
  id: string;
  novelId: string;
  novelTitle: string;
  novelChapterId: string;
  novelChapterTitle: string;
  contentNote: string;
}
const userId = store.getters.getUserId;
const bookmarks = ref<Bookmark[]>([]);
const emit = defineEmits(['close']);
const fetchBookmarkByChapterId = async () => {
  try {
    const response = await getBookmarkByChapter(userId, props.chapter, props.novel);
    bookmarks.value = response; // Set the bookmarks ref with the response data
    console.log('response:', response);
  } catch (error) {
    console.error('Failed to fetch chapters:', error);
  }
};

onMounted(() => {
  fetchBookmarkByChapterId();
});
const truncatedContent = (contentNote: string) => {
  return contentNote.length > 300 ? contentNote.substring(0, 300) + '...' : contentNote;
};
</script>

<template>
  <div class="p-4 bg-white rounded-lg shadow-md w-full md:w-1/2">
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-xl font-semibold">Danh sách bookmark</h2>
      <a-button type="text" @click="$emit('close')">
        <font-awesome-icon :icon="['fas', 'times']" class="text-gray-500 hover:text-gray-700 transition-colors"/>
      </a-button>
    </div>
    <hr class="border-t border-gray-200 mb-4"/>
    <ul class="space-y-2">
      <li v-for="bookmark in bookmarks" :key="bookmark.id"
          class="text-sm font-medium text-gray-700 transition-colors">
        <div>{{ bookmark.novelTitle }} - {{ truncatedContent(bookmark.novelChapterTitle) }}</div>
        <p class="italic">{{ truncatedContent(bookmark.contentNote) }}</p>
      </li>
    </ul>
  </div>
</template>