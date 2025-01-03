<script lang="ts" setup>
import {computed, defineEmits, defineProps, onMounted, onUnmounted, ref} from 'vue';
import {addBookmark} from '../../../api/user';
import store from "../../../store";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import router from "../../../router";
import ChapterList from './ChapterList.vue';
import {message} from "ant-design-vue";
import BookmarkList from "./BookmarkList.vue";

const props = defineProps<{
  chapter: {
    id: string;
    title: string;
    content: string;
    created: string;
    chapterNumber: string;
    volumeId: string;
    wordCount: number;
  };
  novel: {
    id: string;
    authorName: string;
    title: string;
  };
}>();

const emits = defineEmits(['previous-chapter', 'next-chapter']);
const bookmarkPosition = ref({top: 0, left: 0});
const bookmarkVisible = ref(false);
const selectedText = ref('');
const isAuthenticated = computed(() => store.getters.isAuthenticated);
const showChapterList = ref(false);
const showBookmarkList = ref(false);

const handleContentClick = (event: MouseEvent | TouchEvent) => {
  if (!isAuthenticated.value) return;
  const target = event.target as HTMLElement;
  if (target.tagName === 'P') {
    const rect = target.getBoundingClientRect();
    bookmarkPosition.value = {top: rect.top + window.scrollY, left: rect.right + window.scrollX};
    bookmarkVisible.value = true;
    selectedText.value = target.textContent || '';
  } else {
    bookmarkVisible.value = false;
    selectedText.value = '';
  }
};

const handleAddBookmark = async () => {
  if (!isAuthenticated.value) return;
  try {
    const data = {
      userId: store.getters.getUserId,
      novelId: props.novel.id,
      novelTitle: props.novel.title,
      novelChapterId: props.chapter.id,
      novelChapterTitle: props.chapter.title,
      contentNote: selectedText.value,
    };
    console.log('Bookmark added successfully', data);
    await addBookmark(store.getters.getUserId, data);
    bookmarkVisible.value = false;
    message.success('Bookmark added successfully');
  } catch (error) {
    console.error('Failed to add bookmark:', error);
  }
};

const handleResize = () => {
  bookmarkVisible.value = false;
};

const toggleChapterList = () => {
  showChapterList.value = !showChapterList.value;
  if (showChapterList.value) {
    showBookmarkList.value = false;
  }
};

const toggleBookmarkList = () => {
  showBookmarkList.value = !showBookmarkList.value;
  if (showBookmarkList.value) {
    showChapterList.value = false;
  }
};

onMounted(() => {
  const contentElement = document.querySelector('.content');
  if (contentElement) {
    contentElement.addEventListener('click', (event: Event) => handleContentClick(event as MouseEvent));
  }
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

const goBackNovel = () => {
  router.push({name: 'noveldetail', params: {id: props.novel.id}});
};
</script>

<template>
  <div class="text-gray-800 relative content">
    <div v-if="bookmarkVisible && isAuthenticated"
         :style="{ top: `${bookmarkPosition.top}px`, left: `${bookmarkPosition.left}px` }"
         class="absolute z-50 bookmark-button">
      <button
          class="text-black"
          @click.stop="handleAddBookmark"
      >
        <font-awesome-icon :icon="['fas', 'bookmark']" size="lg"/>
      </button>
    </div>
    <main class="container px-4 py-6 max-w-6xl mx-auto">
      <div class="text-center mb-6">
        <h2 class="text-2xl font-semibold mb-2">
          {{ chapter.title }}
        </h2>
        <div class="flex items-center space-x-2 justify-center">
          <p class="text-gray-600 text-sm font-bold">{{ novel.title }}</p>
          <p class="text-gray-600 text-sm">-</p>
          <p class="text-gray-600 text-sm font-light italic">{{ novel.authorName }}</p>
        </div>
        <div class="flex justify-center space-x-4">
          <p class="text-gray-600 text-sm">Word: {{ chapter.wordCount }}</p>
          <p class="text-gray-600 text-sm">Created: {{ chapter.created }}</p>
        </div>
      </div>
      <div class="prose prose-blue mx-auto max-w-none" v-html="chapter.content"></div>
      <div class="flex justify-between mt-8">
        <a-button type="primary" @click="$emit('previous-chapter')">Previous Chapter</a-button>
        <a-button type="primary" @click="$emit('next-chapter')">Next Chapter</a-button>
      </div>
    </main>
    <div class="sidebar fixed right-4 top-1/2 -translate-y-1/2 flex flex-col gap-1 z-10">
      <nav class="bg-white/90 rounded-lg shadow-lg p-2 flex flex-col gap-4 border">
        <button class="p-2 hover:bg-[#E7F5EE] rounded transition-colors" @click="$emit('next-chapter')">
          <font-awesome-icon :icon="['fas', 'angles-right']" class="text-gray-600"/>
        </button>

        <button class="p-2 hover:bg-[#E7F5EE] rounded transition-colors" @click="goBackNovel">
          <font-awesome-icon :icon="['fas', 'home']" class="text-gray-600"/>
        </button>

        <button class="p-2 hover:bg-[#E7F5EE] rounded transition-colors">
          <font-awesome-icon :icon="['fas', 'font']"/>
        </button>

        <button class="p-2 hover:bg-gray-100 rounded transition-colors"  @click="toggleChapterList">
          <font-awesome-icon :icon="['fas', 'list']" class="text-gray-600"/>
        </button>

        <button class="p-2 hover:bg-[#E7F5EE]  rounded transition-colors" @click="toggleBookmarkList">
          <font-awesome-icon :icon="['fas', 'bookmark']" class="text-gray-600"/>
        </button>

        <button class="p-2 hover:bg-[#E7F5EE] rounded transition-colors" @click="$emit('previous-chapter')">
          <font-awesome-icon :icon="['fas', 'angles-left']" class="text-gray-600"/>
        </button>
      </nav>
    </div>
    <div class="mobile-menu fixed bottom-0 left-0 right-0 bg-white border-t border-gray-300 px-4 py-2 z-10">
      <div class="max-w-screen-sm mx-auto flex justify-between items-center">
        <button class="p-2 hover:bg-[#E7F5EE] rounded-full transition-colors" @click="$emit('next-chapter')">
          <font-awesome-icon :icon="['fas', 'angles-left']" class="w-5 h-5 text-gray-600"/>
        </button>
        <button class="p-2 hover:bg-[#E7F5EE] rounded-full transition-colors" @click="goBackNovel">
          <font-awesome-icon :icon="['fas', 'home']" class="w-5 h-5 text-gray-600"/>
        </button>
        <button class="p-2 hover:bg-[#E7F5EE] rounded-full transition-colors">
          <font-awesome-icon :icon="['fas', 'font']" class="w-5 h-5 text-gray-600"/>
        </button>

        <button class="p-2 hover:bg-[#E7F5EE] rounded-full transition-colors"  @click="toggleChapterList" >
          <font-awesome-icon :icon="['fas', 'list']" class="w-5 h-5 text-gray-600"/>
        </button>

        <button class="p-2 hover:bg-[#E7F5EE] rounded-full transition-colors" @click="toggleBookmarkList">
          <font-awesome-icon :icon="['fas', 'bookmark']" class="w-5 h-5 text-gray-600"/>
        </button>

        <button class="p-2 hover:bg-[#E7F5EE] rounded-full transition-colors" @click="$emit('previous-chapter')">
          <font-awesome-icon :icon="['fas', 'angles-right']" class="w-5 h-5 text-gray-600"/>
        </button>
      </div>
    </div>
    <ChapterList v-if="showChapterList" :volumeId="props.chapter.volumeId" :novelId="props.novel.id" @close="toggleChapterList" class="fixed left-0 top-0 h-full bg-white shadow-lg z-50"/>
    <BookmarkList v-if="showBookmarkList" :novel="props.novel.id" :chapter="props.chapter.id" @close="toggleBookmarkList" class="fixed left-0 top-0 h-full bg-white shadow-lg z-50"/>
  </div>
</template>


<style scoped>
.bookmark-button {
  transform: translate(-250%, -300%);
  transition: transform 0.3s ease-in-out;
}

@media (max-width: 1024px) {
  .bookmark-button {
    transform: translate(-50%, -275%);
  }
}

@media (max-width: 768px) {
  .bookmark-button {
    transform: translate(-50%, -250%);
  }
}

@media (max-width: 480px) {
  .bookmark-button {
    transform: translate(-50%, -225%);
  }
}

.sidebar {
  display: block;
}

.mobile-menu {
  display: none;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }

  .mobile-menu {
    display: block;
  }
}
</style>