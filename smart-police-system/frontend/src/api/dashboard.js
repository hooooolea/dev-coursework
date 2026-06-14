import request from '@/utils/request'

export const dashboardApi = {
  stats: () => request.get('/dashboard/stats')
}
