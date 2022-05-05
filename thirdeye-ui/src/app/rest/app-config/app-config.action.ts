import { useHTTPAction } from "../create-rest-action";
import { AppConfiguration } from "../dto/app-config.interface";
import { GetAppConfiguration } from "./app-config.interfaces";
import { getAppConfiguration as getAppConfigurationRest } from "./app-config.rest";

export const useGetAppConfiguration = (): GetAppConfiguration => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<AppConfiguration>(getAppConfigurationRest);

    const getAppConfiguration = (): Promise<AppConfiguration | undefined> => {
        return makeRequest();
    };

    return { appConfig: data, getAppConfiguration, status, errorMessages };
};
