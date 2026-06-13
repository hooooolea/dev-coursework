import request from '@/utils/request'

export const caseApi = {
  list:         (params)         => request.get('/case/list', { params }),
  detail:       (id)             => request.get(`/case/${id}`),
  create:       (data)           => request.post('/case', data),
  update:       (id, data)       => request.put(`/case/${id}`, data),
  updateStatus: (id, status, reason) => request.put(`/case/${id}/status`, { status, reason }),
  listProgress: (id)             => request.get(`/case/${id}/progress`),
  addProgress:  (id, data)       => request.post(`/case/${id}/progress`, data),
  del:          (id)             => request.delete(`/case/${id}`),
  listEvidence: (id)             => request.get(`/case/${id}/evidence`),
  addEvidence:  (id, data)       => request.post(`/case/${id}/evidence`, data),
  /**
   * 上传证据文件（multipart）。返回 { fileUrl, fileName, fileSize, fileType, contentType, relativePath }
   */
  uploadEvidence: (id, file) => {
    const fd = new FormData()
    fd.append('file', file)
    return request.post(`/case/${id}/evidence/upload`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  delEvidence:  (evidenceId)     => request.delete(`/case/evidence/${evidenceId}`),
  listSuspect:  (id)             => request.get(`/case/${id}/suspect`),
  addSuspect:   (id, data)       => request.post(`/case/${id}/suspect`, data),
  updateSuspect:(id, suspectId, data) => request.put(`/case/${id}/suspect/${suspectId}`, data),
  delSuspect:   (id, suspectId)  => request.delete(`/case/${id}/suspect/${suspectId}`)
}
