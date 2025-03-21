import axios from '../utils/axiosInstance';

const USER_API = '/identity/users';
const PROFILE_API = '/profile/users';
const USER_NOVEL_FOLLOW_API = '/profile/user-novel-follow';
const USER_NOVEL_REVIEW_API = '/profile/user-novel-review';
const USER_NOVEL_RATING_API = '/profile/user-novel-rating';
const USER_NOVEL_READING_HISTORY_API = '/profile/user-read-history';
const USER_NOVEL_BOOKMARK_API = '/profile/user-bookmarks';

interface AccountRequest {
    email: string;
    password: string;
}

interface UserNovelFollowRequest {
    userId: string;
    novelId: string;
}

const register = (data: AccountRequest) => {
    return axios.post(`${USER_API}/registration`, data)
        .then(response => response.data.result);
}

const getMyInfo = () => {
    return axios.get(`${USER_API}/my-info`)
        .then(response => response.data.result);
};

const getUserProfile = (userId: string) => {
    return axios.get(`${PROFILE_API}/get/${userId}`)
        .then(response => response.data.result);
};
const updateUserProfile = (id: string, data: any) => {
    return axios.put(`${PROFILE_API}/${id}`, data)
        .then(response => response.data.result);
}

const followNovel = (data: UserNovelFollowRequest) => {
    return axios.post(`${USER_NOVEL_FOLLOW_API}/follow`, data)
        .then(response => response.data.result);
}

const unfollowNovel = (data: UserNovelFollowRequest) => {
    return axios.post(`${USER_NOVEL_FOLLOW_API}/unfollow`, data)
        .then(response => response.data.result);
}

const isFollowingNovel = (data: UserNovelFollowRequest) => {
    return axios.post(`${USER_NOVEL_FOLLOW_API}/is-following`, data)
        .then(response => response.data.result);
}

const createReview = (data: any) => {
    return axios.post(`${USER_NOVEL_REVIEW_API}/create`, data)
        .then(response => response.data.result);
}
const updateReview = (id: string, data: any) => {
    return axios.put(`${USER_NOVEL_REVIEW_API}/${id}`, data)
        .then(response => response.data.result);
}
const deleteReview = (id: string) => {
    return axios.delete(`${USER_NOVEL_REVIEW_API}/${id}`)
        .then(response => response.data.result);
}
const getLatestReview = (page: number, size: number) => {
    return axios.get(`${USER_NOVEL_REVIEW_API}/get/get-latest?page=${page}&size=${size}`)
        .then(response => response.data.result);
}
const getReviewList = (novelId: string, page: number, size: number) => {
    return axios.get(`${USER_NOVEL_REVIEW_API}/get/novel/${novelId}?page=${page}&size=${size}`)
        .then(response => response.data.result);
}
const getMyFollowedNovels = (userId: string, page: number, size: number) => {
    return axios.get(`${USER_NOVEL_FOLLOW_API}/get/followed-novels/${userId}?page=${page}&size=${size}`)
        .then(response => response.data.result);
};

interface RatingRequest {
    userId: string;
    novelId: string;
    rating: number;
}

const createRating = (data: RatingRequest) => {
    return axios.post(`${USER_NOVEL_RATING_API}/rate`, data)
        .then(response => response.data.result);
}
const updateRating = (data: RatingRequest) => {
    return axios.put(`${USER_NOVEL_RATING_API}/update`, data)
        .then(response => response.data.result);
}
const hasRated = (userId: string, novelId: string) => {
    return axios.get(`${USER_NOVEL_RATING_API}/has-rated/${userId}/${novelId}`)
        .then(response => response.data.result);
}
const createReadingHistory = (data: any) => {
    return axios.post(`${USER_NOVEL_READING_HISTORY_API}`, data)
        .then(response => response.data.result);
}
const getMyReadingList = (userId: string, page: number, size: number) => {
    return axios.get(`${USER_NOVEL_READING_HISTORY_API}/reading-history/${userId}?page=${page}&size=${size}`)
        .then(response => response.data.result);
}
const addBookmark = (userId: string, data: any) => {
    return axios.post(`${USER_NOVEL_BOOKMARK_API}/save/${userId}`, data)
        .then(response => response.data.result);
}
const getBookmark = (userId: string, page: number, size: number) => {
    return axios.get(`${USER_NOVEL_BOOKMARK_API}/user/${userId}?page=${page}&size=${size}`)
        .then(response => response.data.result);
}
const getRatingOfNovel = (userId: string, novelId: string) => {
    return axios.get(`${USER_NOVEL_RATING_API}/get-rating/${userId}/${novelId}`)
        .then(response => response.data.result);
}
export {
    register,
    getMyInfo,
    getUserProfile,
    updateUserProfile,
    followNovel,
    unfollowNovel,
    isFollowingNovel,
    createReview,
    updateReview,
    deleteReview,
    getLatestReview,
    getReviewList,
    getMyFollowedNovels,
    createRating,
    updateRating,
    hasRated,
    createReadingHistory,
    getMyReadingList,
    addBookmark,
    getBookmark,
    getRatingOfNovel
};
