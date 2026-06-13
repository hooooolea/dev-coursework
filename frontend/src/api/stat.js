import request from '@/utils/request'
import axios from 'axios'

export const statApi = {
  caseStats:    (months = 6) => request.get('/stat/case', { params: { months } }),
  officerStats: ()           => request.get('/stat/officer'),

  /** 带 token 下载 Excel（直接用 axios，绕过业务响应拦截器） */
  async exportExcel(reportType) {
    const token = localStorage.getItem('accessToken')
    const res = await axios.get(`/api/stat/export`, {
      params: { reportType },
      responseType: 'blob',
      headers: { Authorization: `Bearer ${token}` }
    })
    const disposition = res.headers?.['content-disposition'] || ''
    let fileName = `${reportType}_report.xlsx`
    const match = disposition.match(/filename\*=UTF-8''(.+)/i)
    if (match) fileName = decodeURIComponent(match[1])
    const url = URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url; a.download = fileName; a.click()
    URL.revokeObjectURL(url)
  }
}
