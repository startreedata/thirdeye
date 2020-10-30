import axios from "axios";
import { Authentication } from "./authentication-rest.interfaces";

const BASE_URL_AUTHENTICATION = "/api/auth";

export const login = async (): Promise<Authentication> => {
    const params = new URLSearchParams();
    params.append("grant_type", "password");
    params.append("principal", "admin");
    params.append("password", "password");

    const response = await axios.post(
        `${BASE_URL_AUTHENTICATION}/login`,
        params
    );

    return response.data;
};
