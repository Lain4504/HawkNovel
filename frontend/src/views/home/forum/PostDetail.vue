<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {getPost} from '../../../api/post';
import {createComment, createReply, getAllComments, getAllRepliesByCommentId} from '../../../api/postComment';
import CommentSection from '../../../components/home/CommentSection.vue';
import {getUserProfile} from '../../../api/user';

const props = defineProps({
  id: {
    type: String,
    required: true
  }
});

interface Post {
  id: string;
  title: string;
  content: string;
  userId: string;
  created: string;
}

interface User {
  id: string;
  username: string;
  email: string;
  image: { path: string };
}

const post = ref<Post | null>(null);
const user = ref<User | null>(null);
const comments = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalComments = ref(0);

const fetchPostOwner = async () => {
  try {
    if (post.value) {
      user.value = await getUserProfile(post.value.userId);
    }
  } catch (error) {
    console.error('Failed to fetch post owner:', error);
  }
};

const fetchPost = async () => {
  try {
    const result = await getPost(props.id);
    post.value = {
      id: result.id,
      title: result.title,
      content: result.content,
      userId: result.userId,
      created: result.created,
    };
    await fetchPostOwner(); // Call fetchPostOwner after setting post details
  } catch (error) {
    console.error('Failed to fetch post:', error);
  }
};

const fetchComments = async (page: number, size: number) => {
  try {
    const result = await getAllComments(props.id, page, size);
    console.log('Comments:', result);
    comments.value = result.data;
    totalComments.value = result.totalElements; // Update to use totalElements from the API response
  } catch (error) {
    console.error('Failed to fetch comments:', error);
  }
};

const handleCommentAdded = () => {
  fetchComments(currentPage.value, pageSize.value);
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchComments(page, pageSize.value);
};

onMounted(() => {
  fetchPost();
  fetchComments(currentPage.value, pageSize.value);
});
</script>

<template>
  <div class="mx-auto">
    <article class="bg-white p-6 mt-6 rounded-lg shadow-md">
      <header class="flex items-center justify-between mb-6">
        <div class="flex items-center">
          <img :src="user?.image?.path" alt="Avatar" class="w-10 h-10 rounded-full mr-3">
          <div class="font-semibold text-lg ">{{ user?.username }}</div>
        </div>
        <time class="text-sm text-gray-500">{{ post?.created }}</time>
      </header>
      <h1 class="text-3xl font-bold mb-4">{{ post?.title }}</h1>
      <div class="content-body mb-6" v-html="post?.content"></div>
      <CommentSection :comments="comments" :create-comment-api="createComment"
                      :create-reply-api="createReply" :current-page="currentPage"
                      :fetch-comments="fetchComments"
                      :get-all-replies-api="getAllRepliesByCommentId"
                      :item-name="post?.title"
                      :itemId="props.id"
                      :owner-id="post?.userId"
                      :page-size="pageSize"
                      :total-comments="totalComments"
                      itemType="post"
                      @commentAdded="handleCommentAdded"
                      @pageChange="handlePageChange"
      />
    </article>
  </div>
</template>