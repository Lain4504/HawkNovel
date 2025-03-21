import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import store from '../store';

// Define route names as constants
const ROUTE_NAMES = {
    DASHBOARD: 'dashboard',
    CREATE_NOVEL: 'createNovel',
    NOVELS_OF_AUTHOR: 'novelOfAuthorList',
    NOVEL_AUTHOR_MANAGEMENT: 'novelAuthorManagement',
    CREATE_POST_CATEGORY: 'createPostCategory',
    POST_CATEGORY_LIST: 'postCategoryList',
    UPDATE_POST_CATEGORY: 'updatePostCategory',
    POST_LIST: 'postList',
    CREATE_NOVEL_CATEGORY: 'createNovelCategory',
    NOVEL_CATEGORY_LIST: 'novelCategoryList',
    UPDATE_NOVEL_CATEGORY: 'updateNovelCategory',
    NOVEL_LIST: 'novelList',
    NOVEL_CATEGORY_FOR_AUTHOR: 'novelCategoryForAuthor',
    POST_CATEGORY_FOR_AUTHOR: 'postCategoryForAuthor',
    ADS_LIST: 'adsList',
    UPDATE_ADS: 'updateAds',
    CREATE_ADS: 'createAds',
    LOGIN: 'login',
    REGISTER: 'register',
    HOME: 'home',
    CHAPTER: 'chapter',
    NOVEL_DETAIL: 'noveldetail',
    ACCOUNT: 'account',
    LIST: 'list',
    BOOKMARK: 'bookmark',
    READING_LIST: 'readinglist',
    USER_PROFILE: 'userprofile',
    LIBRARY: 'library',
    FAQ: 'faq',
    POST_FORUM: 'postforum',
    POST_DETAIL: 'postdetail',
    POST_CREATE_FORM: 'postcreateform',
    POST_UPDATE_FORM: 'postupdateform',
    ACTIVATION: 'activation',
    FILTER_BY_CATEGORY: 'filterbycategory',
    SEARCH_ADVANCED: 'searchadvanced',
    NOTIFICATION_LIST: 'notificationlist',
};

const routes: Array<RouteRecordRaw> = [
    {
        name: ROUTE_NAMES.DASHBOARD, path: '/dashboard', component: () => import('../views/admin/Dashboard.vue'),
        children: [
            { name: ROUTE_NAMES.CREATE_NOVEL, path: '/create-novel', component: () => import('../views/admin/novelauthor/novel/AddNovel.vue') },
            { name: ROUTE_NAMES.NOVELS_OF_AUTHOR, path: '/novels-of-author', component: () => import('../views/admin/novelauthor/NovelAuthorList.vue') },
            { name: ROUTE_NAMES.NOVEL_AUTHOR_MANAGEMENT, path: '/novel-author-management/:id', component: () => import('../views/admin/novelauthor/NovelAuthorManagement.vue'), props: true },
            { name: ROUTE_NAMES.CREATE_POST_CATEGORY, path: '/create-post-category', component: () => import('../views/admin/adminmanagement/post/PostCategoryNew.vue') },
            { name: ROUTE_NAMES.POST_CATEGORY_LIST, path: '/post-category-list', component: () => import('../views/admin/adminmanagement/post/PostCategoryList.vue') },
            { name: ROUTE_NAMES.UPDATE_POST_CATEGORY, path: '/update-post-category/:id', component: () => import('../views/admin/adminmanagement/post/PostCategoryEdit.vue'), props: true },
            { name: ROUTE_NAMES.POST_LIST, path: '/post-list', component: () => import('../views/admin/adminmanagement/post/PostList.vue') },
            { name: ROUTE_NAMES.CREATE_NOVEL_CATEGORY, path: '/create-novel-category', component: () => import('../views/admin/adminmanagement/novel/NovelCategoryNew.vue') },
            { name: ROUTE_NAMES.NOVEL_CATEGORY_LIST, path: '/novel-category-list', component: () => import('../views/admin/adminmanagement/novel/NovelCategoryList.vue') },
            { name: ROUTE_NAMES.UPDATE_NOVEL_CATEGORY, path: '/update-novel-category/:id', component: () => import('../views/admin/adminmanagement/novel/NovelCategoryEdit.vue'), props: true },
            { name: ROUTE_NAMES.NOVEL_LIST, path: '/novel-list', component: () => import('../views/admin/adminmanagement/novel/NovelList.vue') },
            { name: ROUTE_NAMES.NOVEL_CATEGORY_FOR_AUTHOR, path: '/novel-category-for-author', component: () => import('../views/admin/NovelCategoryForAuthor.vue') },
            { name: ROUTE_NAMES.POST_CATEGORY_FOR_AUTHOR, path: '/post-category-for-author', component: () => import('../views/admin/PostCategoryForAuthor.vue') },
            { name: ROUTE_NAMES.ADS_LIST, path: '/ads-list', component: () => import('../views/admin/adminmanagement/prompt/AdsList.vue') },
            { name: ROUTE_NAMES.UPDATE_ADS, path: '/update-ads/:id', component: () => import('../views/admin/adminmanagement/prompt/UpdateAds.vue'), props: true },
            { name: ROUTE_NAMES.CREATE_ADS, path: '/create-ads', component: () => import('../views/admin/adminmanagement/prompt/CreateAds.vue') },
        ]
    },
    {
        name: ROUTE_NAMES.HOME, path: '/', component: () => import('../views/home/Home.vue'),
        children: [
            { name: ROUTE_NAMES.CHAPTER, path: '/:novel/:chapter', component: () => import('../views/home/novel/Chapter.vue') },
            { name: ROUTE_NAMES.NOVEL_DETAIL, path: '/novel/:id', component: () => import('../views/home/novel/NovelDetail.vue') },
            { name: ROUTE_NAMES.ACCOUNT, path: '/account/:id', component: () => import('../views/home/account/Account.vue') },
            { name: ROUTE_NAMES.LIST, path: '/list', component: () => import('../views/home/account/TabSwitch.vue'), children: [
                    { path: '', redirect: '/list/readinglist' },
                    { name: ROUTE_NAMES.BOOKMARK, path: 'bookmark', component: () => import('../views/home/account/BookMark.vue'), props: { type: 'bookmark' } },
                    { name: ROUTE_NAMES.READING_LIST, path: 'readinglist', component: () => import('../views/home/homepage/RecentlyReadingList.vue'), props: { type: 'readinglist' } }
                ]
            },
            { name: ROUTE_NAMES.USER_PROFILE, path: '/user-profile', component: () => import('../views/home/account/UserProfileSetting.vue') },
            { name: ROUTE_NAMES.LIBRARY, path: '/library', component: () => import('../views/home/account/Library.vue') },
            { name: ROUTE_NAMES.FAQ, path: '/faq', component: () => import('../views/common/FAQ.vue') },
            { name: ROUTE_NAMES.POST_FORUM, path: '/post-forum', component: () => import('../views/home/forum/PostForum.vue') },
            { name: ROUTE_NAMES.POST_DETAIL, path: '/post/:id', component: () => import('../views/home/forum/PostDetail.vue'), props: true },
            { name: ROUTE_NAMES.POST_CREATE_FORM, path: '/post-create-form', component: () => import('../views/home/forum/PostCreateForm.vue') },
            { name: ROUTE_NAMES.POST_UPDATE_FORM, path: '/post-update-form/:id', component: () => import('../views/home/forum/PostUpdateForm.vue'), props: true },
            { name: ROUTE_NAMES.ACTIVATION, path: '/activation/:token', component: () => import('../views/home/account/Activation.vue') },
            { name: ROUTE_NAMES.FILTER_BY_CATEGORY, path: '/filter-by-category', component: () => import('../views/home/novel/FilterByCategory.vue') },
            { name: ROUTE_NAMES.SEARCH_ADVANCED, path: '/search-advanced', component: () => import('../views/home/novel/SearchAdvanced.vue') },
            { name: ROUTE_NAMES.NOTIFICATION_LIST, path: '/notification-list', component: () => import('../views/home/account/NotifcationList.vue') },
        ]
    },
    { name: ROUTE_NAMES.LOGIN, path: '/login', component: () => import('../views/auth/Login.vue') },
    { name: ROUTE_NAMES.REGISTER, path: '/register', component: () => import('../views/auth/Register.vue') },
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
});

const adminRoutes = [
    ROUTE_NAMES.DASHBOARD, ROUTE_NAMES.NOVELS_OF_AUTHOR, ROUTE_NAMES.NOVEL_CATEGORY_FOR_AUTHOR,
    ROUTE_NAMES.POST_CATEGORY_FOR_AUTHOR, 'authorAccount'
];

router.beforeEach((to, _from, next) => {
    const isAuthenticated = store.getters.isAuthenticated;
    const userRole = store.getters.getUserRole;

    if (isAuthenticated && (to.name === ROUTE_NAMES.LOGIN || to.name === ROUTE_NAMES.REGISTER)) {
        next({ name: ROUTE_NAMES.HOME });
    } else if (!isAuthenticated && to.matched.some(record => record.path.startsWith('/dashboard'))) {
        next({ name: ROUTE_NAMES.LOGIN });
    } else if (adminRoutes.includes(to.name as string) && userRole !== 'ADMIN') {
        next({ name: ROUTE_NAMES.HOME });
    } else {
        next();
    }
});

export default router;