<script lang="ts" setup>
import {onMounted, onUnmounted, ref} from 'vue';
import { useRouter } from 'vue-router';
import { searchNovels } from '../../../api/resource';
import bannerUrl from '../../../assets/banner.webp';

const url = ref('');
const debounceTimeout = ref<ReturnType<typeof setTimeout> | null>(null);
const router = useRouter();
const dropdownVisible = ref(false);

interface Novel {
  id: string;
  title: string;
  image: string;
  author: string;
}
const searchResults = ref<Novel[]>([]);

const query = async (searchUrl: string) => {
  try {
    const searchRequest = {
      keyword: searchUrl
    };
    const response = await searchNovels(searchRequest);
    searchResults.value = response.map((novel: any) => ({
      id: novel.id,
      title: novel.bookName,
      author: novel.authorName,
      image: novel.image,
    }));
    dropdownVisible.value = searchResults.value.length > 0;
    console.log('Search results:', searchResults.value);
  } catch (error) {
    console.error('Error searching novels:', error);
    dropdownVisible.value = false;
  }
};

const handleSearch = () => {
  if (debounceTimeout.value) {
    clearTimeout(debounceTimeout.value);
  }
  debounceTimeout.value = setTimeout(() => {
    query(url.value);
  }, 300);
};

const goToAdvancedSearch = () => {
  router.push({ name: 'searchadvanced' });
};

// Close dropdown when clicking outside
const handleClickOutside = (event: MouseEvent) => {
  const dropdown = document.querySelector('.search-dropdown');
  if (dropdown && !dropdown.contains(event.target as Node)) {
    dropdownVisible.value = false;
  }
};

// Add event listener when component is mounted
onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

// Remove event listener when component is unmounted
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<template>
  <div :style="{ background: `rgba(0, 0, 0, .25) url(${bannerUrl})` }">
    <div id="banner">
      <a-typography-title
          style="
          text-align: center;
          font-size: 3em;
          color: white;
          filter: drop-shadow(0.05em 0.05em black);
        "
      >
        HawkNovel
      </a-typography-title>
      <div class="search-bar">
        <a-input-search
    v-model:value="url"
    size="large"
    placeholder="Nhập tên tác phẩm..."
    enter-button="Tìm kiếm"
    @search="handleSearch"
    style="width: 100%;"
/>
        <div v-if="dropdownVisible"  class="absolute top-full left-0 w-full z-50 bg-white rounded-lg shadow-md">
          <a-card>
            <div class="flex justify-end">
            <button @click="goToAdvancedSearch">
                Tìm kiếm nâng cao
              </button>
            </div>
            <a-divider />
            <div v-for="novel in searchResults" :key="novel.id" class="search-result-item">
              <img :src="novel.image" :alt="novel.title" class="result-image" />
              <div>
                <router-link :to="{ name: 'noveldetail', params: { id: novel.id } }">
                  <a-typography-text class="block">{{ novel.title }}</a-typography-text>
                </router-link>
                <a-typography-text class="block text-gray-500">{{ novel.author }}</a-typography-text>
              </div>
            </div>
          </a-card>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#banner {
  padding-top: 20px;
  padding-bottom: 50px;
}

.search-bar {
  display: flex;
  justify-content: center;
  max-width: 800px;
  margin: 0 auto;
  position: relative;
}

@media only screen and (max-width: 840px) {
  #banner {
    padding-top: 10px;
    padding-bottom: 35px;
  }

  .search-bar {
    margin: 0 10px;
  }
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-result-item:hover {
  background-color: #f5f5f5;
}

.result-image {
  width: 40px;
  height: 60px;
  object-fit: cover;
  margin-right: 8px;
  border-radius: 2px;
}
</style>