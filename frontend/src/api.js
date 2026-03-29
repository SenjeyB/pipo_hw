import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Auth
export const register = (data) => api.post('/auth/register', data)
export const login = (data) => api.post('/auth/login', data)
export const getMe = () => api.get('/auth/me')

// Intervals
export const createInterval = (data) => api.post('/intervals', data)
export const getMyIntervals = () => api.get('/intervals/my')
export const getAvailableIntervals = () => api.get('/intervals/available')
export const getInterval = (id) => api.get(`/intervals/${id}`)
export const updateInterval = (id, data) => api.put(`/intervals/${id}`, data)
export const deleteInterval = (id) => api.delete(`/intervals/${id}`)
export const bookInterval = (id) => api.post(`/intervals/${id}/book`)
export const cancelBooking = (id) => api.delete(`/intervals/${id}/book`)

export default api
