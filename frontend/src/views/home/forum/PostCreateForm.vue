<script lang="ts" setup>
import DynamicFormNew from "../../../components/admin/DynamicFormNew.vue"; // Import dynamic form component
import {onMounted, ref, watch} from "vue";
import {createPost} from "../../../api/post";
import {getPostCategoriesWithoutPagination} from "../../../api/postCategory";
import store from "../../../store";

// State for categories
const categories = ref<any[]>([]);

// Fetch categories
const fetchCategories = async () => {
  try {
    const result = await getPostCategoriesWithoutPagination();
    categories.value = result.map((category: any) => ({
      value: category.id,
      label: category.name,
    })); // Map lại format
  } catch (error) {
    console.error("Failed to fetch post categories:", error);
  }
};

watch(categories, (newCategories) => {
  const categoryInput = inputs.value.find((input) => input.id === "categoryId");
  if (categoryInput) {
    categoryInput.options = newCategories;
  }
});
onMounted(() => {
  fetchCategories();
});

// Handle form submission
const handleAddPost = async (formData: any) => {
  const postData = {
    ...formData,
    userId: store.getters.getUserId, // Lấy userId từ store
  };
  await createPost(postData);
};

// Form inputs
const inputs = ref([
  {
    id: "title",
    label: "Tiêu đề",
    type: "text",
    placeholder: "Nhập tiêu đề bài viết",
    required: true,
  },
  {
    id: "content",
    label: "Nội dung",
    type: "tiptap", // Sử dụng Tiptap editor
    required: true,
  },
  {
    id: "categoryId",
    label: "Chọn chuyên mục",
    type: "select",
    options: categories.value, // Bind với categories
    required: true,
  },
]);
</script>

<template>
  <div class="max-w-7xl mx-auto p-8">
    <Breadcrumb
        :breadcrumbs="[
        { label: 'Home', href: '/' },
        { label: 'Forum', href: '/forum' },
        { label: 'Post', href: '/post', isCurrent: true }
      ]"
    />
    <!-- Dynamic Form -->
    <DynamicFormNew
        :handle-add="handleAddPost"
        :inputs="inputs"
        location="/post-forum"
        title="Tạo bài viết mới"
    />
  </div>
</template>
