<script lang="ts" setup>
import {onMounted, ref} from "vue";
import DynamicFormEdit from "../../../components/admin/DynamicFormEdit.vue";
import {getPost, updatePost} from "../../../api/post";
import router from "../../../router";
import {getPostCategoriesWithoutPagination} from "../../../api/postCategory.ts";

const fields = {
  title: "Edit Post",
  inputs: [
    {id: "title", label: "Title", type: "text", required: true},
    {id: "content", label: "Content", type: "tiptap", required: true},
    {
      id: "categoryId",
      label: "Category",
      type: "select",
      options: [], // Options will be loaded from API
      required: true,
    },
  ],
};

// Nhận prop `id` từ route hoặc component cha
const props = defineProps({
  id: {
    type: String,
    required: true,
  },
});

// Khởi tạo biến chứa dữ liệu ban đầu
const initialData = ref({});

// Hàm tải danh mục chuyên mục từ API
const categoryMap = ref(new Map());

const loadCategories = async () => {
  try {
    const categories = await getPostCategoriesWithoutPagination();
    categoryMap.value = new Map(
        categories.map((category: { name: string; id: string }) => [category.name, category.id]));

    // Ánh xạ options cho select
    const categoryInput = fields.inputs.find((input) => input.id === "categoryId");
    if (categoryInput) {
      categoryInput.options = categories.map((category: { name: string; id: string }) => ({
        value: category.id,
        label: category.name,
      }));
    }
  } catch (error) {
    console.error("Failed to load categories:", error);
  }
};


// Hàm tải dữ liệu bài viết từ API
const loadPost = async () => {
  try {
    const response = await getPost(props.id);

    // Ánh xạ categoryName sang categoryId
    if (response.categoryName && categoryMap.value.has(response.categoryName)) {
      response.categoryId = categoryMap.value.get(response.categoryName);
    }

    initialData.value = response;
  } catch (error) {
    console.error("Failed to fetch post:", error);
  }
};


// Hàm xử lý lưu dữ liệu
const handleSave = async (id: string, data: Record<string, any>) => {
  try {
    await updatePost(id, data);
    console.log("Post updated successfully!");
    await router.push({name: "postList"});
  } catch (error) {
    console.error("Failed to update post:", error);
    throw error; // Ném lỗi để component con xử lý
  }
};

// Hàm xử lý khi người dùng hủy chỉnh sửa
const handleCancel = () => {
  router.back();
};

// Gọi hàm tải dữ liệu khi component được mount
onMounted(async () => {
  await loadCategories(); // Load danh mục trước
  await loadPost(); // Sau đó load bài viết
});
</script>

<template>
  <div class="mx-auto p-8">
    <Breadcrumb
        :breadcrumbs="[
        { label: 'Home', href: '/' },
        { label: 'Forum', href: '/forum' },
        { label: 'Post', href: '/post', isCurrent: true },
      ]"
    />

    <DynamicFormEdit
        :fields="fields"
        :initialData="initialData"
        :onCancel="handleCancel"
        :onSave="handleSave"
        @error="() => console.error('Update failed!')"
        @success="() => console.log('Update successful!')"
    />
  </div>
</template>
