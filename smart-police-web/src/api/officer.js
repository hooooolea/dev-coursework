import request from '@/utils/request'

export const officerApi = {
  list:            (params)      => request.get('/officer/list', { params }),
  detail:          (id)          => request.get(`/officer/${id}`),
  create:          (data)        => request.post('/officer', data),
  update:          (id, data)    => request.put(`/officer/${id}`, data),
  updateStatus:    (id, workStatus) => request.put(`/officer/${id}/status`, { workStatus }),
  del:             (id)          => request.delete(`/officer/${id}`),
  listAssessments: (id)          => request.get(`/officer/${id}/assessments`),
  addAssessment:   (id, data)    => request.post(`/officer/${id}/assessments`, data),
  delAssessment:   (aid)         => request.delete(`/officer/assessments/${aid}`)
}
