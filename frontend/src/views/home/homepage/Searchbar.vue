<script lang="ts" setup>
import {ref} from 'vue';
import bannerUrl from '../../../assets/banner.webp';

const url = ref('');
const debounceTimeout = ref<ReturnType<typeof setTimeout> | null>(null);

const query = (searchUrl: string) => {
  console.log('Searching for:', searchUrl);
};

const handleSearch = () => {
  if (debounceTimeout.value) {
    clearTimeout(debounceTimeout.value);
  }
  debounceTimeout.value = setTimeout(() => {
    query(url.value);
  }, 300); // Debounce time of 300ms
};
</script>

<template>
  <div
      :style="{ background: `rgba(0, 0, 0, .25) url(${bannerUrl})` }"
  >
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
</style>