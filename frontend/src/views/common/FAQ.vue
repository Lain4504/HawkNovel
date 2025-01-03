<script lang="ts" setup>

// Fake data for the FAQ with multiple questions per section
const faqItems = [
  {
    title: 'Cài đặt giao diện sáng - tối',
    questions: [
      {
        question: 'Hướng dẫn cài đặt',
        answer: 'Để cài đặt giao diện sáng - tối, bạn có thể sử dụng tùy chọn trong phần cài đặt của website.',
      },
      {
        question: 'Cài đặt tự động chuyển giao diện',
        answer: 'Bạn có thể bật chế độ tự động chuyển giao diện theo thời gian trong phần cài đặt.',
      },
    ],
  },
  {
    title: 'Thay đổi màu nền, cỡ chữ, font chữ khi đọc truyện',
    questions: [
      {
        question: 'Cách thay đổi giao diện đọc truyện',
        answer: 'Bạn có thể thay đổi cỡ chữ và font chữ trong phần cài đặt để có trải nghiệm đọc tốt hơn.',
      },
      {
        question: 'Cách thay đổi màu nền khi đọc',
        answer: 'Bạn có thể chọn màu nền sáng hoặc tối tùy vào sở thích.',
      },
    ],
  },
];
const breadcrumbItems = [
  {title: 'Home', path: '/'},
  {title: 'Account'},
];
</script>

<template>
  <section class="container mx-auto p-6 bg-white">
    <a-breadcrumb class="mb-6">
      <a-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index">
        <router-link v-if="item.path" :to="item.path">{{ item.title }}</router-link>
        <span v-else>{{ item.title }}</span>
      </a-breadcrumb-item>
    </a-breadcrumb>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div v-for="(item, index) in faqItems" :key="index" class="faq-column">
        <h1 class="font-bold text-lg mb-2 text-[#18A058]">{{ item.title }}</h1>
        <a-collapse accordion class="faq-accordion">
          <a-collapse-panel
              v-for="(qa, qaIndex) in item.questions"
              :key="qaIndex"
              :header="qa.question"
              header-class="text-[#18A058]"
          >
            <p>{{ qa.answer }}</p>
          </a-collapse-panel>
        </a-collapse>
      </div>
    </div>
  </section>
</template>

<style scoped>
.faq-accordion .ant-collapse-header {
  font-weight: bold;
  background-color: #E7F5EE;
}
</style>