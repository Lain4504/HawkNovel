<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue';
import {getUserProfile, updateUserProfile} from "../../../api/user";
import {changePassword} from "../../../api/auth";
import Tiptap from "../../../components/admin/Tiptap.vue";
import { message} from 'ant-design-vue';
import store from "../../../store";

const profile = ref({
  id: '',
  image: '',
  username: '',
  gender: '',
  dateOfBirth: '',
  bio: '',
});

const email = computed(() => store.getters.getEmail);

const passwords = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const activeTab = ref('profile');
const isDarkMode = computed(() => store.getters.isDarkMode);

const fetchUserProfile = async () => {
  try {
    const userProfile = await getUserProfile(store.getters.getUserId);
    profile.value = userProfile;
    profile.value.image = userProfile.image?.path || 'default-avatar.png'; // Use default image if none exists
  } catch (error) {
    console.error(error);
  }
}
const selectedImage = ref<File | null>(null);

const handleProfileSubmit = async () => {
  const profileData = {
    id: profile.value.id,
    username: profile.value.username,
    userId: store.getters.getUserId,
    dateOfBirth: profile.value.dateOfBirth,
    bio: profile.value.bio,
    gender: profile.value.gender,
    imageUrl: selectedImage.value ? '' : profile.value.image // Send empty string if new image is selected
  };
  const formData = new FormData();
  formData.append("profile", new Blob([JSON.stringify(profileData)], { type: "application/json" }));
  if (selectedImage.value) {
    formData.append("image", selectedImage.value);
  }
  try {
    await updateUserProfile(profile.value.id, formData);
    message.success('Profile Updated Successfully!');
  } catch (error) {
    console.error(error);
    message.error('Failed to update profile.');
  }
}
const handleChangePassword = async () => {
  if (passwords.value.newPassword !== passwords.value.confirmPassword) {
    message.error('New password and confirm password do not match!');
    return;
  }

  try {
    await changePassword(JSON.stringify({
      oldPassword: passwords.value.oldPassword,
      newPassword: passwords.value.newPassword,
      token: store.getters.getToken,
    }));

    // Reset password fields
    passwords.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    };

    message.success('Password Changed Successfully!');
  } catch (error) {
    console.error(error);
    message.error('Failed to change password.');
  }
}

const toggleTheme = () => {
  store.dispatch('toggleDarkMode');
  console.log('Dark mode:', store.getters.isDarkMode);
}

const handleImageChange = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (file) {
    selectedImage.value = file;
    profile.value.image = URL.createObjectURL(file);
  }
};
const formattedDateOfBirth = computed({
  get() {
    return profile.value.dateOfBirth ? profile.value.dateOfBirth.split('T')[0] : '';
  },
  set(value) {
    profile.value.dateOfBirth = value;
  }
});

onMounted(() => {
  fetchUserProfile();
});
</script>

<template>
  <a-layout>
    <a-layout-content>
      <div class="container mx-auto px-4 py-8 max-w-5xl">
        <div class="shadow-2xl rounded-2xl overflow-hidden">
          <a-tabs v-model:activeKey="activeTab" class="ml-4">
            <a-tab-pane key="profile" tab="Profile">
              <div class="p-6 space-y-6">
                <div class="flex items-center space-x-6">
                  <div class="relative">
                    <a-avatar
                        :src="profile.image || 'default-avatar.png'"
                        size="large"
                    />
                    <a-upload
                        class="absolute inset-0 opacity-0 cursor-pointer"
                        :beforeUpload="handleImageChange"
                    >
                      <a-button>Upload</a-button>
                    </a-upload>
                  </div>
                  <div>
                    <h2 class="text-2xl font-bold">{{ profile.username }}</h2>
                    <p class="text-gray-500">{{ email }}</p>
                  </div>
                </div>

                <a-form class="grid md:grid-cols-2 gap-6" @submit.prevent="handleProfileSubmit">
                  <div class="space-y-4">
                    <a-form-item label="Username">
                      <a-input v-model:value="profile.username"/>
                    </a-form-item>
                    <a-form-item label="Gender">
                      <a-select v-model:value="profile.gender">
                        <a-select-option value="male">Male</a-select-option>
                        <a-select-option value="female">Female</a-select-option>
                        <a-select-option value="other">Other</a-select-option>
                      </a-select>
                    </a-form-item>
                  </div>
                  <div class="space-y-4">
                    <a-form-item label="Date of Birth">
                      <a-input v-model:value="formattedDateOfBirth" type="date"/>
                    </a-form-item>
                  </div>
                  <div class="md:col-span-2 space-y-4">
                    <a-form-item label="Bio">
                      <Tiptap :content="profile.bio" @update:content="profile.bio = $event"/>
                    </a-form-item>
                  </div>
                  <div class="md:col-span-2 flex justify-end">
                    <a-button html-type="submit" type="primary">
                      Update Profile
                    </a-button>
                  </div>
                </a-form>
              </div>
            </a-tab-pane>
            <a-tab-pane key="security" tab="Security">
              <div class="p-6 space-y-6">
                <a-form class="space-y-4" @submit.prevent="handleChangePassword">
                  <a-form-item :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }" class="form-item-stacked"
                             label="Current Password">
                    <a-input-password v-model:value="passwords.oldPassword"/>
                  </a-form-item>
                  <a-form-item :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }" class="form-item-stacked"
                             label="New Password">
                    <a-input-password v-model:value="passwords.newPassword"/>
                  </a-form-item>
                  <a-form-item :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }" class="form-item-stacked"
                             label="Confirm New Password">
                    <a-input-password v-model:value="passwords.confirmPassword"/>
                  </a-form-item>
                  <div class="flex justify-end">
                    <a-button html-type="submit" type="primary">
                      Change Password
                    </a-button>
                  </div>
                </a-form>
              </div>
            </a-tab-pane>
            <a-tab-pane key="theme" tab="Theme">
              <div class="p-6 space-y-6">
                <div class="flex justify-between items-center">
                  <div>
                    <h3 class="text-lg font-medium">Theme</h3>
                    <p class="text-sm text-gray-500">Switch between light and dark modes</p>
                  </div>
                  <a-button v-if="!isDarkMode" shape="circle" @click="toggleTheme">
                    <font-awesome-icon :icon="['fas', 'sun']"/>
                  </a-button>
                  <a-button v-else shape="circle" @click="toggleTheme">
                    <font-awesome-icon :icon="['fas', 'moon']"/>
                  </a-button>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </div>
    </a-layout-content>
  </a-layout>
</template>