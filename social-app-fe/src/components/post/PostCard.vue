<script setup lang="ts">
import { ref, computed } from "vue";
import type { Post } from "@/types/api";
import { usePostStore } from "@/stores/post";
import { useUserStore } from "@/stores/user";

// Components
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  Card,
  CardContent,
  CardHeader,
  CardFooter,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import CommentSection from "./CommentSection.vue";

// Icons
import {
  ThumbsUp,
  MessageSquare,
  MoreHorizontal,
  Trash2,
  Pencil,
} from "lucide-vue-next";

const props = defineProps<{
  post: Post;
}>();

const postStore = usePostStore();
const userStore = useUserStore();

const showComments = ref(false);
const isEditDialogOpen = ref(false);
const isDeleteDialogOpen = ref(false);
const editingContent = ref(props.post.content || "");

// Kiểm tra quyền sở hữu
const isMyPost = computed(
  () => userStore.currentUser?.id === props.post.author.id
);

function handleLikeClick() {
  postStore.toggleLike(props.post.id);
}
function toggleComments() {
  showComments.value = !showComments.value;
}

async function handleUpdatePost() {
  if (!editingContent.value.trim()) return;
  try {
    await postStore.updatePost(props.post.id, editingContent.value);
    isEditDialogOpen.value = false;
  } catch (error) {
    alert("Cập nhật thất bại!");
  }
}

async function handleDeletePost() {
  try {
    await postStore.deletePost(props.post.id);
    isDeleteDialogOpen.value = false;
    // Component sẽ tự động bị xóa khỏi DOM
  } catch (error) {
    alert("Xóa bài viết thất bại!");
  }
}
</script>

<template>
  <Card>
    <CardHeader class="flex flex-row items-center gap-3 p-4">
      <Avatar>
        <AvatarImage :src="post.author.avatarUrl || ''" />
        <AvatarFallback>{{ post.author.fullName?.charAt(0) }}</AvatarFallback>
      </Avatar>
      <div class="grid gap-1 flex-1">
        <p class="font-semibold">{{ post.author.fullName }}</p>
        <p class="text-xs text-muted-foreground">
          {{ new Date(post.createdAt).toLocaleString() }}
        </p>
      </div>

      <!-- Menu Sửa/Xóa -->
      <DropdownMenu v-if="isMyPost">
        <DropdownMenuTrigger as-child>
          <Button variant="ghost" size="icon" class="h-8 w-8">
            <MoreHorizontal class="h-4 w-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuItem @click="isEditDialogOpen = true">
            <Pencil class="mr-2 h-4 w-4" /> Chỉnh sửa
          </DropdownMenuItem>
          <DropdownMenuItem
            @click="isDeleteDialogOpen = true"
            class="text-red-600"
          >
            <Trash2 class="mr-2 h-4 w-4" /> Xóa
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </CardHeader>

    <!-- Dialog Chỉnh sửa -->
    <Dialog v-model:open="isEditDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Chỉnh sửa bài viết</DialogTitle>
        </DialogHeader>
        <Textarea v-model="editingContent" class="min-h-[100px]" />
        <DialogFooter>
          <Button @click="handleUpdatePost">Lưu thay đổi</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Dialog Xác nhận Xóa -->
    <Dialog v-model:open="isDeleteDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Bạn có chắc chắn?</DialogTitle>
          <DialogDescription>
            Hành động này không thể hoàn tác. Bài viết sẽ bị xóa vĩnh viễn.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" @click="isDeleteDialogOpen = false"
            >Hủy</Button
          >
          <Button variant="destructive" @click="handleDeletePost">Xóa</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Nội dung bài viết -->
    <CardContent class="p-4 pt-0">
      <p class="mb-4 whitespace-pre-wrap">{{ post.content }}</p>
      <img
        v-if="post.imageUrl"
        :src="post.imageUrl"
        class="rounded-lg border w-full"
      />
    </CardContent>

    <CardFooter class="p-4 pt-0 flex justify-between"> </CardFooter>
    <div class="border-t mx-4"></div>
    <CardFooter class="p-2"> </CardFooter>
    <div v-if="showComments" class="border-t">
      <CommentSection :post-id="post.id" :post-author-id="post.author.id" />
    </div>
  </Card>
</template>
