import { createStore } from "vuex";

interface UserState {
    token: string | null;
    refreshToken: string | null;
    user: {
        id: string;
        email: string;
        image: string;
        roles: { name: string; description: string | null; permissions: any[] }[];
    } | null;
    isDarkMode: boolean;
}

const store = createStore({
    state: {
        token: localStorage.getItem('token') || null,
        refreshToken: localStorage.getItem('refreshToken') || null,
        user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user') as string) : null,
        isDarkMode: localStorage.getItem('isDarkMode') === "true",
    } as UserState,
    mutations: {
        setUser(state: UserState, user: { id: string; email: string; image: string; roles: { name: string; description: string | null; permissions: any[] }[] }) {
            state.user = user;
            localStorage.setItem('user', JSON.stringify(user));
        },
        setToken(state: UserState, token: string) {
            state.token = token;
            localStorage.setItem('token', token);
        },
        setRefreshToken(state: UserState, refreshToken: string) {
            state.refreshToken = refreshToken;
            localStorage.setItem('refreshToken', refreshToken);
        },
        clearUser(state: UserState) {
            state.user = null;
            state.token = null;
            state.refreshToken = null;
            localStorage.removeItem('user');
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
        },
        toggleDarkMode(state: UserState) {
            state.isDarkMode = !state.isDarkMode;
            localStorage.setItem('isDarkMode', state.isDarkMode.toString());
        },
        setDarkMode(state: UserState, isDark: boolean) {
            state.isDarkMode = isDark;
            localStorage.setItem('isDarkMode', isDark.toString());
        },
    },
    actions: {
        toggleDarkMode({ commit }: { commit: Function }) {
            commit("toggleDarkMode");
        },
        initializeDarkMode({ commit }: { commit: Function }) {
            const savedDarkMode = localStorage.getItem("isDarkMode") === "true";
            commit("setDarkMode", savedDarkMode);
        },
    },
    getters: {
        isAuthenticated(state: UserState): boolean {
            return !!state.token;
        },
        getUserRole(state: UserState): string | null {
            return state.user ? state.user.roles.map(role => role.name).join(', ') : null;
        },
        getToken(state: UserState): string | null {
            return state.token;
        },
        getRefreshToken(state: UserState): string | null {
            return state.refreshToken;
        },
        getUserId(state: UserState): string | null {
            return state.user ? state.user.id : null;
        },
        getEmail(state: UserState): string | null {
            return state.user ? state.user.email : null;
        },
        isDarkMode(state: UserState): boolean {
            return state.isDarkMode;
        },
        getUserImage(state: UserState): string | null {
            return state.user ? state.user.image : null;
        },
    }
});

export default store;