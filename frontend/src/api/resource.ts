import axios from "../utils/axiosInstance";
const ADS_API = "/novel/prompt";
const createAds = (data: any) => {
    return axios.post(`${ADS_API}/create`, data)
        .then((response) => response.data.result);
}
const updateAds = (id: string, data: any) => {
    return axios.put(`${ADS_API}/update/${id}`, data)
        .then((response) => response.data.result);
}
const deleteAds = (id: string) => {
    return axios.delete(`${ADS_API}/delete/${id}`)
        .then((response) => response.data.result);
}
const getAdsById = (id: string) => {
    return axios.get(`${ADS_API}/get/${id}`)
        .then((response) => response.data.result);
}
const getAllAds = (page: number, size:number) => {
    return axios.get(`${ADS_API}/get/all?page=${page}&size=${size}`)
        .then((response) => response.data.result);
};
export { createAds, updateAds, deleteAds, getAdsById, getAllAds };
