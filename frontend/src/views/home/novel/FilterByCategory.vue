<script lang="ts" setup>
import {ref} from 'vue';
import Banner from "../../../components/home/Banner.vue";

const novels = ref([
  {
    image: 'https://via.placeholder.com/100x150',
    title: 'Chương 05 (P...)',
    description: '"Cậu chẳng thể hôn được đâu"'
  },
  {image: 'https://via.placeholder.com/100x150', title: 'Oneshot', description: '"Don\'t take it wrong!"'},
  // Thêm các mục khác
]);
</script>

<template>
  <div class="max-w-7xl mx-auto">
    <!-- Banner -->
    <Banner/>
    <!-- Content -->
    <main class="container mx-auto px-4 py-6">
      <div class="flex flex-wrap lg:flex-nowrap space-y-6 lg:space-y-0 lg:space-x-6">
        <!-- Main Content -->
        <div class="w-full lg:w-3/4">
          <h2 class="text-xl font-bold mb-4">Giới thiệu</h2>
          <p class="text-gray-700 mb-4">
            Thể loại có các tình tiết gây cười, các xung đột nhẹ nhàng nhưng tạo được tiếng cười nơi độc giả.
          </p>
          <div class="mb-4">
            <select class="border rounded-lg px-3 py-2">
              <option>A - Z</option>
              <option>Popular</option>
            </select>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-6">
            <div
                v-for="(novel, index) in novels"
                :key="index"
                class="flex space-x-4 border p-4 rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            >
              <img
                  :src="novel.image"
                  alt="novel cover"
                  class="w-24 h-36 object-cover rounded-lg max-w-full max-h-full"
              />
              <div class="flex-1">
                <h3 class="text-lg font-semibold line-clamp-2">{{ novel.title }}</h3>
                <p class="text-sm text-gray-500 line-clamp-3">{{ novel.description }}</p>
                <div class="flex items-center text-sm text-gray-700 mt-2">
                  <i class="fas fa-user mr-2"></i>
                  dddd
                </div>
                <span
                    class="inline-block bg-yellow-100 text-yellow-600 text-xs font-semibold mt-3 px-3 py-1 rounded-full"
                >
                </span>
              </div>
            </div>
          </div>
        </div>
        <!-- Sidebar -->
        <aside class="w-full lg:w-1/4 space-y-6 hidden md:block">
          <!-- Filter -->
          <div class="bg-white p-4 rounded-lg shadow">
            <h3 class="font-bold text-gray-800 mb-3">Chữ cái</h3>
            <div class="grid grid-cols-4 gap-2 text-center">
              <a class="text-green-500 hover:underline" href="#">A</a>
              <a class="text-green-500 hover:underline" href="#">B</a>
              <!-- Thêm các chữ cái -->
            </div>
          </div>
          <!-- Category -->
          <div class="bg-white p-4 rounded-lg shadow">
            <h3 class="font-bold text-gray-800 mb-3">Phân loại</h3>
            <label class="block">
              <input class="mr-2" type="checkbox"/> Truyện mới
            </label>
            <label class="block">
              <input class="mr-2" type="checkbox"/> Truyện đang diễn ra
            </label>
            <label class="block">
              <input class="mr-2" type="checkbox"/> Truyện đã hoàn thành
            </label>
            <button class="bg-green-500 text-white px-3 py-2 mt-4 rounded-lg">Áp dụng</button>
          </div>
        </aside>
      </div>
    </main>
  </div>
</template>
