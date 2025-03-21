<script lang="ts" setup>
import {ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {activeAccount} from "../../../api/auth";
import {notification} from "ant-design-vue";

const router = useRouter();
const route = useRoute();

const error = ref(false);
const showNotification = (type: 'success' | 'error', message: string) => {
  notification[type]({
    message: type === 'success' ? 'Success' : 'Error',
    description: message,
    duration: 3,
  });
};

const breadcrumbs = [
  {title: 'Trang chủ', href: '/'},
  {title: 'Active'},
];
const token = route.params.token as string;
const activate = async () => {
  console.log(`Token: ${token}`); // Debug token
  try {
    await activeAccount(token); // Gọi hàm API
    showNotification('success', 'Tài khoản đã được kích hoạt thành công!');
    setTimeout(() => {
      router.push('/login'); // Điều hướng đến trang login
    }, 1000);
  } catch (err) {
    error.value = true; // Đánh dấu trạng thái lỗi
    console.error('Activation failed:', err); // Log chi tiết lỗi
    showNotification('error', 'Kích hoạt tài khoản thất bại!');
  }
};

</script>

<template>
  <div>
    <!-- Breadcrumb Component -->
    <Breadcrumb :items="breadcrumbs"/>
    <div class="flex justify-center items-center">
      <div class="md:w-1/2 lg:w-1/3 text-center">
        <a-button
            class="mt-5 mb-5"
            type="primary"
            @click="activate"
        >
          Kích hoạt
        </a-button>
        <p v-if="error" class="text-red-500 text-center">
          Kích hoạt tài khoản thất bại!
        </p>
      </div>
    </div>
  </div>
</template>
