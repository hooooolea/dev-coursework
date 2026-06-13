import request from '@/utils/request'

export const patrolApi = {
  // 巡逻任务
  list:     (params)         => request.get('/patrol/task/list', { params }),
  create:   (data)           => request.post('/patrol/task', data),
  accept:   (id)             => request.put(`/patrol/task/${id}/accept`),
  checkin:  (id, type, note) => request.post(`/patrol/task/${id}/checkin`, { type, note }),
  checkins: (id)             => request.get(`/patrol/task/${id}/checkins`),
  del:      (id)             => request.delete(`/patrol/task/${id}`),
  complete: (id, summary)    => request.put(`/patrol/task/${id}/complete`, { summary }),

  // 排班管理
  scheduleWeek:   (weekOffset = 0) => request.get('/patrol/schedule/week', { params: { weekOffset } }),
  scheduleCreate: (data)           => request.post('/patrol/schedule', data),
  scheduleBatch:  (list)           => request.post('/patrol/schedule/batch', list),
  scheduleStatus: (id, status, remark) => request.put(`/patrol/schedule/${id}/status`, { status, remark }),
  scheduleDel:    (id)             => request.delete(`/patrol/schedule/${id}`)
}
