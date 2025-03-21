<script lang="ts" setup>
import {useRouter} from 'vue-router';
import Tiptap from './Tiptap.vue';
import {notification} from 'ant-design-vue';
import {type PropType, ref} from "vue";

interface InputField {
  id: string;
  label: string;
  type: string; // text, select, tiptap, etc.
  placeholder?: string;
  options?: { value: string; label: string }[];
  required?: boolean;
}

const props = defineProps({
  inputs: {
    type: Array as PropType<InputField[]>,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  handleAdd: {
    type: Function as PropType<(values: any) => Promise<void>>,
    required: true
  },
});

const router = useRouter();
const formData = ref<any>({});

const showNotification = (type: 'success' | 'error', message: string) => {
  notification[type]({
    message: type === 'success' ? 'Success' : 'Error',
    description: message,
    duration: 3,
  });
};

const handleAddItem = async () => {
  try {
    await props.handleAdd(formData.value);
    router.back();
    showNotification('success', 'Item added successfully.');
  } catch (error: any) {
    console.error('Failed to add item:', error);
    if (error.response && error.response.data) {
      showNotification('error', error.response.data.message || 'Novel category creation failed. Please try again.');
    } else if (error.request) {
      showNotification('error', 'No response from server. Please try again.');
    } else {
      showNotification('error', 'An unexpected error occurred. Please try again.');
    }
  }
};

const handleCancel = () => {
  router.back();
};
</script>

<template>
  <a-layout class="flex-1 p-6 bg-white shadow-md max-w-5xl mx-auto">
    <a-typography-title level="3" class="my-2" :style="{ color: '#18A058', fontSize: '20px' }">{{ title }}</a-typography-title>
    <a-form @submit.prevent="handleAddItem">
      <div class="space-y-6">
        <a-form-item v-for="input in inputs" :key="input.id" :label="input.label" :required="input.required" class="form-item">
          <template v-if="input.type === 'tiptap'">
            <Tiptap :content="formData[input.id]" @update:content="formData[input.id] = $event"/>
          </template>
          <template v-else-if="input.type === 'select'">
            <a-select v-model:value="formData[input.id]" class="w-full">
              <a-select-option v-for="option in input.options" :key="option.value" :value="option.value">
                {{ option.label }}
              </a-select-option>
            </a-select>
          </template>
          <template v-else>
            <a-input v-model:value="formData[input.id]" :type="input.type" :placeholder="input.placeholder || ''" class="w-full"/>
          </template>
        </a-form-item>
      </div>
      <div class="flex justify-end space-x-4 my-4">
        <a-button type="default" @click="handleCancel">Cancel</a-button>
        <a-button type="primary" html-type="submit">Save</a-button>
      </div>
    </a-form>
  </a-layout>
</template>

<style scoped>
.form-item {
  display: flex;
  flex-direction: column;
}
</style>