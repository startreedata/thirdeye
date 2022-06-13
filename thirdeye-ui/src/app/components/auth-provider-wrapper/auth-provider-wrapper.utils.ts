import { AppConfiguration } from "../../rest/dto/app-config.interface";

export const DUMMY_CLIENT_ID = "dummy-client-id";

export const processAuthData = (
    appConfiguration: AppConfiguration | null
): string => {
    // If auth is disabled, and client id is missing,  set a dummy client id
    if (isAuthDisabled(appConfiguration)) {
        if (!(appConfiguration as AppConfiguration).clientId) {
            return DUMMY_CLIENT_ID;
        }
    }

    // Validate received data
    if (!appConfiguration || !appConfiguration.clientId) {
        return "";
    }

    return appConfiguration.clientId;
};

export const isAuthDisabled = (
    appConfiguration: AppConfiguration | null
): boolean => {
    return (
        appConfiguration !== null &&
        appConfiguration.authEnabled !== undefined &&
        !appConfiguration.authEnabled
    );
};
