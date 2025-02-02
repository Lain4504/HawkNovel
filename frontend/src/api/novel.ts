import axios from "../utils/axiosInstance";

const NOVEL_API = "/novel/novels";
const createNovel = (data: FormData) => {
    return axios.post(`${NOVEL_API}/create`, data, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    }).then((response) => response.data.result);
}
const updateNovel = (id: string, data: FormData | null) => {
    return axios.put(`${NOVEL_API}/update/${id}`, data, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    }).then((response) => response.data.result);
};
const deleteNovel = (id: string) => {
    return axios.delete(`${NOVEL_API}/delete/${id}`)
        .then((response) => response.data.result);
}
const getNovel = (id: string) => {
    return axios.get(`${NOVEL_API}/get/${id}`)
        .then((response) => response.data.result);
}
const getNovels = (page: number, size: number) => {
    return axios.get(`${NOVEL_API}/get/all?page=${page}&size=${size}`)
        .then((response) => response.data.result);
}
const getLatestNovels = (page: number, size: number) => {
    return axios.get(`${NOVEL_API}/get/latest-novels?page=${page}&size=${size}`)
        .then((response) => response.data.result);
}
const getMyNovels = (page: number, size: number) => {
    return axios.get(`${NOVEL_API}/my-novels?page=${page}&size=${size}`)
        .then((response) => response.data.result);
}
const getNovelsByAuthorId = (authorId: string, page: number, size: number) => {
    return axios.get(`${NOVEL_API}/get/author/${authorId}?page=${page}&size=${size}`)
        .then((response) => response.data.result);
}
const getNovelsByStatus = (status: string, page: number, size: number) => {
    return axios.get(`${NOVEL_API}/get/find-by-status/${status}?page=${page}&size=${size}`)
        .then((response) => response.data.result);
}
export {
    createNovel,
    updateNovel,
    deleteNovel,
    getNovel,
    getNovels,
    getMyNovels,
    getNovelsByAuthorId,
    getLatestNovels,
    getNovelsByStatus
};