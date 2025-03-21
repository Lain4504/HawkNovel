<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue';
import {getChaptersByVolumeId} from '../../../api/novelChapter';
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {useRouter} from "vue-router";
import {getVolume} from "../../../api/novelVolume";

const props = defineProps<{
  volumeId: string;
  novelId: string;
}>();
interface Chapter {
  id: string;
  chapterTitle: string;
  chapterNumber: string;
  volumeId: string;
  wordCount: number;
  created: string;
}
const chapters = ref<Chapter[]>([]);
const emit = defineEmits(['close']);
const volumeName = ref('');
const fetchVolume = async () => {
  try {
    const data = await getVolume(props.volumeId);
    volumeName.value = data.volumeName;
  } catch (error) {
    console.error('Failed to fetch chapters:', error);
  }
};
const fetchChapters = async () => {
  try {
    chapters.value = await getChaptersByVolumeId(props.volumeId);
  } catch (error) {
    console.error('Failed to fetch chapters:', error);
  }
};

onMounted(() => {
  fetchChapters();
  fetchVolume();
});
const router = useRouter();
watch(() => props.volumeId, fetchChapters);
const goToChapter = (chapterId: string) => {
  router.push({ name: 'chapter', params: { novel: props.novelId, chapter: chapterId } }).then(() => {
    window.location.reload();
  });
};
const truncatedVolumeName = computed(() => {
  return volumeName.value.length > 30 ? volumeName.value.substring(0, 30) + '...' : volumeName.value;
});
const truncatedChapterName = (chapterTitle: string) => {
  return chapterTitle.length > 40 ? chapterTitle.substring(0, 40) + '...' : chapterTitle;
};
</script>

<template>
  <div class="chapter-list p-4 bg-white rounded-lg shadow-md">
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-xl font-semibold">Danh sách chương</h2>
      <a-button type="text" @click="$emit('close')">
        <font-awesome-icon :icon="['fas', 'times']" class="text-gray-500 hover:text-gray-700 transition-colors"/>
      </a-button>
    </div>
    <hr class="border-t border-gray-200 mb-4"/>
    <div class="flex justify-between items-center mb-4">
  <h3 class="text-lg font-semibold break-words">{{ truncatedVolumeName }}</h3>
</div>
    <ul class="space-y-2">
      <li v-for="chapter in chapters" :key="chapter.id" @click="goToChapter(chapter.id)"
          class="text-sm font-medium hover:underline cursor-pointer text-gray-700 hover:text-[#18A058] transition-colors">
        {{ truncatedChapterName(chapter.chapterTitle) }}
      </li>
    </ul>
  </div>
</template>

<style scoped>
.chapter-list {
  padding: 1rem;
}
</style>