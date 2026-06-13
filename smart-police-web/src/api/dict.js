import request from '@/utils/request'

const cache = {}

export const dictApi = {
  get: async (type) => {
    if (cache[type]) return cache[type]
    const res = await request.get(`/dict/${type}`)
    cache[type] = res.data || []
    return cache[type]
  }
}
