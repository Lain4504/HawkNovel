<template>
  <a-layout-footer class="bg-gray-900 text-gray-300 my-8">
    <div class="container px-4 py-8">
      <!-- Main Footer Content -->
      <a-row :gutter="16">
        <!-- About Section -->
        <a-col :xs="24" :md="6">
          <div class="space-y-4">
            <h3 class="text-xl font-bold text-white">HawkNovel</h3>
            <p class="text-sm">
              Khám phá thế giới truyện chữ đa dạng với hàng ngàn tác phẩm từ nhiều thể loại khác nhau.
            </p>
            <div class="flex space-x-4">
              <a href="#" class="hover:text-[#18A058]">
                <font-awesome-icon :icon="['fab', 'facebook-f']" class="text-xl" />
              </a>
              <a href="#" class="hover:text-[#18A058]">
                <font-awesome-icon :icon="['fab', 'x-twitter']" class="text-xl" />
              </a>
            </div>
          </div>
        </a-col>

        <!-- Quick Links -->
        <a-col :xs="24" :md="6">
          <div class="space-y-4">
            <h4 class="text-lg font-semibold text-white">Liên kết nhanh</h4>
            <ul class="space-y-2">
              <li>
                <router-link to="/novels" class="hover:text-[#18A058]">Truyện mới cập nhật</router-link>
              </li>
              <li>
                <router-link to="/rankings" class="hover:text-[#18A058]">Bảng xếp hạng</router-link>
              </li>
              <li>
                <router-link to="/completed" class="hover:text-[#18A058]">Truyện đã hoàn thành</router-link>
              </li>
              <li>
                <router-link to="/forum" class="hover:text-[#18A058]">Diễn đàn</router-link>
              </li>
            </ul>
          </div>
        </a-col>

        <!-- Categories -->
        <a-col :xs="24" :md="6">
          <div class="space-y-4">
            <h4 class="text-lg font-semibold text-white">Thể loại</h4>
            <ul class="space-y-2">
              <li>
                <router-link to="/category/action" class="hover:text-[#18A058]">Hành động</router-link>
              </li>
              <li>
                <router-link to="/category/fantasy" class="hover:text-[#18A058]">Tiên hiệp</router-link>
              </li>
              <li>
                <router-link to="/category/romance" class="hover:text-[#18A058]">Ngôn tình</router-link>
              </li>
              <li>
                <router-link to="/categories" class="hover:text-[#18A058]">Xem tất cả →</router-link>
              </li>
            </ul>
          </div>
        </a-col>

        <!-- Contact -->
        <a-col :xs="24" :md="6">
          <div class="space-y-4">
            <h4 class="text-lg font-semibold text-white">Liên hệ</h4>
            <ul class="space-y-2">
              <li class="flex items-center space-x-2">
                <font-awesome-icon :icon="['far', 'envelope']" />
                <span>support@hawknovel.com</span>
              </li>
              <li>
                <router-link to="/contact" class="hover:text-[#18A058]">Gửi phản hồi</router-link>
              </li>
              <li>
                <router-link to="/dmca" class="hover:text-[#18A058]">Báo cáo vi phạm</router-link>
              </li>
            </ul>
          </div>
        </a-col>
      </a-row>

      <!-- Bottom Footer -->
      <div class="mt-8 pt-8 border-t border-gray-800">
        <a-row justify="space-between" align="middle">
          <a-col :xs="24" :md="12" class="text-center md:text-left">
            <div class="text-sm">
              © 2023 HawkNovel. All Rights Reserved.
            </div>
          </a-col>
          <a-col :xs="24" :md="12" class="text-center md:text-right">
            <div class="flex justify-center md:justify-end space-x-4 text-sm">
              <router-link to="/about" class="hover:text-[#18A058]">Giới thiệu</router-link>
              <router-link to="/privacy-policy" class="hover:text-[#18A058]">Chính sách bảo mật</router-link>
              <router-link to="/terms" class="hover:text-[#18A058]">Điều khoản dịch vụ</router-link>
            </div>
          </a-col>
        </a-row>
      </div>
    </div>
  </a-layout-footer>
</template>

<script lang="ts" setup>
</script>
