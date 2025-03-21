<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { getLatestReview } from "../../../api/user";

interface Review {
  id: string;
  userId: string;
  novelId: string;
  novelName: string;
  review: string;
  created: string;
  userName: string;
  image: string;
}

const reviews = ref<Review[]>([]);
const expandedReviews = ref<string[]>([]);
const loading = ref(true);

const getLatestReviews = async () => {
  try {
    const response = await getLatestReview(1, 8);
    reviews.value = response.data;
  } catch (error) {
    console.error('Failed to fetch latest reviews:', error);
  } finally {
    loading.value = false;
  }
};

const toggleReview = (id: string) => {
  if (expandedReviews.value.includes(id)) {
    expandedReviews.value = expandedReviews.value.filter(reviewId => reviewId !== id);
  } else {
    expandedReviews.value.push(id);
  }
};

onMounted(() => {
  getLatestReviews();
});
</script>

<template>
  <div class="max-w-7xl mx-auto p-4">
    <a-typography-title level={2} style="font-size: 24px">Đánh giá mới nhất</a-typography-title>

    <a-skeleton :loading="loading" active>
      <template #default>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <a-card
            v-for="review in reviews"
            :key="review.id"
            class="hover:shadow-lg transition-shadow duration-200"
            :bordered="false"
            :bodyStyle="{ padding: '12px' }"
          >
            <a-typography-title level={4} style="font-size: 20px; color: #18A058">
              <router-link :to="{ name: 'noveldetail', params: { id: review.novelId } }">
                {{ review.novelName }}
              </router-link>
            </a-typography-title>
            <div :class="{'max-h-48': !expandedReviews.includes(review.id)}" class="overflow-hidden flex-grow">
              <a-typography-text class="text-gray-600 italic mb-4 break-words">{{ review.review }}</a-typography-text>
            </div>
            <a-button type="link" v-if="review.review.length > 200" @click="toggleReview(review.id)" class="text-[#18A058] p-0">
              {{ expandedReviews.includes(review.id) ? 'Read Less' : 'Read More' }}
            </a-button>
            <div class="mt-auto flex items-center justify-between">
              <div class="flex items-center space-x-2">
                <a-avatar :src="review.image" size="large" alt="User Avatar" class="w-8 h-8 rounded-full"/>
                <div class="font-medium">{{ review.userName }}</div>
              </div>
              <div class="text-sm text-gray-500">{{ review.created }}</div>
            </div>
          </a-card>
        </div>
      </template>
    </a-skeleton>
  </div>
</template>

<style scoped>
.max-h-48 {
  max-height: 12rem;
}
</style>