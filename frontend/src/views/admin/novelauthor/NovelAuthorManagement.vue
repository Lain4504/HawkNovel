<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {deleteNovel, getNovel} from "../../../api/novel";
import EditContentChapter from "./chapter/EditNovelChapter.vue";
import AddChapter from "./chapter/AddNovelChapter.vue";
import AddNovelVolume from "./volume/AddNovelVolume.vue";
import EditNovelVolume from "./volume/EditNovelVolume.vue";
import {useRoute} from "vue-router";
import {deleteVolume, getVolumesByNovelId} from "../../../api/novelVolume";
import {deleteChapter, getChaptersByVolumeId} from "../../../api/novelChapter";
import router from "../../../router";
import NovelEdit from "./novel/NovelEdit.vue";
import OrderSortChapter from "./chapter/OrderSortChapter.vue";
import OrderSortVolume from "./volume/OrderSortVolume.vue";
import {notification} from "ant-design-vue";


interface Chapter {
  id: string;
  chapterTitle: string;
  chapterNumber: number;
}

interface Volume {
  volumeName: string;
  id: string;
  chapters: Chapter[];
}

interface Novel {
  id: string;
  title: string;
  authorName: string;
  description: string;
  bookStatus: string;
  categories: Array<{ name: string }>;
  coverPicture: string;
  image: {
    id: string;
    name: string;
    type: string;
    path: string;
  };
}

const activeChapters = ref<Record<string, boolean>>({});
const volumes = ref<Volume[]>([]);
const novel = ref<Novel | Record<string, any>>({});
const showEditNovel = ref(false);
const showEditChapter = ref(false);
const showAddChapter = ref(false);
const showOrderSortChapter = ref(false);
const showOrderSortVolume = ref(false);
const showEditVolume = ref(false);
const showAddVolume = ref(false);
const selectedVolumeData = ref<Volume | Record<string, any>>({});
const selectedChapterData = ref<Chapter | Record<string, any>>({});
const showConfirmModal = ref(false);
const itemToDelete = ref<{ id: string, type: string } | null>(null);

const route = useRoute();
const id = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id;

const fetchChaptersForVolume = async (volumeId: string) => {
  try {
    const response = await getChaptersByVolumeId(volumeId);
    const chapters = response.map((chapter: any) => ({
      id: chapter.id,
      volumeId: chapter.volumeId,
      chapterTitle: chapter.chapterTitle,
      chapterNumber: chapter.chapterNumber,
      content: chapter.content,
      created: chapter.created,
      createdDate: chapter.createdDate,
      status: chapter.status,
    }));
    const volume = volumes.value.find(v => v.id === volumeId);
    if (volume) {
      volume.chapters = chapters.length ? chapters : [];
    }
  } catch (error) {
    console.error("Error fetching chapters:", error);
    const volume = volumes.value.find(v => v.id === volumeId);
    if (volume) {
      volume.chapters = [];
    }
  }
};

const refreshNovelData = async () => {
  try {
    const novelData = await getNovel(id);
    novel.value = {
      id: novelData.id,
      title: novelData.title,
      authorName: novelData.authorName,
      description: novelData.description,
      bookStatus: novelData.status,
      categories: novelData.categories,
      image: novelData.image,
    };

    volumes.value = await getVolumesByNovelId(id);

    const activeChaptersState = {...activeChapters.value};
    activeChapters.value = {};
    volumes.value.forEach(volume => {
      activeChapters.value[volume.volumeName] = activeChaptersState[volume.volumeName] || false;
      if (activeChapters.value[volume.volumeName]) {
        fetchChaptersForVolume(volume.id);
      }
    });
  } catch (error) {
    console.error("Error refreshing novel data:", error);
  }
};

const toggleChapters = async (volumeName: string) => {
  const volume = volumes.value.find(v => v.volumeName === volumeName);
  if (volume) {
    if (!volume.chapters) {
      await fetchChaptersForVolume(volume.id);
    }
    activeChapters.value[volumeName] = !activeChapters.value[volumeName];
  } else {
    activeChapters.value[volumeName] = true;
  }
};


const viewChapter = (chapterId: string, novelId: string) => {
  router.push({name: 'chapter', params: {novel: novelId, chapter: chapterId}});
};

const viewNovel = (novelId: string) => {
  router.push({name: 'noveldetail', params: {id: novelId}});
};

const showModal = (modalName: string) => {
  showEditNovel.value = false;
  showEditVolume.value = false;
  showAddVolume.value = false;
  showEditChapter.value = false;
  showAddChapter.value = false;
  showOrderSortChapter.value = false;
  showOrderSortVolume.value = false;
  if (modalName === 'editNovel') showEditNovel.value = true;
  if (modalName === 'editVolume') showEditVolume.value = true;
  if (modalName === 'addVolume') showAddVolume.value = true;
  if (modalName === 'editChapter') showEditChapter.value = true;
  if (modalName === 'addChapter') showAddChapter.value = true;
  if (modalName === 'orderSortChapter') showOrderSortChapter.value = true;
  if (modalName === 'orderSortVolume') showOrderSortVolume.value = true;
};

const sortVolumes = () => {
  showModal('orderSortVolume');
};

const sortChapters = (volumeId: string) => {
  if (volumeId) {
    const volume = volumes.value.find(v => v.id === volumeId);
    if (volume) {
      selectedVolumeData.value = volume;
      showModal('orderSortChapter');
    } else {
      selectedVolumeData.value = {};
    }
  }
};

const editChapter = (chapter: Chapter) => {
  selectedChapterData.value = chapter;
  showModal('editChapter');
};

const addChapter = (volumeId: string | null) => {
  if (volumeId) {
    const volume = volumes.value.find(v => v.id === volumeId);
    if (volume) {
      selectedVolumeData.value = volume;
      showModal('addChapter');
    } else {
      selectedVolumeData.value = {};
    }
  }
};

const addVolume = () => {
  showModal('addVolume');
};

const editVolume = (volume: Volume) => {
  selectedVolumeData.value = volume;
  showModal('editVolume');
};

const editNovel = () => {
  showModal('editNovel');
};

const handleChapterUpdated = async () => {
  if (selectedVolumeData.value && selectedVolumeData.value.id) {
    await fetchChaptersForVolume(selectedVolumeData.value.id);
  }
  await refreshNovelData();
};

const handleVolumeUpdated = async () => {
  await refreshNovelData();
};

const handleVolumeCreated = async () => {
  await refreshNovelData();
};

const handleDelete = (id: string, type: string) => {
  itemToDelete.value = {id, type};
  showConfirmModal.value = true;
};

const handleNovelUpdated = async () => {
  await refreshNovelData();
};

const showNotification = (type: 'success' | 'error', message: string) => {
  notification[type]({
    message: type === 'success' ? 'Success' : 'Error',
    description: message,
    duration: 3,
  });
};

const confirmDelete = async () => {
  if (itemToDelete.value) {
    try {
      if (itemToDelete.value.type === "novel") {
        await deleteNovel(itemToDelete.value.id);
        router.back();
      } else if (itemToDelete.value.type === "volume") {
        await deleteVolume(itemToDelete.value.id);
      } else if (itemToDelete.value.type === "chapter") {
        await deleteChapter(itemToDelete.value.id);
        if (selectedVolumeData.value && selectedVolumeData.value.id) {
          await fetchChaptersForVolume(selectedVolumeData.value.id);
        }
      }
      showNotification('success', `${itemToDelete.value.type} deleted successfully.`);
      await refreshNovelData();
    } catch (error: any) {
      console.error('Failed to delete item:', error);
      if (error.response) {
        showNotification('error', error.response.data.message || 'Item delete failed. Please try again.');
      } else if (error.request) {
        showNotification('error', 'No response from server. Please try again.');
      } else {
        showNotification('error', 'An unexpected error occurred. Please try again.');
      }
    }
  }
  showConfirmModal.value = false;
};

const cancelDelete = () => {
  showConfirmModal.value = false;
};

onMounted(() => {
  refreshNovelData();
});
</script>

<template>
  <a-modal
      v-model:visible="showConfirmModal"
      title="Are you sure you want to delete?"
      okText="Delete"
      cancelText="Cancel"
      @ok="confirmDelete"
      @cancel="cancelDelete"
  >
    <p>This action cannot be undone.</p>
  </a-modal>
  <div class="flex justify-center items-center max-w-7xl mx-auto">
    <a-card class="bg-white rounded-lg shadow-md w-full">
      <a-typography-title level={1} style="font-size: 20px;">Quản lý tiểu thuyết</a-typography-title>
      <div class="space-y-4 mt-10">
        <div class="md:col-span-1 relative">
          <a-dropdown :trigger="['click']">
            <template #overlay>
              <a-menu style="width: 200px">
                <a-menu-item key="1" @click="viewNovel(novel.id)">View Novel</a-menu-item>
                <a-menu-item key="2" @click="addVolume">Add Volume</a-menu-item>
                <a-menu-item key="3" @click="sortVolumes">Sort Volumes</a-menu-item>
                <a-menu-item key="4" @click="editNovel">Edit Novel</a-menu-item>
                <a-menu-item key="5" @click="() => { handleDelete(novel.id, 'novel'); }" style="color: #ff0f0f">Delete
                  Novel
                </a-menu-item>
              </a-menu>
            </template>
            <a-typography-title level={2} style="font-size: 16px">{{ novel.title }}</a-typography-title>
          </a-dropdown>
        </div>
        <div>
          <ul class="space-y-4 text-gray-700">
            <li v-for="volume in volumes" :key="volume.volumeName" class="relative">
              <div class="flex items-center w-full">
                <a-button type="default" class="mr-2" @click="toggleChapters(volume.volumeName)">
                  <font-awesome-icon
                      :icon="activeChapters[volume.volumeName] ? 'fas fa-square-minus' : 'fas fa-square-plus'"
                      size='xl'/>
                </a-button>
                <a-dropdown :trigger="['click']">
                  <template #overlay>
                    <a-menu>
                      <a-menu-item key="1" @click="() => { addChapter(volume.id) }">Add Chapter</a-menu-item>
                      <a-menu-item key="2" @click="() => { sortChapters(volume.id) }">Sort Chapters</a-menu-item>
                      <a-menu-item key="3" @click="() => { editVolume(volume) }">Edit Volume</a-menu-item>
                      <a-menu-item key="4" @click="() => { handleDelete(volume.id, 'volume') }" style="color: #ff0f0f">
                        Delete Volume
                      </a-menu-item>
                    </a-menu>
                  </template>
                  <button class="text-left font-medium hover:underline">
                    {{ volume.volumeName }}
                  </button>
                </a-dropdown>
              </div>
              <div v-if="activeChapters[volume.volumeName]" class="mt-2 relative">
                <div v-if="!volume.chapters || volume.chapters.length === 0" class="text-gray-500 text-sm">
                  No chapters
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  <div v-for="chapter in volume.chapters" :key="chapter.id" class="text-left relative">
                    <a-dropdown :trigger="['click']">
                      <template #overlay>
                        <a-menu>
                          <a-menu-item key="1" @click="() => { viewChapter(chapter.id, novel.id) }">View Chapter
                          </a-menu-item>
                          <a-menu-item key="2" @click="() => { editChapter(chapter) }">Edit Chapter</a-menu-item>
                          <a-menu-item key="3" @click="() => { handleDelete(chapter.id, 'chapter') }"
                                       style="color: #ff0f0f">Delete Chapter
                          </a-menu-item>
                        </a-menu>
                      </template>
                      <a-button type="link" class="text-sm hover:underline">
                        {{ chapter.chapterTitle }}
                      </a-button>
                    </a-dropdown>
                  </div>
                </div>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </a-card>
  </div>
  <div class="flex justify-center items-center max-w-7xl mx-auto">
    <EditContentChapter v-if="showEditChapter" :chapterData="selectedChapterData"
                        class="my-10" @chapter-updated="handleChapterUpdated"/>
    <AddChapter v-if="showAddChapter" :volumeId="selectedVolumeData.id" class="my-10"
                @chapter-added="handleChapterUpdated"/>
    <AddNovelVolume v-if="showAddVolume" :novelId="novel.id" class="my-10" @volume-added="handleVolumeCreated"/>
    <EditNovelVolume v-if="showEditVolume" :volumeData="selectedVolumeData" class="my-10"
                     @volume-updated="handleVolumeUpdated"/>
    <NovelEdit v-if="showEditNovel" :novel="novel" class="my-10" @novel-updated="handleNovelUpdated"/>
    <OrderSortChapter v-if="showOrderSortChapter" :volumeId="selectedVolumeData.id" class="my-10"
                      @chapter-order-updated="handleChapterUpdated"/>
    <OrderSortVolume v-if="showOrderSortVolume" :novelId="novel.id" class="my-10"
                     @volume-order-updated="handleVolumeUpdated"/>
  </div>
</template>

<style scoped>
.bg-white {
  background-color: #FFFFFF;
}

.border {
  border-color: #E7F5EE;
}
</style>