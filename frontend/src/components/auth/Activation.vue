<script lang="ts" setup>
import {ref, onMounted} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {activeAccount} from "../../api/auth.ts";
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
const token = route.params.token as string;
const activate = async () => {
  try {
    await activeAccount(token);
    showNotification('success', 'Tài khoản đã được kích hoạt thành công!');
    setTimeout(() => {
      router.push('/login');
    }, 300);
  } catch (err) {
    error.value = true;
    showNotification('error', 'Kích hoạt tài khoản thất bại!');
  }
};

onMounted(() => {
  activate();
});
</script>