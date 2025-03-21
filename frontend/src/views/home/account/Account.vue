<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { getUserProfile } from '../../../api/user';
import { getNovelsByAuthorId } from '../../../api/novel';
import { useRoute } from "vue-router";
import store from "../../../store";

const route = useRoute();
const userId = route.params.id as string;
const userProfile = ref({
  id: '',
  image: '',
  username: '',
  gender: '',
  dateOfBirth: '',
  bio: '',
  readNovels: '0',
  marked: '0',
  recommended: '0',
  comments: '0',
  ratings: '0',
  publishedNovels: '0',
  created: ''
});

interface Novel {
  id: string;
  title: string;
  authorName: string;
  description: string;
  image: {
    path: string;
  };
  categories: { name: string }[];
  chapterCount: number;
  created: string;
}

const novels = ref<Novel[]>([]);
const currentPage = ref(1);
const pageSize = 3;
const totalPages = ref(1);
const isLoading = ref(true);
const expandedNovels = ref<{ [key: string]: boolean }>({});

const userStats = computed(() => [
  { key: 'Read', value: `${userProfile.value.readNovels} novels` },
  { key: 'Marked', value: userProfile.value.marked },
  { key: 'Recommended', value: userProfile.value.recommended },
  { key: 'Comments', value: userProfile.value.comments },
  { key: 'Ratings', value: userProfile.value.ratings }
]);

const breadcrumbItems = [
  { title: 'Home', path: '/' },
  { title: 'Account' },
];
const totalElements = ref(0);
const fetchNovelsData = async (page: number, size: number) => {
  isLoading.value = true;
  try {
    const novelsData = await getNovelsByAuthorId(userId, page, size);
    novels.value = novelsData.data;
    totalElements.value = novelsData.totalElements;
    totalPages.value = novelsData.totalPages;
  } catch (error) {
    console.error('Error fetching novels data:', error);
  } finally {
    isLoading.value = false;
  }
};

const fetchUserProfile = async () => {
  isLoading.value = true;
  try {
    const userProfileData = await getUserProfile(userId);
    userProfile.value = {
      ...userProfileData,
      image: userProfileData.image.path
    };
  } catch (error) {
    console.error('Error fetching user profile:', error);
  } finally {
    isLoading.value = false;
  }
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchNovelsData(page, pageSize);
};

const toggleExpanded = (id: string) => {
  expandedNovels.value[id] = !expandedNovels.value[id];
};

const isDescriptionLong = (description: string) => {
  return description.length > 300; // Adjust the length as needed
};

onMounted(() => {
  fetchNovelsData(currentPage.value, pageSize);
  fetchUserProfile();
});
</script>

<template>
  <div class="bg-[#f0f2f5] py-8">
    <div class="max-w-7xl mx-auto px-4">
      <a-breadcrumb class="mb-6">
        <a-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index">
          <router-link v-if="item.path" :to="item.path">{{ item.title }}</router-link>
          <span v-else>{{ item.title }}</span>
        </a-breadcrumb-item>
      </a-breadcrumb>

      <a-row :gutter="24">
        <!-- User Profile Card -->
        <a-col :md="8" :sm="24" :xs="24">
          <a-card :bordered="false" class="shadow-sm">
            <template #title>
              <a-typography-title :level="4">User Information</a-typography-title>
            </template>

            <a-skeleton :loading="isLoading" avatar active>
              <div class="flex items-center mb-6">
                <a-avatar
                    :icon="!userProfile.image"
                    :size="80"
                    :src="userProfile.image"
                    class="mr-4"
                    loading="lazy"
                />
                <div>
                  <a-typography-title :level="4" class="mb-0">{{ userProfile.username }}</a-typography-title>
                  <a-tag class="bg-purple-300">{{store.getters.getUserRole}}</a-tag>
                </div>
              </div>

              <a-descriptions :column="1" layout="vertical">
                <a-descriptions-item label="Date Join">
                  {{ userProfile.created || 'No date available' }}
                </a-descriptions-item>
                <a-descriptions-item label="Bio">
                  <span v-html="userProfile.bio || 'No bio available'"></span>
                </a-descriptions-item>
                <template v-for="stat in userStats" :key="stat.key">
                  <a-descriptions-item :label="stat.key">
                    {{ stat.value }}
                  </a-descriptions-item>
                </template>
              </a-descriptions>
            </a-skeleton>
          </a-card>
        </a-col>

        <!-- Published Novels Card -->
        <a-col :md="16" :sm="24" :xs="24">
          <a-card :bordered="false" class="shadow-sm">
            <template #title>
              <a-typography-title :level="4">Tiểu thuyết đã đăng</a-typography-title>
            </template>

            <a-skeleton :loading="isLoading" active>
              <a-list
                  :data-source="novels"
                  :loading="isLoading"
                  item-layout="vertical"
                  size="middle"
              >
                <template #renderItem="{ item }">
                  <a-list-item>
                    <div class="flex">
                      <img
                          :alt="item.title"
                          :src="item.image.path"
                          class="w-32 h-48 object-cover rounded mr-6"
                          loading="lazy"
                      />
                      <div class="flex-1">
                        <router-link :to="'/novel/' + item.id">
                          <a-typography-title :level="4" class="mb-2 hover:text-[#18A058]">{{ item.title }}</a-typography-title>
                        </router-link>

                        <a-typography-paragraph type="secondary">
                          <strong>Tác giả:</strong> <span class="italic">{{ item.authorName }}</span>
                        </a-typography-paragraph>

                        <a-typography-paragraph type="secondary">
                          <strong class="mr-2">Thể loại:</strong>
                          <template v-for="cat in item.categories" :key="cat.name">
                            <a-tag class="text-[#18A058] font-semibold bg-[#E7F5EE]">{{ cat.name }}</a-tag>
                          </template>
                        </a-typography-paragraph>

                        <a-typography-paragraph class="italic" v-html="expandedNovels[item.id] ? item.description : item.description.slice(0, 300) + '...'" />
                        <div v-if="isDescriptionLong(item.description) && !expandedNovels[item.id]" class="text-right">
                          <a @click="toggleExpanded(item.id)" class="text-[#18A058] cursor-pointer hover:text-[#18A058]">Đọc thêm</a>
                        </div>
                        <div v-if="expandedNovels[item.id]" class="text-right">
                          <a @click="toggleExpanded(item.id)" class="text-[#18A058] cursor-pointer hover:text-[#18A058]">Rút gọn</a>
                        </div>

                        <div class="flex justify-between text-gray-500 text-sm">
                          <span>{{ item.chapterCount }} Chapters</span>
                          <span>Last Updated: {{ item.created }}</span>
                        </div>
                      </div>
                    </div>
                  </a-list-item>
                </template>
              </a-list>
            </a-skeleton>

            <div class="flex justify-center mt-6">
              <a-pagination
                  :current="currentPage"
                  :pageSize="pageSize"
                  :show-total="() => `Total ${totalElements} items`"
                  :total="totalPages * pageSize"
                  show-quick-jumper
                  @change="handlePageChange"
              />
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>