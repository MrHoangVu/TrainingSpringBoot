<script setup lang="ts">
import { computed } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useUserStore } from "@/stores/user";
import { useRouter } from "vue-router";
import reportApi from "@/api/reportApi";

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import {
  Home,
  Users,
  MessageSquare,
  Search,
  LogOut,
  Settings,
  Download,
} from "lucide-vue-next";

const authStore = useAuthStore();
const userStore = useUserStore();
const router = useRouter();

const currentUser = computed(() => userStore.currentUser);

function handleLogout() {
  authStore.logout();
}

function goToProfile() {
  if (currentUser.value) {
    router.push({ name: "Profile", params: { userId: currentUser.value.id } });
  }
}

const getAvatarFallback = (name?: string | null) => {
  return name ? name.charAt(0).toUpperCase() : "?";
};

async function handleDownloadReport() {
  try {
    const response = await reportApi.getWeeklySummary();

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;

    const contentDisposition = response.headers["content-disposition"];
    let fileName = "weekly_report.xlsx";
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename="(.+)"/);
      if (fileNameMatch && fileNameMatch.length === 2) {
        fileName = fileNameMatch[1];
      }
    }

    link.setAttribute("download", fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("Failed to download report:", error);
    alert("Tải báo cáo thất bại!");
  }
}
</script>

<template>
  <header
    class="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60"
  >
    <div class="container flex h-16 items-center justify-between">
      <div class="flex items-center gap-4">
        <router-link :to="{ name: 'Home' }" class="flex items-center space-x-2">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 256 256"
            class="h-8 w-8 text-primary"
          >
            <rect width="256" height="256" fill="none"></rect>
            <circle
              cx="128"
              cy="128"
              r="96"
              fill="none"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="16"
            ></circle>
            <circle
              cx="128"
              cy="128"
              r="32"
              fill="none"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="16"
            ></circle>
            <line
              x1="128"
              y1="96"
              x2="128"
              y2="68"
              fill="none"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="16"
            ></line>
            <line
              x1="96"
              y1="128"
              x2="68"
              y2="128"
              fill="none"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="16"
            ></line>
            <line
              x1="128"
              y1="160"
              x2="128"
              y2="188"
              fill="none"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="16"
            ></line>
            <line
              x1="160"
              y1="128"
              x2="188"
              y2="128"
              fill="none"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="16"
            ></line>
          </svg>
          <span class="hidden font-bold text-lg sm:inline-block"
            >fakebook</span
          >
        </router-link>
        <div class="relative hidden md:block">
          <Search
            class="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground"
          />
          <Input placeholder="Tìm kiếm..." class="w-40 pl-8 lg:w-64" />
        </div>
      </div>
  
      <nav class="hidden items-center gap-2 md:flex">
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger as-child>
              <Button
                variant="ghost"
                size="icon"
                class="h-12 w-24 rounded-lg"
                as-child
              >
                <router-link
                  :to="{ name: 'Home' }"
                  active-class="bg-accent text-accent-foreground"
                >
                  <Home class="h-6 w-6" />
                </router-link>
              </Button>
            </TooltipTrigger>
            <TooltipContent>
              <p>Trang chủ</p>
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger as-child>
              <Button
                variant="ghost"
                size="icon"
                class="h-12 w-24 rounded-lg"
                as-child
              >
                <router-link
                  :to="{ name: 'Friends' }"
                  active-class="bg-accent text-accent-foreground"
                >
                  <Users class="h-6 w-6" />
                </router-link>
              </Button>
            </TooltipTrigger>
            <TooltipContent>
              <p>Bạn bè</p>
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      </nav>

      <div class="flex items-center gap-2">
        <Button variant="ghost" size="icon" class="rounded-full">
          <MessageSquare class="h-5 w-5" />
        </Button>

        <DropdownMenu v-if="currentUser">
          <DropdownMenuTrigger as-child>
            <Button variant="ghost" class="relative h-10 w-10 rounded-full">
              <Avatar class="h-10 w-10">
                <AvatarImage
                  :src="currentUser.avatarUrl || ''"
                  :alt="currentUser.fullName || 'User Avatar'"
                />
                <AvatarFallback>{{
                  getAvatarFallback(currentUser.fullName)
                }}</AvatarFallback>
              </Avatar>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent class="w-56" align="end">
            <DropdownMenuLabel class="font-normal" as-child>
              <div
                class="flex cursor-pointer items-center gap-3 rounded-md p-2 hover:bg-accent"
                @click="goToProfile"
              >
                <Avatar class="h-10 w-10">
                  <AvatarImage
                    :src="currentUser.avatarUrl || ''"
                    :alt="currentUser.fullName || 'User Avatar'"
                  />
                  <AvatarFallback>{{
                    getAvatarFallback(currentUser.fullName)
                  }}</AvatarFallback>
                </Avatar>
                <div class="flex flex-col space-y-1">
                  <p class="text-sm font-medium leading-none">
                    {{ currentUser.fullName || "Người dùng" }}
                  </p>
                  <p class="text-xs leading-none text-muted-foreground">
                    Xem trang cá nhân
                  </p>
                </div>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem>
              <Settings class="mr-2 h-4 w-4" />
              <span>Cài đặt</span>
            </DropdownMenuItem>
            <DropdownMenuItem @click="handleDownloadReport">
              <Download class="mr-2 h-4 w-4" />
              <span>Tải báo cáo tuần</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem @click="handleLogout">
              <LogOut class="mr-2 h-4 w-4" />
              <span>Đăng xuất</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  </header>
</template>
