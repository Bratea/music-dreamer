import request from '../utils/request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  getUserInfo: () => request.get('/auth/user/info'),
  refreshToken: (data) => request.post('/auth/refresh', data),
}

export const singerApi = {
  apply: (data) => request.post('/singer/apply', data),
  getMyApply: () => request.get('/singer/apply/my'),
  isSinger: () => request.get('/singer/manage/is-singer'),
  getPendingApplies: () => request.get('/singer/admin/applies'),
  approveApply: (id) => request.put(`/singer/admin/apply/${id}/approve`),
  rejectApply: (id, reason) => request.put(`/singer/admin/apply/${id}/reject`, null, { params: { reason } }),
}

export const songApi = {
  getById: (id) => request.get(`/song/${id}`),
  search: (params) => request.get('/search', { params }),
  getHot: (page = 1, size = 10) => request.get('/song/hot', { params: { page, size } }),
  getNew: (page = 1, size = 10) => request.get('/song/new', { params: { page, size } }),
  getSimilar: (id, size = 10) => request.get(`/song/${id}/similar`, { params: { size } }),
  play: (songId) => request.post(`/song/${songId}/play`),
  publish: (data) => request.post('/singer/song/publish', data),
  mySongs: (status) => request.get('/singer/song/my', { params: { status } }),
  editSong: (data) => request.put(`/singer/song/${data.songId}/edit`, data),
  deleteSong: (id) => request.delete(`/singer/song/${id}`),
  offlineSong: (id) => request.put(`/singer/song/${id}/offline`),
  reSubmitSong: (id) => request.put(`/singer/song/${id}/online`),
}

/** 用户交互接口（由 playlist-service 承载，网关路由 /api/me/**） */
export const meApi = {
  getCollections: (targetType) => request.get('/me/collections', { params: { targetType } }),
  collect: (targetId, targetType = 1) => request.post('/me/collect', { targetId, targetType }),
  uncollect: (targetId, targetType = 1) => request.delete(`/me/collect/${targetId}`, { params: { targetType } }),
  isCollected: (targetId, targetType = 1) => request.get(`/me/collect/${targetId}`, { params: { targetType } }),
  getHistory: (limit = 50) => request.get('/me/history', { params: { limit } }),
  clearHistory: () => request.delete('/me/history'),
  getNotifications: () => request.get('/me/notifications'),
  markNotificationRead: (id) => request.put(`/me/notification/${id}/read`),
  followSinger: (singerId) => request.post('/me/follow', { singerId }),
  unfollowSinger: (singerId) => request.delete(`/me/follow/${singerId}`),
  getFollowing: () => request.get('/me/following'),
  isFollowing: (singerId) => request.get(`/me/follow/${singerId}`),
}

export const playlistApi = {
  getList: (params) => request.get('/playlist', { params }),
  getById: (id) => request.get(`/playlist/${id}`),
  create: (data) => request.post('/playlist', data),
  update: (data) => request.put('/playlist', data),
  delete: (id) => request.delete(`/playlist/${id}`),
  addSong: (playlistId, songId) => request.post(`/playlist/${playlistId}/song/${songId}`),
  removeSong: (playlistId, songId) => request.delete(`/playlist/${playlistId}/song/${songId}`),
  getHot: (page = 1, size = 10) => request.get('/playlist/hot', { params: { page, size } }),
  getUserPlaylists: (userId) => request.get(`/playlist/user/${userId}`),
}

export const adminApi = {
  userList: (page, size) => request.get('/admin/users', { params: { page, size } }),
  disableUser: (id, status) => request.put(`/admin/user/${id}/disable`, null, { params: { status } }),
  deleteUser: (id) => request.delete(`/admin/user/${id}`),
  userDetail: (id) => request.get(`/admin/role/user-detail/${id}`),
  assignRole: (userId, roleId) => request.post('/admin/role/assign', null, { params: { userId, roleId } }),
  revokeRole: (userId, roleId) => request.delete('/admin/role/revoke', null, { params: { userId, roleId } }),
  roleList: () => request.get('/admin/role/list'),
  getUserRoles: (id) => request.get(`/admin/role/user/${id}`),
  songList: (status, page, size) => request.get('/admin/songs', { params: { status, page, size } }),
  pendingSongs: () => request.get('/admin/songs/pending'),
  auditPass: (id) => request.put(`/admin/song/${id}/audit/pass`),
  auditReject: (id, reason) => request.put(`/admin/song/${id}/audit/reject`, null, { params: { reason } }),
  offlineSong: (id) => request.put(`/admin/song/${id}/offline`),
  statsOverview: () => request.get('/admin/stats/overview'),
  statsDaily: () => request.get('/admin/stats/daily'),
}

