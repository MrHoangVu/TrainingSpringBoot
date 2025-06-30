<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useUserStore } from "@/stores/user";
import { usePostStore } from "@/stores/post";
import { useRoute } from "vue-router";
import { toTypedSchema } from "@vee-validate/zod";
import * as z from "zod";
import { Form, Field } from "vee-validate";

// Import types and APIs
import type { UserProfile, FriendshipStatus, Post } from "@/types/api";
import userApi from "@/api/userApi";
import friendApi from "@/api/friendApi";

// Import components
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import LoadingSpinner from "@/components/ui/LoadingSpinner.vue";
import CreatePost from "@/components/post/CreatePost.vue";
import PostCard from "@/components/post/PostCard.vue";

// Import icons
import {
  Camera,
  UserPlus,
  UserX,
  Check,
  X,
  Briefcase,
  Home,
  Gift,
  Grid,
  Image,
  UserCircle2,
  Users,
  MessageCircle,
} from "lucide-vue-next";

const userStore = useUserStore();
const postStore = usePostStore();
const route = useRoute();

// STATE
const profileData = ref<UserProfile | null>(null);
const friendshipStatus = ref<FriendshipStatus | null>(null);
const userPosts = ref<Post[]>([]);
const isLoadingProfile = ref(true);
const isLoadingPosts = ref(false);
const isEditDialogOpen = ref(false);
const avatarInput = ref<HTMLInputElement | null>(null);

// COMPUTED
const isMyProfile = computed(
  () => userStore.currentUser?.id === Number(route.params.userId)
);

// METHODS
async function fetchProfileData() {
  isLoadingProfile.value = true;
  userPosts.value = []; // Reset bài viết khi đổi profile
  const userId = Number(route.params.userId);
  try {
    if (isMyProfile.value && userStore.currentUser) {
      profileData.value = userStore.currentUser;
      friendshipStatus.value = "SELF";
    } else {
      const [profileRes, statusRes] = await Promise.all([
        userApi.getUserById(userId),
        userApi.getFriendshipStatus(userId),
      ]);
      profileData.value = profileRes.data;
      friendshipStatus.value = statusRes.data.status;
    }
    await fetchUserPosts(userId);
  } catch (error) {
    console.error("Failed to load profile data:", error);
    profileData.value = null;
  } finally {
    isLoadingProfile.value = false;
  }
}

async function fetchUserPosts(userId: number) {
  isLoadingPosts.value = true;
  try {
    if (postStore.posts.length === 0 && !postStore.isLoading) {
      await postStore.fetchTimeline();
    }
    userPosts.value = postStore.posts
      .filter((post) => post.author.id === userId)
      .sort(
        (a, b) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
  } catch (error) {
    console.error("Failed to fetch user posts:", error);
  } finally {
    isLoadingPosts.value = false;
  }
}

const editProfileSchema = toTypedSchema(
  z.object({
    fullName: z.string().nullable(),
    dateOfBirth: z.string().nullable(),
    occupation: z.string().nullable(),
    address: z.string().nullable(),
  })
);

async function handleUpdateProfile(values: any) {
  try {
    await userStore.updateUserProfile(values);
    isEditDialogOpen.value = false;
  } catch (error) {
    alert("Cập nhật thất bại!");
  }
}

function triggerAvatarUpload() {
  avatarInput.value?.click();
}

async function handleAvatarChange(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    try {
      await userStore.updateUserAvatar(target.files[0]);
    } catch (error) {
      alert("Upload avatar thất bại!");
    }
  }
}

async function handleFriendAction() {
  if (!profileData.value) return;
  const targetId = profileData.value.id;

  try {
    switch (friendshipStatus.value) {
      case "NONE":
        await friendApi.sendRequest(targetId);
        break;
      case "PENDING_SENT":
        await friendApi.cancelRequest(targetId);
        break;
      case "PENDING_RECEIVED":
        await friendApi.acceptRequest(targetId);
        break;
      case "FRIENDS":
        await friendApi.unfriend(targetId);
        break;
    }
    const statusRes = await userApi.getFriendshipStatus(targetId);
    friendshipStatus.value = statusRes.data.status;
  } catch (error) {
    alert("Thao tác thất bại!");
  }
}

// LIFECYCLE HOOKS
watch(() => route.params.userId, fetchProfileData, { immediate: true });
</script>

<template>
  <div v-if="isLoadingProfile" class="flex h-64 items-center justify-center">
    <LoadingSpinner class="h-8 w-8" />
  </div>
  <div v-else-if="profileData" class="space-y-6">
    <div class="rounded-lg border bg-card text-card-foreground shadow-sm">
      <div class="group relative h-48 rounded-t-lg bg-muted md:h-64 lg:h-80">
        <img
          v-if="profileData.coverUrl"
          :src="profileData.coverUrl"
          alt="Ảnh bìa"
          class="h-full w-full rounded-t-lg object-cover"
        />
        <div
          v-else
          class="h-full w-full rounded-t-lg bg-gradient-to-r from-sky-400 to-blue-500"
        ></div>
        <Button
          v-if="isMyProfile"
          variant="secondary"
          size="sm"
          class="absolute bottom-4 right-4 opacity-0 transition-opacity group-hover:opacity-100"
        >
          <Camera class="mr-2 h-4 w-4" /> Chỉnh sửa ảnh bìa
        </Button>
      </div>
      <div class="p-4 md:p-6">
        <div class="flex flex-col sm:flex-row sm:items-end -mt-16 md:-mt-20">
          <div class="relative shrink-0">
            <Avatar class="h-32 w-32 border-4 border-card md:h-40 md:w-40">
              <AvatarImage
                :src="profileData.avatarUrl || ''"
                :alt="profileData.fullName || ''"
              />
              <AvatarFallback class="text-5xl md:text-6xl">{{
                profileData.fullName?.charAt(0).toUpperCase() || "U"
              }}</AvatarFallback>
            </Avatar>
            <TooltipProvider v-if="isMyProfile">
              <Tooltip>
                <TooltipTrigger as-child>
                  <Button
                    @click="triggerAvatarUpload"
                    variant="secondary"
                    size="icon"
                    class="absolute bottom-2 right-2 h-9 w-9 rounded-full"
                  >
                    <Camera class="h-5 w-5" />
                  </Button>
                </TooltipTrigger>
                <TooltipContent><p>Thay đổi ảnh đại diện</p></TooltipContent>
              </Tooltip>
            </TooltipProvider>
            <input
              ref="avatarInput"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleAvatarChange"
            />
          </div>
          <div class="mt-3 min-w-0 flex-1 sm:ml-4">
            <h1
              class="truncate text-2xl font-bold md:text-3xl"
              :title="profileData.fullName || ''"
            >
              {{ profileData.fullName || "Chưa cập nhật" }}
            </h1>
            <p v-if="profileData.friendCount" class="text-muted-foreground">
              {{ profileData.friendCount }} bạn bè
            </p>
            <p class="text-muted-foreground sm:hidden">
              {{ profileData.email }}
            </p>
          </div>
          <div class="mt-4 flex gap-2 sm:mt-0 sm:ml-auto">
            <Dialog v-if="isMyProfile" v-model:open="isEditDialogOpen">
              <DialogTrigger as-child>
                <Button variant="outline">Chỉnh sửa trang cá nhân</Button>
              </DialogTrigger>
              <DialogContent class="sm:max-w-[425px]">
                <DialogHeader>
                  <DialogTitle>Chỉnh sửa thông tin</DialogTitle>
                  <DialogDescription
                    >Cập nhật thông tin cá nhân của bạn. Nhấn lưu khi hoàn
                    tất.</DialogDescription
                  >
                </DialogHeader>
                <Form
                  :validation-schema="editProfileSchema"
                  :initial-values="profileData"
                  @submit="handleUpdateProfile"
                  class="grid gap-4 py-4"
                >
                  <div class="grid grid-cols-4 items-center gap-4">
                    <Label for="fullName" class="text-right">Họ và tên</Label>
                    <Field name="fullName" v-slot="{ field }"
                      ><Input id="fullName" v-bind="field" class="col-span-3"
                    /></Field>
                  </div>
                  <div class="grid grid-cols-4 items-center gap-4">
                    <Label for="dateOfBirth" class="text-right"
                      >Ngày sinh</Label
                    >
                    <Field name="dateOfBirth" v-slot="{ field }"
                      ><Input
                        id="dateOfBirth"
                        type="date"
                        v-bind="field"
                        class="col-span-3"
                    /></Field>
                  </div>
                  <div class="grid grid-cols-4 items-center gap-4">
                    <Label for="occupation" class="text-right"
                      >Nghề nghiệp</Label
                    >
                    <Field name="occupation" v-slot="{ field }"
                      ><Input id="occupation" v-bind="field" class="col-span-3"
                    /></Field>
                  </div>
                  <div class="grid grid-cols-4 items-center gap-4">
                    <Label for="address" class="text-right">Nơi sống</Label>
                    <Field name="address" v-slot="{ field }"
                      ><Input id="address" v-bind="field" class="col-span-3"
                    /></Field>
                  </div>
                  <DialogFooter>
                    <Button type="submit" :disabled="userStore.isLoading">
                      {{ userStore.isLoading ? "Đang lưu..." : "Lưu thay đổi" }}
                    </Button>
                  </DialogFooter>
                </Form>
              </DialogContent>
            </Dialog>

            <Button
              v-else-if="friendshipStatus === 'NONE'"
              @click="handleFriendAction"
              ><UserPlus class="mr-2 h-4 w-4" /> Thêm bạn bè</Button
            >
            <Button
              v-else-if="friendshipStatus === 'PENDING_SENT'"
              @click="handleFriendAction"
              variant="secondary"
              ><X class="mr-2 h-4 w-4" /> Hủy lời mời</Button
            >
            <div
              v-else-if="friendshipStatus === 'PENDING_RECEIVED'"
              class="flex gap-2"
            >
              <Button @click="handleFriendAction"
                ><Check class="mr-2 h-4 w-4" /> Chấp nhận</Button
              >
              <Button variant="secondary">Từ chối</Button>
            </div>
            <Button
              v-else-if="friendshipStatus === 'FRIENDS'"
              @click="handleFriendAction"
              variant="secondary"
              ><UserX class="mr-2 h-4 w-4" /> Hủy kết bạn</Button
            >
            <Button v-if="friendshipStatus === 'FRIENDS'" variant="outline"
              ><MessageCircle class="mr-2 h-4 w-4" /> Nhắn tin</Button
            >
          </div>
        </div>
        <hr class="my-4 border-border" />
        <Tabs default-value="posts" class="w-full">
          <TabsList class="grid w-full grid-cols-4">
            <TabsTrigger value="posts"
              ><Grid class="mr-1 h-4 w-4 sm:hidden" />Bài viết</TabsTrigger
            >
            <TabsTrigger value="about"
              ><UserCircle2 class="mr-1 h-4 w-4 sm:hidden" />Giới
              thiệu</TabsTrigger
            >
            <TabsTrigger value="friends"
              ><Users class="mr-1 h-4 w-4 sm:hidden" />Bạn bè</TabsTrigger
            >
            <TabsTrigger value="photos"
              ><Image class="mr-1 h-4 w-4 sm:hidden" />Ảnh</TabsTrigger
            >
          </TabsList>

          <TabsContent value="posts" class="mt-4">
            <div class="grid grid-cols-1 gap-6 md:grid-cols-5">
              <div class="space-y-6 md:col-span-2">
                <div class="rounded-lg border bg-card p-4">
                  <h2 class="mb-4 text-xl font-bold">Giới thiệu</h2>
                  <div class="space-y-2 text-sm">
                    <p>
                      <strong>Nghề nghiệp:</strong>
                      {{ profileData.occupation || "ksksfjs" }}
                    </p>
                    <p>
                      <strong>Nơi sống:</strong>
                      {{ profileData.address || "Hà Nội" }}
                    </p>
                    <p>
                      <strong>Ngày sinh:</strong>
                      {{ profileData.dateOfBirth || "12-12-2001" }}
                    </p>
                  </div>
                </div>
              </div>
              <div class="space-y-6 md:col-span-3">
                <CreatePost v-if="isMyProfile" />
                <div v-if="isLoadingPosts" class="flex justify-center py-8">
                  <LoadingSpinner />
                </div>
                <div v-else-if="userPosts.length > 0" class="space-y-6">
                  <PostCard
                    v-for="post in userPosts"
                    :key="post.id"
                    :post="post"
                  />
                </div>
                <p v-else class="py-8 text-center text-muted-foreground">
                  Chưa có bài viết nào.
                </p>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="about" class="mt-4">
            <Card>
              <CardHeader
                ><CardTitle>Giới thiệu chi tiết</CardTitle></CardHeader
              >
              <CardContent class="space-y-3">
                <div class="flex items-center">
                  <Briefcase class="mr-3 h-5 w-5 text-muted-foreground" />
                  <span
                    >Nghề nghiệp:
                    {{ profileData.occupation || "Chưa cập nhật" }}</span
                  >
                </div>
                <div class="flex items-center">
                  <Home class="mr-3 h-5 w-5 text-muted-foreground" />
                  <span
                    >Sống tại:
                    {{ profileData.address || "Chưa cập nhật" }}</span
                  >
                </div>
                <div class="flex items-center">
                  <Gift class="mr-3 h-5 w-5 text-muted-foreground" />
                  <span
                    >Ngày sinh:
                    {{
                      profileData.dateOfBirth
                        ? new Date(profileData.dateOfBirth).toLocaleDateString(
                            "vi-VN"
                          )
                        : "Chưa cập nhật"
                    }}</span
                  >
                </div>
              </CardContent>
            </Card>
          </TabsContent>
          <TabsContent value="friends" class="mt-4">
            <p class="py-8 text-center text-muted-foreground">
              Tính năng Bạn bè sẽ được cập nhật sớm.
            </p>
          </TabsContent>
          <TabsContent value="photos" class="mt-4">
            <p class="py-8 text-center text-muted-foreground">
              Tính năng Ảnh sẽ được cập nhật sớm.
            </p>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  </div>
  <div v-else class="p-10 text-center text-muted-foreground">
    <p>Không thể tải thông tin người dùng hoặc người dùng không tồn tại.</p>
  </div>
</template>
