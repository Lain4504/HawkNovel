<script lang="ts" setup>
import {onMounted, ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import {getNovelCategoriesWithoutPagination} from '../../../api/novelCategory';
import {searchNovels} from '../../../api/resource.ts';


const toggleCategory = (category: string) => {
  const index = selectedCategories.value.indexOf(category);
  if (index === -1) {
    selectedCategories.value.push(category);
  } else {
    selectedCategories.value.splice(index, 1);
  }
};
const genres = ref<string[]>([]);
const showAdvancedSearch = ref(false);
const selectedCategories = ref<string[]>([]);
const selectedStatus = ref<string>('all');
const novels = ref<Novel[]>([]);
const loadingNovels = ref(true);
const route = useRoute();
const searchQuery = ref<string>(route.query.q as string || '');

interface Novel {
  id: string;
  title: string;
  description: string;
  image: string;
  authorName: string;
  authorId: string;
  followCount?: number;
  categories: Category[];
}

interface Category {
  id: string;
  name: string;
  description?: string;
  createdDate?: string;
  modifiedDate?: string;
}

const fetchNovels = async () => {
  try {
    loadingNovels.value = true;
    const query = {
      keyword: searchQuery.value,
      categories: selectedCategories.value,
      status: selectedStatus.value,
    };
    console.log('Search query:', query);
    const response = await searchNovels(query);
    console.log('Response:', response);
    novels.value = response.map((novel: any) => ({
      id: novel.id,
      title: novel.bookName,
      description: novel.description,
      image: novel.image,
      authorName: novel.authorName,
      authorId: novel.authorId,
      categories: novel.categoryId.map((id: string, index: number) => ({
        id: id,
        name: novel.categoryName[index],
      })),
    }));
    console.log('Novels:', novels);
  } catch (error) {
    console.error('Error fetching novels:', error);
  } finally {
    loadingNovels.value = false;
  }
};
onMounted(async () => {
  try {
    const response = await getNovelCategoriesWithoutPagination();
    genres.value = response.map((category: { name: string }) => category.name);
    await fetchNovels();
  } catch (error) {
    console.error('Error fetching novel categories:', error);
  }
});

watch(() => route.query.q, async (newQuery) => {
  searchQuery.value = newQuery as string;
  await fetchNovels();
});
</script>

<template>
    <a-card class="rounded-md shadow-md">
      <h1 class="text-lg font-semibold mb-4 text-green-600">Tìm kiếm</h1>
      <div class="mb-4 relative">
        <a-input-search
            v-model:value="searchQuery"
            size="large"
            placeholder="Nhập tên tác phẩm..."
            style="width: 100%;"
            enter-button="Tìm kiếm"
            @search="fetchNovels"
        />
      </div>
      <div v-if="showAdvancedSearch" class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Tình trạng</label>
          <a-select v-model:value="selectedStatus" class="w-full" default-value="all">
            <a-select-option value="all">Tất cả</a-select-option>
            <a-select-option value="ongoing">Đang tiến hành</a-select-option>
            <a-select-option value="completed">Hoàn thành</a-select-option>
          </a-select>
        </div>
      </div>
      <div v-if="showAdvancedSearch">
        <label class="block text-sm font-medium text-gray-700 mb-2">Thể loại</label>
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
          <div v-for="genre in genres" :key="genre">
            <label class="inline-flex items-center">
              <a-checkbox :value="genre" :checked="selectedCategories.includes(genre)" @change="toggleCategory(genre)">
                {{ genre }}
              </a-checkbox>
            </label>
          </div>
        </div>
      </div>
      <div class="mt-6 flex justify-end">
        <a-button type="link" @click="showAdvancedSearch = !showAdvancedSearch">
          <font-awesome-icon :icon="showAdvancedSearch ? 'fas fa-chevron-up' : 'fas fa-chevron-down'"/>
          <span class="ml-1">Tìm kiếm nâng cao</span>
        </a-button>
      </div>
    </a-card>

    <a-skeleton :loading="loadingNovels" active>
      <template #default>
        <a-empty v-if="novels.length === 0" description="Không tìm thấy tiểu thuyết nào"/>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
          <a-card
              v-for="novel in novels"
              :key="novel.id"
              class="hover:shadow-md transition-shadow duration-200"
              :bordered="false"
              :bodyStyle="{ padding: '12px' }"
          >
            <div class="flex space-x-4">
              <img
                  :src="novel.image"
                  :alt="novel.title"
                  class="w-24 h-36 object-cover rounded-lg"
                  loading="lazy"
              />
              <div class="flex-1">
                <router-link
                    :to="{ name: 'noveldetail', params: { id: novel.id } }"
                    class="hover:text-[#18A058]"
                >
                  <a-typography-title :level="5" class="!mb-2 line-clamp-2">
                    {{ novel.title }}
                  </a-typography-title>
                </router-link>
                <a-typography-text class="text-gray-500 line-clamp-3" v-html="novel.description"/>
                <div class="flex items-center mt-2 italic">
                  <router-link
                      :to="{ name: 'account', params: { id: novel.authorId } }"
                      class="text-gray-700"
                  >
                    <a-typography-text>
                      {{ novel.authorName }}
                    </a-typography-text>
                  </router-link>
                </div>
                <div class="mt-3 flex flex-wrap gap-2">
                  <a-tag
                      v-for="category in novel.categories.slice(0, 4)"
                      :key="category.id"
                      class="text-[#18A058] bg-[#E7F5EE] border-0"
                  >
                    {{ category.name }}
                  </a-tag>
                </div>
              </div>
            </div>
          </a-card>
        </div>
      </template>
    </a-skeleton>
</template>
