<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { usePostStore } from '@/stores/post'


import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Image as ImageIcon, X as XIcon } from 'lucide-vue-next'

const userStore = useUserStore()
const postStore = usePostStore()

const content = ref('')
const selectedImageFile = ref<File | null>(null)
const imagePreviewUrl = ref<string | null>(null)
const imageInput = ref<HTMLInputElement | null>(null)

// Hàm xử lý khi người dùng chọn ảnh
function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    selectedImageFile.value = file
    // Tạo URL tạm thời để preview ảnh
    imagePreviewUrl.value = URL.createObjectURL(file)
  }
}

// Hàm xóa ảnh đã chọn
function removeImage() {
  selectedImageFile.value = null
  imagePreviewUrl.value = null
  if (imageInput.value) {
    imageInput.value.value = '' // Reset input file
  }
}

// Hàm xử lý khi nhấn nút đăng bài
async function handleSubmit() {
  if (!content.value.trim() && !selectedImageFile.value) {
    return // Không cho đăng nếu không có nội dung và không có ảnh
  }
  try {
    await postStore.createPost(content.value, selectedImageFile.value)
    // Reset form sau khi đăng thành công
    content.value = ''
    removeImage()
  } catch (error) {
    alert('Đăng bài thất bại!')
  }
}
</script>

<template>
  <Card>
    <CardContent class="p-4">
      <div class="flex gap-4">
        <Avatar>
          <AvatarImage :src="userStore.currentUser?.avatarUrl || ''" />
          <AvatarFallback>{{ userStore.currentUser?.fullName?.charAt(0) }}</AvatarFallback>
        </Avatar>
        <Textarea
          v-model="content"
          placeholder="Bạn đang nghĩ gì?"
          class="flex-1 border-none focus-visible:ring-0 text-base resize-none"
        />
      </div>
      <!-- Image Preview -->
      <div v-if="imagePreviewUrl" class="mt-4 relative">
        <img :src="imagePreviewUrl" class="rounded-lg w-full max-h-96 object-contain" />
        <Button @click="removeImage" variant="destructive" size="icon" class="absolute top-2 right-2 h-7 w-7">
          <XIcon class="h-4 w-4" />
        </Button>
      </div>
    </CardContent>
    <Separator />
    <CardFooter class="p-2 flex justify-between">
      <div class="flex">
        <Button @click="imageInput?.click()" variant="ghost">
          <ImageIcon class="mr-2 h-5 w-5 text-green-500" />
          Ảnh/Video
        </Button>
        <!-- Thêm các nút khác ở đây (tag friends, feeling...) -->
        <input ref="imageInput" type="file" accept="image/*" class="hidden" @change="handleFileChange" />
      </div>
      <Button @click="handleSubmit" :disabled="postStore.isLoading || (!content.trim() && !selectedImageFile)">
        {{ postStore.isLoading ? 'Đang đăng...' : 'Đăng' }}
      </Button>
    </CardFooter>
  </Card>
</template>