import apiClient from './axios'
import type { Comment, Page } from '@/types/api'

export default {
    getComments(postId: number, page: number, size: number) {
        return apiClient.get<Page<Comment>>(`/posts/${postId}/comments`, {
            params: { page, size },
        })
    },
    createComment(postId: number, content: string) {
        return apiClient.post<Comment>(`/posts/${postId}/comments`, { content })
    },
    // THÊM 2 HÀM NÀY
    updateComment(commentId: number, content: string) {
        return apiClient.put<Comment>(`/comments/${commentId}`, { content })
    },
    deleteComment(commentId: number) {
        return apiClient.delete(`/comments/${commentId}`)
    },
}