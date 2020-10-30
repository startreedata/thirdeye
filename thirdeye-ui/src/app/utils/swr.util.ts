import axios, { AxiosResponse } from "axios";

// Provides SWR default fetcher configured with axios
export const swrFetcher = async <T extends unknown>(
    url: string
): Promise<T> => {
    const response: AxiosResponse = await axios.get(url);

    return response.data;
};
