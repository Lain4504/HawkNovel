<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import store from "../../store/index";

interface Comment {
  id: string;
  userId: string;
  username: string;
  userAvatar: string;
  content: string;
  replies?: Reply[];
  replyPage?: number;
  replyCount: number;
  created: string;
}

interface Reply {
  id: string;
  username: string;
  userAvatar: string;
  replyContent: string;
  replyTo: string;
  userIdOfReplyTo: string;
  userId: string;
  created: string;
}

const props = defineProps<{
  itemId: string;
  itemType: 'post' | 'novel' | 'chapter';
  comments: Comment[];
  ownerId?: string;
  itemName?: string;
  createCommentApi: (data: any) => Promise<any>;
  createReplyApi: (data: any) => Promise<any>;
  getAllRepliesApi: (commentId: string, page: number, size: number) => Promise<any>;
  fetchComments: (page: number, size: number) => Promise<void>;
  currentPage: number;
  pageSize: number;
  totalComments: number;
}>();

const isAuthenticated = store.getters.isAuthenticated;
const submitting = ref(false);
const loading = ref(true);
const currentUsername = store.getters.getUsername;
const currentUserAvatar = store.getters.getUserImage;

onMounted(async () => {
  await props.fetchComments(props.currentPage, props.pageSize);
  loading.value = false;
});

const newComment = ref<string>('');
const replyText = ref<Record<string, string>>({});
const replyBoxes = ref<Record<string, boolean>>({});
const showReplies = ref<Record<string, boolean>>({});
const replyPageSize = 10;

const fetchReplies = async (commentId: string, page: number) => {
  try {
    const result = await props.getAllRepliesApi(commentId, page, replyPageSize);
    const comment = props.comments.find(comment => comment.id === commentId);
    if (comment) {
      if (page === 1) {
        comment.replies = []; // Refresh the replies list when loading from the first page
      } else {
        comment.replies = comment.replies || []; // Initialize replies if undefined
      }
      comment.replies.push(...result.data);
      comment.replyPage = page;
    }
    showReplies.value[commentId] = true;
  } catch (error) {
    console.error('Failed to fetch replies:', error);
  }
};
const emit = defineEmits(['commentAdded', 'pageChange']);
const addComment = async () => {
  if (newComment.value.trim()) {
    submitting.value = true;
    try {
      const commentData: any = {
        content: newComment.value,
        userId: store.getters.getUserId,
        ownerId: props.ownerId,
      };
      if (props.itemType === 'post') {
        commentData.postId = props.itemId;
        commentData.postName = props.itemName;
      } else if (props.itemType === 'novel') {
        commentData.novelId = props.itemId;
        commentData.novelName = props.itemName;
      } else if (props.itemType === 'chapter') {
        commentData.chapterId = props.itemId;
        commentData.chapterName = props.itemName;
      }
      await props.createCommentApi(commentData);
      newComment.value = '';
      emit('commentAdded');
    } catch (error) {
      console.error('Failed to add comment:', error);
    } finally {
      submitting.value = false;
    }
  }
};

const handlePageChange = (page: number) => {
  emit('pageChange', page);
};

const toggleReplyBox = (commentId: string) => {
  replyBoxes.value[commentId] = !replyBoxes.value[commentId];
};

const submitReply = async (commentId: string) => {
  if (replyText.value[commentId]?.trim()) {
    submitting.value = true;
    try {
      const comment = props.comments.find(comment => comment.id === commentId);
      const replyData: any = {
        commentId,
        replyContent: replyText.value[commentId],
        userId: store.getters.getUserId,
        replyTo: comment ? comment.username : '',
        userIdOfReplyTo: comment ? comment.userId : '',
      };
      if (props.itemType === 'post') {
        replyData.postId = props.itemId;
        replyData.postName = props.itemName;
      } else if (props.itemType === 'novel') {
        replyData.novelId = props.itemId;
        replyData.novelName = props.itemName;
      } else if (props.itemType === 'chapter') {
        replyData.chapterId = props.itemId;
        replyData.chapterName = props.itemName;
      }
      await props.createReplyApi(replyData);
      replyText.value[commentId] = '';
      await fetchReplies(commentId, 1);
    } catch (error) {
      console.error('Failed to submit reply:', error);
    } finally {
      submitting.value = false;
    }
  }
};

const toggleShowReplies = (commentId: string) => {
  if (showReplies.value[commentId]) {
    showReplies.value[commentId] = false;
  } else {
    fetchReplies(commentId, 1);
  }
};

const loadMoreReplies = (commentId: string) => {
  const comment = props.comments.find(comment => comment.id === commentId);
  if (comment) {
    fetchReplies(commentId, (comment.replyPage || 1) + 1);
  }
};

const toggleReplyBoxForReply = (replyId: string) => {
  replyBoxes.value[replyId] = !replyBoxes.value[replyId];
};

const submitReplyForReply = async (replyId: string, commentId: string) => {
  if (replyText.value[replyId]?.trim()) {
    submitting.value = true;
    try {
      const comment = props.comments.find(comment => comment.id === commentId);
      const reply = comment?.replies?.find(reply => reply.id === replyId);
      const replyData: any = {
        commentId,
        replyContent: replyText.value[replyId],
        userId: store.getters.getUserId,
        replyTo: reply ? reply.username : '',
        userIdOfReplyTo: reply ? reply.userId : '',
      };
      if (props.itemType === 'post') {
        replyData.postId = props.itemId;
        replyData.postName = props.itemName;
      } else if (props.itemType === 'novel') {
        replyData.novelId = props.itemId;
        replyData.novelName = props.itemName;
      } else if (props.itemType === 'chapter') {
        replyData.chapterId = props.itemId;
        replyData.chapterName = props.itemName;
      }
      await props.createReplyApi(replyData);
      replyText.value[replyId] = '';
      await fetchReplies(commentId, 1);
    } catch (error) {
      console.error('Failed to submit reply:', error);
    } finally {
      submitting.value = false;
    }
  }
};
</script>

<template>
  <div>
    <!-- Main comment form -->
    <a-comment v-if="isAuthenticated">
      <template #avatar>
        <a-avatar :src="currentUserAvatar" :alt="currentUsername"/>
      </template>
      <template #content>
        <a-form-item>
          <a-textarea v-model:value="newComment" :rows="4"/>
        </a-form-item>
        <a-form-item>
          <a-button html-type="submit" :loading="submitting" type="primary" @click="addComment">
            Add Comment
          </a-button>
        </a-form-item>
      </template>
    </a-comment>

    <!-- Comments list -->
    <a-skeleton :loading="loading" active>
      <template #default>
        <div v-for="comment in comments" :key="comment.id">
          <a-comment>
            <template #actions>
              <div class="comment-actions">
                <span key="comment-basic-reply-to" @click="toggleReplyBox(comment.id)" class="mr-2">Reply to</span>
                <span v-if="comment.replyCount > 0" @click="toggleShowReplies(comment.id)">
                  {{ showReplies[comment.id] ? 'Hide Replies' : `Show Replies (${comment.replyCount})` }}
                </span>
              </div>
            </template>
            <template #author>
              <a>{{ comment.username }}</a>
            </template>
            <template #avatar>
              <a-avatar :src="comment.userAvatar" :alt="comment.username"/>
            </template>
            <template #content>
              <p>{{ comment.content }}</p>
            </template>
            <template #datetime>
              <a-tooltip>
                <span> {{ comment.created }}</span>
              </a-tooltip>
            </template>
            <!-- Comment reply form -->
            <div v-if="replyBoxes[comment.id]">
              <a-comment>
                <template #avatar>
                  <a-avatar :src="currentUserAvatar" :alt="currentUsername"/>
                </template>
                <template #content>
                  <a-form-item>
                    <a-textarea v-model:value="replyText[comment.id]" :rows="4"/>
                  </a-form-item>
                  <a-form-item>
                    <a-button html-type="submit" :loading="submitting" type="primary"
                              @click="() => submitReply(comment.id)">
                      Add Reply
                    </a-button>
                  </a-form-item>
                </template>
              </a-comment>
            </div>

            <!-- Replies -->
            <div v-if="showReplies[comment.id] && comment.replies?.length">
              <div v-for="reply in comment.replies" :key="reply.id">
                <a-comment :author="reply.username" :avatar="reply.userAvatar">
                  <template #actions>
                    <div class="reply-actions">
                      <span @click="() => toggleReplyBoxForReply(reply.id)">Reply to</span>
                    </div>
                  </template>
                  <template #content>
                    <p>{{ reply.replyContent }}</p>
                  </template>
                </a-comment>

                <!-- Reply to reply form -->
                <div v-if="replyBoxes[reply.id]" class="ml-16">
                  <a-comment>
                    <template #avatar>
                      <a-avatar :src="currentUserAvatar" :alt="currentUsername"/>
                    </template>
                    <template #content>
                      <a-form-item>
                        <a-textarea v-model:value="replyText[reply.id]" :rows="4"/>
                      </a-form-item>
                      <a-form-item>
                        <a-button html-type="submit" :loading="submitting" type="primary"
                                  @click="() => submitReplyForReply(reply.id, comment.id)">
                          Add Reply
                        </a-button>
                      </a-form-item>
                    </template>
                  </a-comment>
                </div>
              </div>
            </div>

            <!-- Load more replies button -->
            <div
                v-if="showReplies[comment.id] && comment.replies?.length && comment.replyCount > comment.replies.length"
                class="load-more">
              <a-button @click="() => loadMoreReplies(comment.id)">Load More Replies</a-button>
            </div>
          </a-comment>
        </div>
      </template>
    </a-skeleton>

    <!-- Pagination -->
    <a-pagination
        v-if="totalComments > 0"
        :current="currentPage"
        :total="totalComments"
        :pageSize="pageSize"
        @change="handlePageChange"
    />
  </div>
</template>

<style scoped>
.ml-16 {
  margin-left: 64px;
}

.load-more {
  text-align: center;
  margin-top: 12px;
}

.reply-actions {
  display: flex;
  justify-content: space-between;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
}
</style>