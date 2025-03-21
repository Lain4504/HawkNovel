<script lang="ts" setup>
import {ref} from 'vue';
const genres = ref([
  "Action",
  "Adapted to Anime",
  "Adapted to Drama",
  "Adapted to Manga",
  "Adult",
  "Adventure",
  "Age Gap",
  "Boys Love",
  "Character Growth",
  "Chinese Novel",
  "Comedy",
  "Cooking",
  "Different Social Stat",
  "Drama",
  "Ecchi",
  "English Novel",
  "Fantasy",
  "Female Protagonist",
  "Game",
  "Gender Bender",
  "Harem",
  "Historical",
  "Horror",
  "Incest",
  "Isekai",
  "Josei",
  "Korean Novel",
  "Magic",
  "Martial Arts",
  "Mature",
  "Mecha",
  "Military",
]);

const showAdvancedSearch = ref(false);
</script>

<template>
  <div class="p-6 max-w-7xl mx-auto">
    <div class="bg-white p-6 rounded-md shadow-md">
      <h1 class="text-lg font-semibold mb-4 text-green-600">Tìm kiếm</h1>
      <div class="mb-4 relative">
        <input
            class="w-full border border-gray-300 rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-green-500"
            placeholder="Nhập từ khóa..."
            type="text"
        />
        <button
            class="absolute right-2 top-2 bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-400"
        >
          Tìm kiếm
        </button>
      </div>
      <div v-if="showAdvancedSearch" class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Tác giả</label>
          <input
              class="w-full border border-gray-300 rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-green-500"
              placeholder="Có thể bỏ trống..."
              type="text"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Họa sĩ</label>
          <input
              class="w-full border border-gray-300 rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-green-500"
              placeholder="Có thể bỏ trống..."
              type="text"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Tình trạng</label>
          <select
              class="w-full border border-gray-300 rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-green-500"
          >
            <option value="all">Tất cả</option>
            <option value="ongoing">Đang tiến hành</option>
            <option value="completed">Hoàn thành</option>
          </select>
        </div>
      </div>
      <div v-if="showAdvancedSearch">
        <label class="block text-sm font-medium text-gray-700 mb-2">Thể loại</label>
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
          <div v-for="genre in genres" :key="genre">
            <label class="inline-flex items-center">
              <input
                  class="form-checkbox h-4 w-4 text-green-600 border-gray-300 rounded focus:ring-0"
                  type="checkbox"
              />
              <span class="ml-2 text-sm text-gray-700">{{ genre }}</span>
            </label>
          </div>
        </div>
      </div>
      <div class="mt-6 flex justify-end">
        <button class="flex items-center text-gray-500 hover:text-gray-700"
                @click="showAdvancedSearch = !showAdvancedSearch">
          <svg
              class="h-5 w-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
          >
            <path
                d="M3 10h2M5 6h14M5 6h14M5 6v2M3 14h14M21 10h-2M5 10h14M5 10h14M5 10v2M5 14h14M21 14h-2"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
            />
          </svg>
          <span class="ml-1">Tìm kiếm nâng cao</span>
        </button>
      </div>
    </div>
  </div>
</template>