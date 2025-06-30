<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import CreatePost from '@/components/post/CreatePost.vue'
import PostCard from '@/components/post/PostCard.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { usePostStore } from '@/stores/post'

const postStore = usePostStore()

// Ref cho phần tử trigger ở cuối danh sách
const trigger = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

// Hàm này sẽ được gọi khi phần tử trigger đi vào màn hình
const onIntersect = (entries: IntersectionObserverEntry[]) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      postStore.fetchTimeline()
    }
  })
}

// Thiết lập observer khi component được mount
onMounted(() => {
  // Reset timeline cũ và fetch trang đầu tiên
  postStore.resetTimeline()
  postStore.fetchTimeline()

  // Tạo observer
  observer = new IntersectionObserver(onIntersect, {
    root: null, // quan sát so với viewport
    threshold: 0.5, // kích hoạt khi 50% phần tử trigger hiện ra
  })

  // Bắt đầu quan sát phần tử trigger
  if (trigger.value) {
    observer.observe(trigger.value)
  }
})

// Dọn dẹp observer khi component bị unmount để tránh memory leak
onUnmounted(() => {
  if (observer) {
    observer.disconnect()
  }
})
</script>

<template>
  <div class="space-y-6">
    <!-- Component tạo bài viết -->
    <CreatePost />

    <!-- Danh sách các bài viết -->
    <div v-if="postStore.posts.length > 0" class="space-y-6">
      <PostCard v-for="post in postStore.posts" :key="post.id" :post="post" />
    </div>
    
    <!-- Thông báo khi không có bài viết nào (chỉ hiện lúc đầu) -->
    <div v-else-if="!postStore.isLoading" class="text-center text-muted-foreground py-10">
      <p>Bảng tin của bạn chưa có bài viết nào.</p>
    </div>

    <!-- Loading Spinner và Trigger -->
    <div class="flex justify-center py-6">
      <!-- Hiển thị spinner khi đang tải -->
      <LoadingSpinner v-if="postStore.isLoading" />
      <!-- Hiển thị thông báo khi đã hết bài viết -->
      <p v-else-if="postStore.page >= postStore.totalPages && postStore.posts.length > 0" class="text-muted-foreground">
        Bạn đã xem hết bài viết.
      </p>

      <!-- Phần tử trigger vô hình để kích hoạt infinite scroll -->
      <div ref="trigger" class="h-10"></div>
    </div>
  </div>
</template>