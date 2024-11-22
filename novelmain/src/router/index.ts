import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import Dashboard from '../views/admin/Dashboard.vue';
import Analysis from '../views/admin/Analysis.vue';
import AddNovel from '../views/admin/AddNovel.vue';
import ExistedNovel from '../views/admin/ExistedNovel.vue';
import Support from '../views/admin/Support.vue';
import SystemNotification from '../views/admin/SystemNotification.vue';
import Login from '../views/common/Login.vue';
import Register from '../views/common/Register.vue';
import Home from '../views/home/Home.vue';
const routes: Array<RouteRecordRaw> = [
  {
    name: 'dashboard',
    path: '/dashboard',
    component: Dashboard,
    children: [
      {
        name: 'analytics',
        path: '/analytics',
        component: Analysis,
      },
      {
        name: 'newnovel',
        path: '/new-novel',
        component: AddNovel,
      },
      {
        name: 'existednovels',
        path: '/existing-novels',
        component: ExistedNovel,
      },
      {
        name: 'support',
        path: '/support',
        component: Support,
      },
      {
        name: 'adminnotification',
        path: '/admin-notification',
        component: SystemNotification,
      }
    ]
  },
  {
    name: 'login',
    path: '/login',
    component: Login,
  },
  {
    name: 'register',
    path: '/register',
    component: Register,
  },
  {
    name: 'home',
    path: '/',
    component: Home,
  },
];

// Tạo router
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

export default router;