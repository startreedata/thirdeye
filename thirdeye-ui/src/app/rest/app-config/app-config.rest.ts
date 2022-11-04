import axios from "axios";
import { AppConfiguration } from "../dto/app-config.interface";

const URL_CONFIG = "/api/ui/config";

export const getAppConfiguration = async (): Promise<AppConfiguration> => {
    const response = await axios.get(URL_CONFIG);

    return response.data;
};
