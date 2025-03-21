<script lang="ts" setup>
import {ref, watch} from 'vue';
import Tiptap from "./Tiptap.vue";
import {notification} from 'ant-design-vue';
import router from '../../router';

interface Field {
  id: string;
  label: string;
  type: string;
  options?: { value: string; label: string }[];
  required?: boolean;
}

interface Props {
  fields: { title: string; inputs: Field[] };
  initialData: Record<string, any>;
  onSave: (id: string, data: Record<string, any>) => Promise<void>;
}

const props = defineProps<Props>();
const formData = ref({...props.initialData});

watch(
    () => props.initialData,
    (newData) => {
      formData.value = {...newData};
    }
);

const showNotification = (type: 'success' | 'error', message: string) => {
  notification[type]({
    message: type === 'success' ? 'Success' : 'Error',
    description: message,
    duration: 3,
  });
};

const handleSave = async () => {
  try {
    await props.onSave(formData.value.id, formData.value);
    showNotification('success', 'Item updated successfully.');
    router.back();
  } catch (error: any) {
    console.error('Failed to update item:', error);
    if (error.response) {
      showNotification('error', error.response.data.message || 'Item update failed. Please try again.');
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
    <a-typography-title level="3" class="my-2" :style="{ color: '#18A058', fontSize: '20px' }">Edit
      {{ props.fields.title }}
    </a-typography-title>
    <a-form @submit.prevent="handleSave">
      <div class="space-y-6">
        <a-form-item v-for="field in props.fields.inputs" :key="field.id" :label="field.label"
                     :required="field.required" class="form-item">
          <template v-if="field.type === 'tiptap'">
            <Tiptap :content="formData[field.id]" @update:content="formData[field.id] = $event"/>
          </template>
          <template v-else-if="field.type === 'select'">
            <a-select v-model:value="formData[field.id]" class="w-full">
              <a-select-option v-for="option in field.options" :key="option.value" :value="option.value">
                {{ option.label }}
              </a-select-option>
            </a-select>
          </template>
          <template v-else>
            <a-input v-model:value="formData[field.id]" :type="field.type" class="w-full"/>
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
</style>