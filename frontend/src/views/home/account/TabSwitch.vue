<script lang="ts" setup>
import { useRouter } from 'vue-router';

interface TabSwitch {
  name: string;
  label: string;
}

const tabs: TabSwitch[] = [
  {name: 'readinglist', label: 'TRUYỆN ĐANG ĐỌC'},
  {name: 'bookmark', label: 'BOOKMARK'}
];

const router = useRouter();

const handleTabChange = (key: string) => {
  router.push(`/list/${key}`);
};
</script>

<template>
  <section class="mx-auto bg-white p-4 rounded">
    <a-tabs :activeKey="$route.name" @change="handleTabChange">
      <a-tab-pane v-for="tab in tabs" :key="tab.name" :tab="tab.label">
        <router-view v-if="$route.name === tab.name"/>
      </a-tab-pane>
    </a-tabs>
  </section>
</template>
