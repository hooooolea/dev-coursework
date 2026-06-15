import request from '@/utils/request'

export const alarmApi = {
  list: (params) => request.get('/alarm/list', { params }),
  detail: (id) => request.get(`/alarm/${id}`),
  create: (data) => request.post('/alarm', data),
  myTasks: (params) => request.get('/alarm/my-tasks', { params }),
  equipmentRecommend: (id) => request.get(`/alarm/${id}/equipment-recommend`, { timeout: 60000 }),
  dispatch: (id, officerId) => request.put(`/alarm/${id}/dispatch`, { officerId }),
  arrive: (dispatchId) => request.put(`/alarm/dispatch/${dispatchId}/arrive`),
  arriveByAlarm: (alarmId) => request.put(`/alarm/${alarmId}/arrive`),
  close: (id, summary) => request.put(`/alarm/${id}/close`, { summary }),
  linkCase: (id, caseId) => request.put(`/alarm/${id}/link-case`, { caseId })
}
