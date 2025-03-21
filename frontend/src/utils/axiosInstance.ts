import axios from 'axios';
import store from "../store/index";
import router from "../router/index";


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
        store.commit('clearUser');
        console.log('401 error');
        router.push('/login');
    }
    return Promise.reject(error);
});
export default axios;