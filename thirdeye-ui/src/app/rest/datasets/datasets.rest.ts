import axios from "axios";
import { Dataset } from "../dto/metric.interfaces";

const BASE_URL_DATASET = "/api/datasets";

export const getAllDatasets = async (): Promise<Dataset[]> => {
    const response = await axios.get(BASE_URL_DATASET);

    return response.data;
};
