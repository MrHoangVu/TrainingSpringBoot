import { defineStore } from 'pinia'
import { ref } from 'vue'
import postApi from '@/api/postApi'
import type { Post } from '@/types/api'
import commentApi from '@/api/commentApi'
import type { Comment } from '@/types/api'

export const usePostStore = defineStore('post', () => {
    // STATE
    const posts = ref<Post[]>([])
    const isLoading = ref(false)
    const page = ref(0)
    const totalPages = ref(1)

    // ACTIONS
    async function createPost(content: string | null, image: File | null) {
        isLoading.value = true
        try {
            const response = await postApi.createPost(content, image)
            // Thêm bài viết mới vào đầu danh sách
            posts.value.unshift(response.data)
        } catch (error) {
            console.error('Failed to create post:', error)
            throw error
        } finally {
            isLoading.value = false
        }
    }


    async function fetchTimeline() {
        // Ngừng fetch nếu đang loading hoặc đã hết trang
        if (isLoading.value || page.value >= totalPages.value) return

        isLoading.value = true
        try {
            const response = await postApi.getTimeline(page.value, 10) // Lấy 10 bài mỗi lần
            const newPosts = response.data.content

            // Nối các bài viết mới vào danh sách hiện tại
            posts.value.push(...newPosts)

            // Cập nhật thông tin phân trang
            page.value++
            totalPages.value = response.data.totalPages
        } catch (error) {
            console.error('Failed to fetch timeline:', error)
        } finally {
            isLoading.value = false
        }
    }

    function resetTimeline() {
        posts.value = []
        page.value = 0
        totalPages.value = 1
    }

    async function toggleLike(postId: number) {
        // 1. Tìm bài viết trong store
        const post = posts.value.find(p => p.id === postId)
        if (!post) return

        // 2. Optimistic Update: Cập nhật giao diện ngay lập tức
        const originalIsLiked = post.isLiked
        const originalLikeCount = post.likeCount

        if (post.isLiked) {
            post.isLiked = false
            post.likeCount--
        } else {
            post.isLiked = true
            post.likeCount++
        }

        // 3. Gọi API
        try {
            await postApi.toggleLike(postId)
            // API thành công, không cần làm gì thêm vì giao diện đã được cập nhật
        } catch (error) {
            // 4. Nếu API thất bại, hoàn tác lại thay đổi trên giao diện
            console.error('Failed to toggle like:', error)
            post.isLiked = originalIsLiked
            post.likeCount = originalLikeCount
            alert('Thao tác thất bại, vui lòng thử lại.')
        }
    }
    async function createComment(postId: number, content: string) {
        try {
            const response = await commentApi.createComment(postId, content)
            const newComment = response.data

            // Tìm bài viết và cập nhật số lượng bình luận
            const post = posts.value.find(p => p.id === postId)
            if (post) {
                post.commentCount++
                // Có thể thêm logic để đẩy bình luận mới vào danh sách nếu đang hiển thị
            }
            return newComment // Trả về bình luận mới để component cập nhật UI
        } catch (error) {
            console.error('Failed to create comment:', error)
            throw error
        }
    }
    async function updatePost(postId: number, content: string) {
        try {
            const response = await postApi.updatePost(postId, content)
            // Tìm và cập nhật bài viết trong store
            const index = posts.value.findIndex(p => p.id === postId)
            if (index !== -1) {
                posts.value[index] = response.data
            }
        } catch (error) {
            console.error('Failed to update post:', error)
            throw error
        }
    }

    async function deletePost(postId: number) {
        try {
            await postApi.deletePost(postId)
            // Xóa bài viết khỏi danh sách trong store
            posts.value = posts.value.filter(p => p.id !== postId)
        } catch (error) {
            console.error('Failed to delete post:', error)
            throw error
        }
    }

    // Action này chỉ để giảm số lượng comment trên UI
    function decrementCommentCount(postId: number) {
        const post = posts.value.find(p => p.id === postId)
        if (post && post.commentCount > 0) {
            post.commentCount--
        }
    }
    return {
        posts,
        isLoading,
        page,
        totalPages,
        createPost,
        fetchTimeline,
        resetTimeline,
        toggleLike,
        createComment,
        updatePost,
        deletePost,
        decrementCommentCount,
    }
})