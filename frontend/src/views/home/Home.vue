<script lang="ts" setup>
import Header from "../../components/home/Header.vue";
import Footer from "../../components/home/Footer.vue";
import Layout from "./homepage/Layout.vue";
import { useRoute } from 'vue-router';
import { computed } from 'vue';
import Searchbar from "./homepage/Searchbar.vue";
import Banner from "../../components/home/Banner.vue";
const route = useRoute();
const isHomeRoute = computed(() => route.name === 'home');
const isChapterRoute = computed(() => route.name === 'chapter');
</script>

<template>
  <Header/>
  <Searchbar v-if="isHomeRoute"/>
  <div class="max-w-[83rem] mx-auto px-4 sm:px-6 lg:px-8 my-5 min-h-screen">
    <Banner v-if="!isHomeRoute && !isChapterRoute"/>
    <Layout v-if="isHomeRoute"/>
    <keep-alive>
      <router-view v-if="!isHomeRoute"/>
    </keep-alive>
  </div>
  <Footer/>
</template>
