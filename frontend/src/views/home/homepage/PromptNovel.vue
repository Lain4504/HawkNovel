<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { getLatestNovels } from '../../../api/novel';

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
  description: string;
  image: string;
  authorName: string;
  authorId: string;
  categories: Category[];
}

const tabs = [
  { label: 'TOP THÁNG', value: 'top' },
  { label: 'ĐỀ CỬ', value: 'recommentation' },
];

const activeTab = ref(tabs[0].value);
const currentPage = ref(1);
const books = ref<Novel[]>([]);
const itemsPerPage = 6;
const loading = ref(true);

const fetchBooks = async (page: number) => {
  try {
    const response = await getLatestNovels(page, itemsPerPage);
    console.log('Books:', response);
    books.value = response.data.map((book: any) => ({
      id: book.id,
      title: book.title,
      description: book.description,
      image: book.image.path,
      authorName: book.authorName,
      authorId: book.authorId,
      categories: book.categories.map((category: any) => ({
        id: category.id,
        name: category.name,
        description: category.description,
        createdDate: category.createdDate,
        modifiedDate: category.modifiedDate,
      })),
    }));
  } catch (error) {
    console.error('Error fetching books:', error);
  } finally {
    loading.value = false;
  }
};

const setActiveTab = (tab: { label: string; value: string }) => {
  activeTab.value = tab.value;
  currentPage.value = 1;
  fetchBooks(currentPage.value);
};

const changePage = (page: number) => {
  currentPage.value = page;
  fetchBooks(page);
};

const shouldShowSingleDot = computed(() => {
  return currentPage.value === 1 && books.value.length < itemsPerPage;
});

onMounted(() => {
  fetchBooks(currentPage.value);
});

watch(activeTab, () => {
  fetchBooks(currentPage.value);
});
</script>

<template>
  <div class="p-6">
    <ul class="hnt-tab flex justify-start space-x-4 text-start py-4">
      <li
        v-for="tab in tabs"
        :key="tab.value"
        :class="[
          activeTab === tab.value
            ? 'text-[#18A058] font-bold border-b-2 border-[#18A058]'
            : 'text-gray-600',
          'hover:scale-105',
        ]"
        class="item cursor-pointer transition-transform duration-300 ease-in-out"
        @click="setActiveTab(tab)"
      >
        {{ tab.label }}
      </li>
    </ul>

    <a-skeleton :loading="loading" active>
      <template #default>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <a-card
            v-for="book in books"
            :key="book.id"
            class="hover:shadow-md transition-shadow duration-200"
            :bordered="false"
            :bodyStyle="{ padding: '12px' }"
          >
            <div class="flex space-x-4">
              <img
                :src="book.image"
                alt="book cover"
                class="w-24 h-36 object-cover rounded-lg"
                loading="lazy"
              />
              <div class="flex-1">
                <router-link
                  :to="{ name: 'noveldetail', params: { id: book.id } }"
                  class="hover:text-[#18A058]"
                >
                  <Title :level="5" class="!mb-2 line-clamp-2">
                    {{ book.title }}
                  </Title>
                </router-link>
                <a-typography-text class="text-gray-500 line-clamp-3" v-html="book.description" />
                <div class="flex items-center mt-2 italic">
                  <router-link
                    :to="{ name: 'account', params: { id: book.authorId } }"
                    class="text-gray-700"
                  >
                    <a-typography-text>
                      <template #prefix>
                        <i class="fas fa-user mr-2"></i>
                      </template>
                      {{ book.authorName }}
                    </a-typography-text>
                  </router-link>
                </div>
                <div class="mt-3 flex flex-wrap gap-2">
                  <a-tag
                    v-for="category in book.categories.slice(0, 4)"
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

    <div class="flex justify-center mt-4 space-x-2">
      <button
        v-if="!shouldShowSingleDot"
        v-for="page in [1, 2]"
        :key="page"
        :class="[
          'w-3 h-3 rounded-full transition-all',
          currentPage === page ? 'bg-[#18A058]' : 'bg-gray-300',
        ]"
        @click="() => changePage(page)"
      ></button>
      <button
        v-else
        :class="[
          'w-3 h-3 rounded-full transition-all',
          'bg-[#18A058]',
        ]"
      ></button>
    </div>
  </div>
</template>