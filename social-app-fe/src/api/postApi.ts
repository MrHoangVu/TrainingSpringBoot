import apiClient from './axios'
import type { Post, Page } from '@/types/api'

export default {
    createPost(content: string | null, image: File | null) {
        const formData = new FormData()
        if (content) {
            formData.append('content', content)
        }
        if (image) {
            formData.append('image', image)
        }
        return apiClient.post<Post>('/posts', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        })
    },
    getTimeline(page: number, size: number) {
        return apiClient.get<Page<Post>>('/timeline', {
            params: {
                page,
                size,
            },
        })
    },

    toggleLike(postId: number) {
        return apiClient.post<{ isLiked: boolean; likeCount: number }>(`/posts/${postId}/like`)
    },

    updatePost(postId: number, content: string) {
        return apiClient.put<Post>(`/posts/${postId}`, { content })
    },
    deletePost(postId: number) {
        return apiClient.delete(`/posts/${postId}`)
    },

}