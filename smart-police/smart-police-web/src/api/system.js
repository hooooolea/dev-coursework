import request from '@/utils/request'

export const userApi = {
  list:          (params)       => request.get('/user/list', { params }),
  detail:        (id)           => request.get(`/user/${id}`),
  create:        (data)         => request.post('/user', data),
  update:        (id, data)     => request.put(`/user/${id}`, data),
  del:           (id)           => request.delete(`/user/${id}`),
  toggleStatus:  (id, status)   => request.put(`/user/${id}/status`, { status }),
  resetPassword: (id, password) => request.put(`/user/${id}/reset-password`, { password }),
  getUserRoles:  (id)           => request.get(`/user/${id}/roles`),
  assignRoles:   (id, roleIds)  => request.put(`/user/${id}/roles`, roleIds),
  allRoles:      ()             => request.get('/user/roles/all')
}

export const roleApi = {
  list:        ()         => request.get('/role/list'),
  create:      (data)     => request.post('/role', data),
  update:      (id, data) => request.put(`/role/${id}`, data),
  del:         (id)       => request.delete(`/role/${id}`),
  getPerms:    (id)       => request.get(`/role/${id}/perms`),
  assignPerms: (id, ids)  => request.put(`/role/${id}/perms`, ids),
  permTree:    ()         => request.get('/role/perm-tree')
}

export const deptApi = {
  tree:   ()         => request.get('/dept/tree'),
  list:   ()         => request.get('/dept/list'),
  create: (data)     => request.post('/dept', data),
  update: (id, data) => request.put(`/dept/${id}`, data),
  del:    (id)       => request.delete(`/dept/${id}`)
}

export const logApi = {
  list:   (params) => request.get('/operation-log/list', { params }),
  export: (params) => request.get('/operation-log/export', { params, responseType: 'blob' })
}
