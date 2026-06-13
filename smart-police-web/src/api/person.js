import request from '@/utils/request'

export const personApi = {
  list: (params) => request.get('/person/list', { params }),
  detail: (id) => request.get(`/person/${id}`),
  create: (data) => request.post('/person', data),
  update: (id, data) => request.put(`/person/${id}`, data),
  label: (id, type, reason) => request.post(`/person/${id}/label`, { type, reason }),
  violations: (id) => request.get(`/person/${id}/violations`),
  addViolation: (id, data) => request.post(`/person/${id}/violations`, data),
  /**
   * 上传人员头像（multipart）。返回 { photoUrl, fileName, fileSize, fileType }
   */
  uploadAvatar: (id, file) => {
    const fd = new FormData()
    fd.append('file', file)
    return request.post(`/person/${id}/avatar`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  del: (id) => request.delete(`/person/${id}`)
}
