<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import Comment from '../../../components/home/CommentSection.vue';
import ReviewNovel from '../../../components/home/novel/ReviewNovelForm.vue';
import { getNovel } from '../../../api/novel';
import { getVolumesByNovelId } from '../../../api/novelVolume';
import { getChaptersByVolumeId } from '../../../api/novelChapter';
import { message } from "ant-design-vue";
import {
  createRating,
  followNovel,
  getRatingOfNovel,
  getReviewList,
  hasRated,
  isFollowingNovel,
  unfollowNovel,
  updateRating,
} from "../../../api/user";
import store from "../../../store/index";
import {
  createNovelComment,
  createNovelReply,
  getAllNovelComments,
  getAllNovelRepliesByCommentId
} from "../../../api/novelComment";

const route = useRoute();
const novelId = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id;
const userId = store.getters.getUserId;

const novel = ref<Novel | Record<string, any>>({});
const reviews = ref([]);
const volumes = ref<Volume[]>([]);
const isCollected = ref(false);
const currentTab = ref('Thảo Luận');
const tabs = ['Thảo Luận', 'Đánh Giá'];
const userRating = ref<number | null>(null);
const isAuthenticated = computed(() => store.getters.isAuthenticated);

interface Chapter {
  id: string;
  chapterTitle: string;
  chapterNumber: number;
}

interface Category {
  id: string;
  name: string;
  description: string;
  createdDate: string;
  modifiedDate: string;
}

interface Novel {
  id: string;
  title: string;
  authorId: string;
  authorName: string;
  description: string;
  status: string;
  categories: Category[];
  wordCount: number;
  coverPicture: string;
  image: {
    id: string;
    name: string;
    type: string;
    path: string;
  };
}

interface Volume {
  id: string;
  volumeName: string;
  showMore: boolean;
  expanded: boolean;
  chapters: Chapter[];
  isLoading: boolean;
}

const fetchNovelData = async () => {
  try {
    const data = await getNovel(novelId);
    novel.value = {
      ...data,
      categories: data.categories.map((category: any) => ({
        id: category.id,
        name: category.name,
        description: category.description,
        createdDate: category.createdDate,
        modifiedDate: category.modifiedDate,
      })),
    };
  } catch (error) {
    console.error(error);
  }
};
const comments = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalComments = ref(0);
const fetchComments = async (page: number, size: number) => {
  try {
    const result = await getAllNovelComments(novelId, page, size);
    console.log('Comments:', result);
    comments.value = result.data;
    totalComments.value = result.totalElements;
  } catch (error) {
    console.error('Failed to fetch comments:', error);
  }
};

const fetchReviews = async () => {
  try {
    const result = await getReviewList(novelId, 1, 10);
    reviews.value = result.data;
  } catch (error) {
    console.error('Failed to fetch reviews:', error);
  }
};

const fetchVolumes = async (novelId: string) => {
  try {
    const data = await getVolumesByNovelId(novelId);
    volumes.value = data.map((volume: any) => ({
      ...volume,
      showMore: false,
      expanded: false,
      chapters: [],
      isLoading: false,
    }));
  } catch (error) {
    console.error(error);
  }
};

const fetchChapters = async (volumeIndex: number, volumeId: string) => {
  const status = "COMPLETED";
  volumes.value[volumeIndex].isLoading = true;
  try {
    volumes.value[volumeIndex].chapters = await getChaptersByVolumeId(volumeId, status);
  } catch (error) {
    console.error(error);
  } finally {
    volumes.value[volumeIndex].isLoading = false;
  }
};

const toggleVolume = (index: number, volumeId: string) => {
  volumes.value[index].expanded = !volumes.value[index].expanded;
  if (volumes.value[index].expanded && volumes.value[index].chapters.length === 0) {
    fetchChapters(index, volumeId);
  }
};

const toggleShowMore = (index: number) => {
  volumes.value[index].showMore = !volumes.value[index].showMore;
};

const checkFollowStatus = async () => {
  if (!isAuthenticated.value) return;
  try {
    isCollected.value = await isFollowingNovel({userId, novelId});
  } catch (error) {
    console.error(error);
  }
};

const toggleCollect = async () => {
  if (!isAuthenticated.value) {
    alert('Please log in to follow the novel.');
    return;
  }
  try {
    if (isCollected.value) {
      await unfollowNovel({userId, novelId});
    } else {
      await followNovel({userId, novelId});
    }
    isCollected.value = !isCollected.value;
    showNotification(isCollected.value ? 'Followed successfully!' : 'Unfollowed successfully!');
  } catch (error) {
    console.error(error);
  }
};

const handleReviewCreated = () => {
  fetchReviews();
};

const handleCommentAdded = () => {
  fetchComments(currentPage.value, pageSize.value);
};
const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchComments(page, pageSize.value);
};
const currentTabComponent = computed(() => {
  switch (currentTab.value) {
    case 'Thảo Luận':
      return Comment;
    case 'Đánh Giá':
      return ReviewNovel;
    default:
      return Comment;
  }
});

const rateNovel = async (rating: number) => {
  if (!isAuthenticated.value) {
    message.error('Please log in to rate the novel.');
    return;
  }
  try {
    if (!novelId || !userId) {
      console.error('Novel ID or User ID is missing.');
      return;
    }
    if (userRating.value !== null) {
      await updateRating({userId, novelId, rating});
    } else {
      await createRating({userId, novelId, rating});
    }
    userRating.value = rating;
    showNotification('Rating updated successfully!');
  } catch (error) {
    console.error('Failed to rate novel:', error);
  }
};
const checkUserRating = async () => {
  if (!isAuthenticated.value) return;
  try {
    const response = await hasRated(userId, novelId);
    userRating.value = response ? response : null;
    await fetchUserRating();
  } catch (error) {
    console.error('Failed to check user rating:', error);
  }
};
const fetchUserRating = async () => {
  if (isAuthenticated.value) {
    try {
      const response = await getRatingOfNovel(userId, novelId);
      console.log('User rating:', response);
      userRating.value = response.rating;
    } catch (error) {
      console.error('Failed to fetch user rating:', error);
    }
  }
};
onMounted(() => {
  fetchNovelData();
  fetchComments(currentPage.value, pageSize.value);
  fetchReviews();
  fetchVolumes(novelId);
  checkFollowStatus();
  checkUserRating();
});
const isRatingModalVisible = ref(false);

const toggleRatingModal = () => {
  isRatingModalVisible.value = !isRatingModalVisible.value;
};
const novelUrl = `${window.location.origin}/novel/${novelId}`;
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text);
    showNotification('URL copied to clipboard!');
  } catch (err) {
    console.error('Failed to copy: ', err);
  }
};
const showNotification = (msg: string) => {
  message.success({
    content: msg,
    duration: 3,
  });
};
const handleShareClick = () => {
  copyToClipboard(novelUrl);
};
</script>

<template>
  <div>
    <a-breadcrumb class="mb-6">
      <a-breadcrumb-item v-for="(item, index) in [
        { label: 'Home', to: { name: 'home' } },
        { label: novel.title, to: { name: 'noveldetail', params: { id: novel.id } }, isCurrent: true }
      ]" :key="index">
        <router-link v-if="item.to" :to="item.to">{{ item.label }}</router-link>
        <span v-else>{{ item.label }}</span>
      </a-breadcrumb-item>
    </a-breadcrumb>
  </div>
    <div class="p-4">
      <div class="flex flex-col md:flex-row gap-6">
        <div class="w-48 flex-shrink-0">
          <img
              :src="novel.image?.path"
              alt="Novel Cover"
              class="w-full rounded-lg shadow-md"
              loading="lazy"
          />
        </div>

        <div class="flex-grow">
          <h1 class="text-xl font-bold mb-4">
            {{ novel.title }}
          </h1>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6 text-sm">
            <div class="flex items-start gap-2">
              <span class="text-gray-500">Tác giả:</span>
              <span>{{ novel.authorName }}</span>
            </div>
            <div class="flex items-start gap-2">
              <span class="text-gray-500">Trình trạng:</span>
              <span>{{ novel.status }}</span>
            </div>
            <div class="flex flex-wrap items-start gap-1">
              <span class="text-gray-500 w-full sm:w-auto">Thể loại:</span>
              <a-tag v-for="(category, index) in novel.categories" :key="index" class="mt-1">
                {{ category.name }}
              </a-tag>
            </div>
            <div class="flex items-start gap-2">
              <span class="text-gray-500">Ngày cập nhập mới nhất:</span>
              <span>2024-12-14</span>
            </div>
          </div>

          <div class="flex gap-4 mb-6">
            <button class="flex items-center gap-2 px-4 py-2 rounded-full border border-gray-300 hover:bg-gray-50"
                    id="collect" @click="toggleCollect">
              <span class="text-sm text-gray-600">Yêu thích</span>
              <font-awesome-icon :class="isCollected ? 'text-[#18A058]' : 'text-[#E7F5EE]'" :icon="['fa', 'heart']"
                                 class="text-xl transition-colors duration-200"/>
            </button>
            <button class="flex items-center gap-2 px-4 py-2 rounded-full border border-gray-300 hover:bg-gray-50"
                    @click="toggleRatingModal">
              <span class="text-sm text-gray-600">Đánh giá</span>
              <font-awesome-icon :icon="['fas', 'star']" v-if="userRating !== null" class="text-xl text-yellow-500 transition-colors duration-200"/>
              <font-awesome-icon :icon="['far', 'star']" v-else class="text-xl text-gray-400 transition-colors duration-200"/>
            </button>
            <button @click="handleShareClick"
                    class="flex items-center gap-2 px-4 py-2 rounded-full border border-gray-300 hover:bg-gray-50">
              <span class="text-sm text-gray-600">Chia sẻ</span>
              <font-awesome-icon :icon="['fa', 'share-alt']" class="text-xl"/>
            </button>
          </div>
          <div v-if="isRatingModalVisible"
               class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
            <div class="bg-white p-6 rounded-lg shadow-lg relative">
              <h2 class="text-lg font-semibold mb-4">Đánh giá tiểu thuyết</h2>
              <!-- Rating form content -->
              <div class="flex items-center justify-between space-x-2">
                <button v-for="star in 5" :key="star" @click="rateNovel(star)"
                        :class="{'text-yellow-500': (userRating ?? 0) >= star, 'text-yellow-400 hover:text-yellow-500 transition-colors': (userRating ?? 0) < star}">
                  <font-awesome-icon :icon="['fas', 'star']" v-if="(userRating ?? 0) >= star" class="text-xl"/>
                  <font-awesome-icon :icon="['far', 'star']" v-else class="text-xl"/>
                </button>
              </div>
              <!-- Close button with "X" -->
              <button @click="toggleRatingModal"
                      class="absolute top-2 right-2 text-gray-500 hover:text-black transition">
                <font-awesome-icon :icon="['fa', 'times']"/>
              </button>
            </div>
          </div>

          <p class="text-gray-700 mb-6 leading-relaxed">
            <a class="italic" v-html="novel.description"/>
          </p>
        </div>
      </div>
    </div>
    <div class="w-full mt-10">
      <h2 class="text-xl font-semibold text-gray-800 mb-6">Danh sách chương</h2>
      <div class="space-y-6">
        <div v-for="(volume, volumeIndex) in volumes" :key="volumeIndex"
             class="border border-gray-200 rounded-lg overflow-hidden">
          <button
              class="w-full px-4 py-3 bg-gray-100 flex items-center justify-between hover:bg-gray-200 transition-colors text-[#18A058] font-semibold"
              @click="toggleVolume(volumeIndex, volume.id)">
            {{ volume.volumeName }}
            <font-awesome-icon :icon="volume.expanded ? 'fa-solid fa-sort-up' : 'fa-solid fa-sort-down'"/>
          </button>
          <div v-show="volume.expanded" class="transition-all duration-300 ease-in-out">
            <div class="p-4">
              <a-skeleton :loading="volume.isLoading" active>
                <template #default>
                  <div v-if="volume.chapters.length === 0" class="text-center text-gray-600">
                    Không có chapter
                  </div>
                  <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                    <router-link
                        v-for="(chapter, chapterIndex) in volume.chapters.slice(0, volume.showMore ? undefined : 6)"
                        :key="chapterIndex"
                        :to="{ name: 'chapter', params: { novel: novelId, chapter: chapter.id } }"
                        class="group relative">
                      <a-tooltip :title="chapter.chapterTitle">
                        <div
                            class="p-2 bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow border border-gray-300 hover:border-gray-400">
                       <span class="text-sm text-gray-700 truncate block">
                     {{ chapter.chapterTitle }}
                     </span>
                        </div>
                      </a-tooltip>
                    </router-link>
                  </div>
                  <div v-if="volume.chapters.length > 6" class="mt-4 text-center">
                    <button
                        class="inline-flex items-center justify-center gap-2 px-4 py-2 text-sm text-gray-600 hover:text-gray-800 transition-colors"
                        @click="toggleShowMore(volumeIndex)">
                      <span>{{ volume.showMore ? 'Show Less' : 'Show More' }}</span>
                      <font-awesome-icon :icon="volume.showMore ? 'fa-solid fa-angles-up' : 'fa-solid fa-angles-down'"/>
                    </button>
                  </div>
                </template>
              </a-skeleton>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="container p-4">
      <div class="flex space-x-4 border-b mb-4">
        <button v-for="(tab, index) in tabs" :key="index"
                :class="['px-4 py-2 text-sm font-semibold', currentTab === tab ? 'border-b-2 border-[#18A058] text-[#18A058]' : 'text-gray-600']"
                @click="currentTab = tab">
          {{ tab }}
        </button>
      </div>
      <component :is="currentTabComponent"
                 :comments="comments" :create-comment-api="createNovelComment"
                 :create-reply-api="createNovelReply" :current-page="currentPage"
                 :fetch-comments="fetchComments" :get-all-replies-api="getAllNovelRepliesByCommentId"
                 :item-name="novel.title"
                 :itemId="novel.id"
                 :owner-id="novel.authorId"
                 :page-size="pageSize"
                 :reviews="reviews"
                 :total-comments="totalComments"
                 itemType="novel"
                 @commentAdded="handleCommentAdded"
                 @pageChange="handlePageChange"
                 @reviewCreated="handleReviewCreated"
      />
    </div>
</template>
