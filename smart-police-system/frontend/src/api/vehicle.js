import request from '@/utils/request'

export const vehicleApi = {
  list:           (params)   => request.get('/vehicle/list', { params }),
  detail:         (id)       => request.get(`/vehicle/${id}`),
  searchByPlate:  (plate)    => request.get('/vehicle/search', { params: { plate } }),
  create:         (data)     => request.post('/vehicle', data),
  update:         (id, data) => request.put(`/vehicle/${id}`, data),
  control:        (id, data) => request.post(`/vehicle/${id}/control`, data),
  decontrol:      (id, reason) => request.put(`/vehicle/${id}/decontrol`, { reason }),
  del:            (id)       => request.delete(`/vehicle/${id}`),
  listViolations: (id)       => request.get(`/vehicle/${id}/violations`),
  addViolation:   (id, data) => request.post(`/vehicle/${id}/violations`, data),
  markViolationPaid: (vId)   => request.put(`/vehicle/violations/${vId}/pay`)
}
