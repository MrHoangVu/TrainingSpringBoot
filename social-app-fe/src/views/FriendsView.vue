<script setup lang="ts">
import { ref, onMounted } from "vue";
import type { UserProfile } from "@/types/api";
import friendApi from "@/api/friendApi";

// Import components
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import LoadingSpinner from "@/components/ui/LoadingSpinner.vue";

const friends = ref<UserProfile[]>([]);
const pendingRequests = ref<UserProfile[]>([]);
const isLoadingFriends = ref(true);
const isLoadingRequests = ref(true);

// Hàm tải danh sách bạn bè
async function fetchFriends() {
  isLoadingFriends.value = true;
  try {
    const response = await friendApi.getList();
    friends.value = response.data;
  } catch (error) {
    console.error("Failed to fetch friends:", error);
  } finally {
    isLoadingFriends.value = false;
  }
}

// Hàm tải danh sách lời mời
async function fetchPendingRequests() {
  isLoadingRequests.value = true;
  try {
    const response = await friendApi.getPendingRequests();
    pendingRequests.value = response.data;
  } catch (error) {
    console.error("Failed to fetch pending requests:", error);
  } finally {
    isLoadingRequests.value = false;
  }
}

// Hàm xử lý khi chấp nhận lời mời
async function handleAccept(requesterId: number) {
  try {
    await friendApi.acceptRequest(requesterId);
    // Tải lại cả 2 danh sách
    fetchFriends();
    fetchPendingRequests();
  } catch (error) {
    alert("Thao tác thất bại");
  }
}

// Hàm xử lý khi từ chối lời mời
async function handleDecline(requesterId: number) {
  try {
    await friendApi.declineRequest(requesterId);
    // Chỉ cần tải lại danh sách lời mời
    fetchPendingRequests();
  } catch (error) {
    alert("Thao tác thất bại");
  }
}

// Hàm xử lý khi hủy kết bạn
async function handleUnfriend(friendId: number) {
  if (confirm("Bạn có chắc chắn muốn hủy kết bạn?")) {
    try {
      await friendApi.unfriend(friendId);
      // Chỉ cần tải lại danh sách bạn bè
      fetchFriends();
    } catch (error) {
      alert("Thao tác thất bại");
    }
  }
}

onMounted(() => {
  fetchFriends();
  fetchPendingRequests();
});
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle>Bạn bè</CardTitle>
    </CardHeader>
    <CardContent>
      <Tabs default-value="all-friends" class="w-full">
        <TabsList class="grid w-full grid-cols-2">
          <TabsTrigger value="all-friends">Tất cả bạn bè</TabsTrigger>
          <TabsTrigger value="requests">
            Lời mời kết bạn
            <span
              v-if="pendingRequests.length > 0"
              class="ml-2 bg-primary text-primary-foreground h-5 w-5 text-xs rounded-full flex items-center justify-center"
            >
              {{ pendingRequests.length }}
            </span>
          </TabsTrigger>
        </TabsList>

        <!-- Tab Tất cả bạn bè -->
        <TabsContent value="all-friends" class="mt-4">
          <div v-if="isLoadingFriends" class="flex justify-center p-8">
            <LoadingSpinner />
          </div>
          <div
            v-else-if="friends.length > 0"
            class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4"
          >
            <Card v-for="friend in friends" :key="friend.id">
              <CardContent class="p-4 flex flex-col items-center text-center">
                <router-link
                  :to="{ name: 'Profile', params: { userId: friend.id } }"
                >
                  <Avatar class="h-20 w-20 mb-2">
                    <AvatarImage :src="friend.avatarUrl || ''" />
                    <AvatarFallback>{{
                      friend.fullName?.charAt(0)
                    }}</AvatarFallback>
                  </Avatar>
                  <p class="font-semibold">{{ friend.fullName }}</p>
                </router-link>
                <Button
                  @click="handleUnfriend(friend.id)"
                  variant="secondary"
                  class="mt-2 w-full"
                  >Hủy kết bạn</Button
                >
              </CardContent>
            </Card>
          </div>
          <p v-else class="text-center text-muted-foreground p-8">
            Bạn chưa có người bạn nào.
          </p>
        </TabsContent>

        <!-- Tab Lời mời kết bạn -->
        <TabsContent value="requests" class="mt-4">
          <div v-if="isLoadingRequests" class="flex justify-center p-8">
            <LoadingSpinner />
          </div>
          <div
            v-else-if="pendingRequests.length > 0"
            class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4"
          >
            <Card v-for="request in pendingRequests" :key="request.id">
              <CardContent class="p-4 flex flex-col items-center text-center">
                <router-link
                  :to="{ name: 'Profile', params: { userId: request.id } }"
                >
                  <Avatar class="h-20 w-20 mb-2">
                    <AvatarImage :src="request.avatarUrl || ''" />
                    <AvatarFallback>{{
                      request.fullName?.charAt(0)
                    }}</AvatarFallback>
                  </Avatar>
                  <p class="font-semibold">{{ request.fullName }}</p>
                </router-link>
                <div class="mt-2 w-full space-y-2">
                  <Button @click="handleAccept(request.id)" class="w-full"
                    >Chấp nhận</Button
                  >
                  <Button
                    @click="handleDecline(request.id)"
                    variant="secondary"
                    class="w-full"
                    >Từ chối</Button
                  >
                </div>
              </CardContent>
            </Card>
          </div>
          <p v-else class="text-center text-muted-foreground p-8">
            Bạn không có lời mời kết bạn nào.
          </p>
        </TabsContent>
      </Tabs>
    </CardContent>
  </Card>
</template>
