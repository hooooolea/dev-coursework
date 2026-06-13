import request from '@/utils/request'

export const alarmApi = {
  list: (params) => request.get('/alarm/list', { params }),
  detail: (id) => request.get(`/alarm/${id}`),
  create: (data) => request.post('/alarm', data),
  dispatch: (id, officerId) => request.put(`/alarm/${id}/dispatch`, { officerId }),
  arrive: (dispatchId) => request.put(`/alarm/dispatch/${dispatchId}/arrive`),
  close: (id, summary) => request.put(`/alarm/${id}/close`, { summary })
}
