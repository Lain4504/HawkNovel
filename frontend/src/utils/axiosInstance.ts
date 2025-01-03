import axios from 'axios';
import store from "../store/index";
import router from "../router/index";
import {refreshToken} from "../api/auth.ts";


axios.defaults.baseURL = import.meta.env.VITE_API_URL
axios.defaults.timeout = 10000;
axios.defaults.headers['X-Requested-With'] = 'XMLHttpRequest'
axios.defaults.headers.post['Content-Type'] = 'application/json'
axios.interceptors.request.use((config) => {
    if (store.getters.getToken) {
        config.headers.Authorization = `Bearer ${store.getters.getToken}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});
axios.interceptors.response.use((response) => {
    return response;
}, (error) => {
    if (error.response.status === 401) {
        refreshToken(store.getters.getRefreshToken).then((response) => {
            store.commit('setToken', response.accessToken);
            store.commit('setRefreshToken', response.refreshToken);
            console.log('refresh token success', response);
        }).catch((error) => {
            console.log('refresh token error', error);
            store.commit('clearUser');
            router.push('/login');
        });
    }
    return Promise.reject(error);
});
export default axios;