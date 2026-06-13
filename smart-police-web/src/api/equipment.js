import request from '@/utils/request'

export const equipmentApi = {
  list:         (params)      => request.get('/equipment/list', { params }),
  create:       (data)        => request.post('/equipment', data),
  update:       (id, data)    => request.put(`/equipment/${id}`, data),
  del:          (id)          => request.delete(`/equipment/${id}`),
  borrows:      (id)          => request.get(`/equipment/${id}/borrows`),
  borrow:       (id, data)    => request.post(`/equipment/${id}/borrow`, data),
  returnEquip:  (borrowId, note) => request.put(`/equipment/borrow/${borrowId}/return`, { returnNote: note })
}
