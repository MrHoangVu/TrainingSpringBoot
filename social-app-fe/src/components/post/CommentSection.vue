<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { usePostStore } from '@/stores/post'
import commentApi from '@/api/commentApi'
import type { Comment } from '@/types/api'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { MoreHorizontal, Trash2, Pencil } from 'lucide-vue-next'

const props = defineProps<{
  postId: number,
  postAuthorId: number,
}>()

const userStore = useUserStore()
const postStore = usePostStore()

const comments = ref<Comment[]>([])
const isLoading = ref(false)
const newCommentContent = ref('')
const editingCommentId = ref<number | null>(null)
const editingContent = ref('')

const currentUser = computed(() => userStore.currentUser)

async function fetchComments() {
  isLoading.value = true
  try {
    const response = await commentApi.getComments(props.postId, 0, 10)
    comments.value = response.data.content.reverse()
  } catch (error) {
    console.error('Failed to fetch comments:', error)
  } finally {
    isLoading.value = false
  }
}

async function handlePostComment() {
  if (!newCommentContent.value.trim()) return
  try {
    const newComment = await postStore.createComment(props.postId, newCommentContent.value)
    comments.value.push(newComment)
    newCommentContent.value = ''
  } catch (error) {
    alert('Bình luận thất bại!')
  }
}

function startEditing(comment: Comment) {
  editingCommentId.value = comment.id
  editingContent.value = comment.content
}

async function handleUpdateComment() {
  if (!editingContent.value.trim() || editingCommentId.value === null) return
  try {
    const updatedComment = await commentApi.updateComment(editingCommentId.value, editingContent.value)
    const index = comments.value.findIndex(c => c.id === editingCommentId.value)
    if (index !== -1) {
      comments.value[index] = updatedComment.data
    }
    editingCommentId.value = null
  } catch (error) {
    alert('Sửa bình luận thất bại!')
  }
}

async function handleDeleteComment(commentId: number) {
  if (!confirm('Bạn có chắc muốn xóa bình luận này?')) return
  try {
    await commentApi.deleteComment(commentId)
    comments.value = comments.value.filter(c => c.id !== commentId)
    postStore.decrementCommentCount(props.postId)
  } catch (error) {
    alert('Xóa bình luận thất bại!')
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<template>
  <div class="p-4 pt-2">
    <div class="mt-2 flex items-center gap-2">
      <Avatar class="h-8 w-8">
        <AvatarImage :src="userStore.currentUser?.avatarUrl || ''" />
        <AvatarFallback>{{ userStore.currentUser?.fullName?.charAt(0) }}</AvatarFallback>
      </Avatar>
      <form @submit.prevent="handlePostComment" class="flex-1">
        <Input v-model="newCommentContent" placeholder="Viết bình luận..." class="rounded-full" />
      </form>
    </div>

    <div class="mt-4 space-y-1">
      <div v-if="isLoading" class="flex justify-center">
        <LoadingSpinner />
      </div>
      <div v-for="comment in comments" :key="comment.id" class="group flex items-start gap-2">
        <Avatar class="h-8 w-8">
          <AvatarImage :src="comment.author.avatarUrl || ''" />
          <AvatarFallback>{{ comment.author.fullName?.charAt(0) }}</AvatarFallback>
        </Avatar>
        <div class="flex-1">
          <div v-if="editingCommentId !== comment.id" class="rounded-lg bg-muted p-2">
            <p class="text-sm font-semibold">{{ comment.author.fullName }}</p>
            <p class="text-sm">{{ comment.content }}</p>
          </div>
          <form v-else @submit.prevent="handleUpdateComment">
            <Input v-model="editingContent" />
            <div class="mt-1 text-xs">
              Nhấn Enter để lưu, hoặc <button type="button" @click="editingCommentId = null" class="underline">Hủy</button>
            </div>
          </form>
        </div>

        <div v-if="currentUser && (currentUser.id === comment.author.id || currentUser.id === postAuthorId)" class="opacity-0 group-hover:opacity-100">
          <DropdownMenu>
            <DropdownMenuTrigger as-child>
              <Button variant="ghost" size="icon" class="h-6 w-6">
                <MoreHorizontal class="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem v-if="currentUser.id === comment.author.id" @click="startEditing(comment)">
                <Pencil class="mr-2 h-4 w-4" /> Chỉnh sửa
              </DropdownMenuItem>
              <DropdownMenuItem @click="handleDeleteComment(comment.id)" class="text-red-600">
                <Trash2 class="mr-2 h-4 w-4" /> Xóa
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </div>
  </div>
</template>