import apiClient from './axios'

export default {
    getWeeklySummary() {
        return apiClient.get('/reports/weekly-summary', {
            responseType: 'blob',
        })
    },
}