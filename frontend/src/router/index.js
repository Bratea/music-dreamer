import { createRouter, createWebHistory } from 'vue-router'
import LayoutView from '../views/LayoutView.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/',
    component: LayoutView,
    children: [
      { path: '', name: 'Home', component: () => import('../views/HomeView.vue'), meta: { keepAlive: true } },
      { path: '/search', name: 'Search', component: () => import('../views/SearchView.vue'), meta: { keepAlive: true } },
      { path: '/song/:id', name: 'SongDetail', component: () => import('../views/SongDetailView.vue') },
      { path: '/playlist/:id', name: 'PlaylistDetail', component: () => import('../views/PlaylistDetailView.vue') },
      { path: '/singer', name: 'SingerCenter', component: () => import('../views/SingerCenterView.vue'), meta: { keepAlive: true } },
      { path: '/singer/apply', name: 'SingerApply', component: () => import('../views/SingerApplyView.vue') },
      { path: '/admin', name: 'AdminPanel', component: () => import('../views/AdminPanelView.vue') },
      { path: '/stats', name: 'DataStats', component: () => import('../views/DataStatsView.vue') },
      { path: '/my', name: 'MyProfile', component: () => import('../views/MyProfileView.vue'), meta: { keepAlive: true } },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：需要登录的页面检查 token
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const singerPaths = ['/singer', '/singer/apply']
  const adminPaths = ['/admin', '/stats']
  if ((singerPaths.some(p => to.path.startsWith(p)) || adminPaths.some(p => to.path.startsWith(p))) && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
