<script lang="ts" setup>
import {computed, onBeforeUnmount, onMounted, reactive, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {getChapter, getNextChapter, getPreviousChapter, incrementChapterView} from '../../../api/novelChapter';
import ChapterContent from '../../../components/home/novel/ChapterContent.vue';
import {
  createChapterComment,
  createChapterReply,
  getAllChapterComments,
  getAllRepliesByChapterCommentId
} from "../../../api/novelComment";
import CommentSection from "../../../components/home/CommentSection.vue";
import {getNovel} from "../../../api/novel";
import {createReadingHistory} from "../../../api/user";
import store from "../../../store";
const isAuthenticated = computed(() => store.getters.isAuthenticated); // Use the isAuthenticated computed property
const route = useRoute();
const router = useRouter();
const chapterId = ref(route.params.chapter as string);
const novelId = route.params.novel as string;
const chapter = reactive({
  id: '',
  title: '',
  content: '',
  created: '',
  chapterNumber: '',
  volumeId: '',
  userId: '',
  wordCount: 0,
});
const novel = reactive({
  id: '',
  authorName: '',
  title: '',
});
let timeoutId: number | null = null;

const fetchNovel = async (id: string) => {
  try {
    const response = await getNovel(id);
    console.log('Novel:', response);
    novel.id = response.id;
    novel.authorName = response.authorName;
    novel.title = response.title;
  } catch (error) {
    console.error('Failed to fetch novel:', error);
  }
};

const fetchChapter = async (id: string) => {
  try {
    const response = await getChapter(id);
    console.log('Chapter:', response);
    chapter.id = response.id;
    chapter.title = response.chapterTitle;
    chapter.content = response.content;
    chapter.created = response.created;
    chapter.chapterNumber = response.chapterNumber;
    chapter.volumeId = response.volumeId;
    chapter.userId = response.authorId;
    chapter.wordCount = response.wordCount;
    startIncrementViewTimer(id);
  } catch (error) {
    console.error('Failed to fetch chapter:', error);
  }
};

const startIncrementViewTimer = (id: string) => {
  if (timeoutId) {
    clearTimeout(timeoutId);
  }
  timeoutId = window.setTimeout(() => {
    incrementChapterView(id);
  }, 90000); // 1.5 minutes
};

const fetchPreviousChapter = async () => {
  try {
    const response = await getPreviousChapter(chapter.volumeId, parseInt(chapter.chapterNumber));
    await fetchChapter(response.id);
    chapterId.value = response.id;
    await router.push({name: 'chapter', params: {id: response.id}});
  } catch (error) {
    console.error('Failed to fetch previous chapter:', error);
  }
};

const fetchNextChapter = async () => {
  try {
    const response = await getNextChapter(chapter.volumeId, parseInt(chapter.chapterNumber));
    await fetchChapter(response.id);
    chapterId.value = response.id;
    await router.push({name: 'chapter', params: {id: response.id}});
  } catch (error) {
    console.error('Failed to fetch next chapter:', error);
  }
};

const trackReadingHistory = async () => {
  if (!isAuthenticated.value) return;
  try {
    const data = {
      userId: chapter.userId,
      novelId: novel.id,
      novelTitle: novel.title,
      novelChapterId: chapter.id,
      novelChapterTitle: chapter.title,
    };
    await createReadingHistory(data);
  } catch (error) {
    console.error('Failed to track reading history:', error);
  }
};

const comments = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalComments = ref(0);
const fetchComments = async (page: number, size: number) => {
  try {
    const result = await getAllChapterComments(chapterId.value, page, size);
    console.log('Comments:', result);
    comments.value = result.data;
    totalComments.value = result.totalElements; // Update to use totalElements from the API response
  } catch (error) {
    console.error('Failed to fetch comments:', error);
  }
};
const handleCommentAdded = () => {
  fetchComments(currentPage.value, pageSize.value);
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchComments(page, pageSize.value);
};

onMounted(() => {
  fetchNovel(novelId);
  fetchChapter(chapterId.value);
  fetchComments(currentPage.value, pageSize.value);
  if (isAuthenticated.value) {
    window.addEventListener('beforeunload', trackReadingHistory);
  }
});

onBeforeUnmount(() => {
  if (timeoutId) {
    clearTimeout(timeoutId);
  }
  if (isAuthenticated.value) {
    trackReadingHistory();
    window.removeEventListener('beforeunload', trackReadingHistory);
  }
});

watch(chapterId, () => {
  fetchComments(currentPage.value, pageSize.value);
});
</script>

<template>
  <ChapterContent :chapter="chapter" :novel="novel" @previous-chapter="fetchPreviousChapter"
                  @next-chapter="fetchNextChapter"/>
  <CommentSection :comments="comments" :create-comment-api="createChapterComment"
                  :create-reply-api="createChapterReply" :current-page="currentPage"
                  :fetch-comments="fetchComments"
                  :get-all-replies-api="getAllRepliesByChapterCommentId"
                  :item-name="chapter.title"
                  :itemId="chapterId"
                  :owner-id="chapter.userId"
                  :page-size="pageSize"
                  :total-comments="totalComments"
                  itemType="chapter"
                  @commentAdded="handleCommentAdded"
                  @pageChange="handlePageChange"
  />
</template>