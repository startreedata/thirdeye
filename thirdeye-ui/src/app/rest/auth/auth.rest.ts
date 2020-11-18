import axios from "axios";
import { Auth } from "../dto/auth.interfaces";

const BASE_URL_AUTH = "/api/auth";

export const login = async (): Promise<Auth> => {
    const params = new URLSearchParams();
    params.append("grant_type", "password");
    params.append("principal", "admin");
    params.append("password", "password");

    const response = await axios.post(`${BASE_URL_AUTH}/login`, params);

    return response.data;
};

export const logout = async (): Promise<void> => {
    await axios.post(`${BASE_URL_AUTH}/logout`);
};
