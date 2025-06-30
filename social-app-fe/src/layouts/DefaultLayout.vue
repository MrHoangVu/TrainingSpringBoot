<script setup lang="ts">
import Navbar from "@/components/layout/Navbar.vue";
import { useUserStore } from "@/stores/user";
import { computed, ref, onMounted, watch } from "vue";
import friendApi from "@/api/friendApi";
import type { UserProfile } from "@/types/api";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import LoadingSpinner from "@/components/ui/LoadingSpinner.vue";
import {
  Users,
  Clock,
  Youtube,
  Store,
  ChevronDown,
  Flag,
  CalendarDays,
  Bookmark,
  Users2,
} from "lucide-vue-next";

const userStore = useUserStore();
const currentUser = computed(() => userStore.currentUser);

const mainMenuItems = [
  {
    name: "Bạn bè",
    icon: Users,
    to: { name: "Friends" },
    activeFor: "Friends",
  },
  {
    name: "Feeds",
    icon: Clock,
    to: { name: "Home" },
    exact: true,
    activeFor: "Home",
  },
  { name: "Watch", icon: Youtube, to: { name: "Home" } },
  { name: "Marketplace", icon: Store, to: { name: "Home" } },
  { name: "Nhóm", icon: Users2, to: { name: "Home" } },
];

const shortcutItems = [
  { name: "Sự kiện", icon: CalendarDays, to: { name: "Home" } },
  { name: "Kỷ niệm", icon: Clock, to: { name: "Home" } },
  { name: "Đã lưu", icon: Bookmark, to: { name: "Home" } },
  { name: "Trang", icon: Flag, to: { name: "Home" } },
];

const realFriendsList = ref<UserProfile[]>([]);
const isLoadingFriendsSidebar = ref(true);

async function fetchFriendsForSidebar() {
  isLoadingFriendsSidebar.value = true;
  try {
    const response = await friendApi.getList();
    realFriendsList.value = response.data;
  } catch (error) {
    console.error("Failed to fetch friends for sidebar:", error);
    realFriendsList.value = [];
  } finally {
    isLoadingFriendsSidebar.value = false;
  }
}

onMounted(() => {
  if (userStore.currentUser) {
    fetchFriendsForSidebar();
  }
});

watch(currentUser, (newUser) => {
  if (newUser) {
    fetchFriendsForSidebar();
  } else {
    realFriendsList.value = [];
  }
});

const getAvatarFallback = (name?: string | null) => {
  return name ? name.charAt(0).toUpperCase() : "?";
};
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <Navbar />
    <div class="flex pt-16">
      <!-- === MENU TRÁI (LEFT SIDEBAR) === -->
      <aside
        class="fixed left-0 top-16 z-30 hidden h-[calc(100vh-4rem)] w-72 shrink-0 overflow-y-auto border-r bg-card p-3 md:sticky md:block"
      >
        <nav class="flex flex-col gap-0.5">
          <router-link
            v-if="currentUser"
            :to="{ name: 'Profile', params: { userId: currentUser.id } }"
            class="flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium text-card-foreground transition-all hover:bg-accent hover:text-accent-foreground"
            active-class="bg-accent text-accent-foreground"
          >
            <Avatar class="h-7 w-7">
              <AvatarImage
                :src="currentUser.avatarUrl || ''"
                :alt="currentUser.fullName || ''"
              />
              <AvatarFallback>{{
                getAvatarFallback(currentUser.fullName)
              }}</AvatarFallback>
            </Avatar>
            <span>{{ currentUser.fullName || "Người dùng" }}</span>
          </router-link>

          <router-link
            v-for="item in mainMenuItems"
            :key="item.name"
            :to="item.to"
            class="flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium text-card-foreground transition-all hover:bg-accent hover:text-accent-foreground"
            :active-class="
              item.activeFor && $route.name === item.activeFor
                ? 'bg-accent text-accent-foreground'
                : ''
            "
            :exact-active-class="
              item.exact ? 'bg-accent text-accent-foreground' : ''
            "
          >
            <component :is="item.icon" class="h-5 w-5 text-primary" />
            {{ item.name }}
          </router-link>

          <Button
            variant="ghost"
            class="flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium text-card-foreground justify-start hover:bg-accent hover:text-accent-foreground"
          >
            <div class="rounded-full bg-muted p-1">
              <ChevronDown class="h-4 w-4" />
            </div>
            Xem thêm
          </Button>

          <hr class="my-3 border-border" />
          <p
            class="px-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground"
          >
            Lối tắt của bạn
          </p>

          <router-link
            v-for="item in shortcutItems"
            :key="item.name"
            :to="item.to"
            class="flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium text-card-foreground transition-all hover:bg-accent hover:text-accent-foreground"
          >
            <component :is="item.icon" class="h-5 w-5" />
            {{ item.name }}
          </router-link>
        </nav>
      </aside>

      <!-- === NỘI DUNG CHÍNH (MAIN CONTENT)=== -->
      <main
        class="mx-auto flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 xl:max-w-6xl"
      >
        <router-view />
      </main>

      <!-- === MENU PHẢI (RIGHT SIDEBAR) === -->
      <aside
        class="fixed right-0 top-16 z-30 hidden h-[calc(100vh-4rem)] w-72 shrink-0 overflow-y-auto border-l bg-card p-3 lg:sticky lg:block"
      >
        <div class="sticky top-0 bg-card py-2">
          <h3
            class="mb-2 px-1 text-sm font-semibold uppercase tracking-wider text-muted-foreground"
          >
            Người liên hệ
          </h3>
        </div>
        <div
          v-if="isLoadingFriendsSidebar"
          class="flex h-32 items-center justify-center"
        >
          <LoadingSpinner />
        </div>
        <nav
          v-else-if="realFriendsList.length > 0"
          class="flex flex-col gap-0.5"
        >
          <router-link
            v-for="friend in realFriendsList"
            :key="friend.id"
            :to="{ name: 'Profile', params: { userId: friend.id } }"
            class="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium text-card-foreground transition-all hover:bg-accent hover:text-accent-foreground"
          >
            <div class="relative">
              <Avatar class="h-7 w-7">
                <AvatarImage
                  :src="friend.avatarUrl || ''"
                  :alt="friend.fullName || ''"
                />
                <AvatarFallback>{{
                  getAvatarFallback(friend.fullName)
                }}</AvatarFallback>
              </Avatar>
              <span
                class="absolute bottom-0 right-0 block h-2 w-2 rounded-full bg-green-500 ring-1 ring-card"
              />
            </div>
            <span class="truncate" :title="friend.fullName || ''">{{
              friend.fullName
            }}</span>
          </router-link>
        </nav>
        <p v-else class="px-3 py-4 text-center text-xs text-muted-foreground">
          Chưa có bạn bè nào.
        </p>
      </aside>
    </div>
  </div>
</template>
