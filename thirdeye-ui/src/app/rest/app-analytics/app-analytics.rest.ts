import axios from "axios";
import { AppAnalytics } from "../dto/app-analytics.interfaces";

const URL_CONFIG = "/api/app-analytics";

export const getAppAnalytics = async (): Promise<AppAnalytics> => {
    const response = await axios.get(URL_CONFIG);

    return response.data;
};
