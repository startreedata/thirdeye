const LOCAL_STORAGE_KEY_AUTH = "LOCAL_STORAGE_KEY_AUTH";

export const isAuthenticated = (): boolean => {
    return localStorage.getItem(LOCAL_STORAGE_KEY_AUTH) ? true : false;
};

export const getAccessToken = (): string => {
    return localStorage.getItem(LOCAL_STORAGE_KEY_AUTH) as string;
};

export const setAccessToken = (token: string): void => {
    localStorage.setItem(LOCAL_STORAGE_KEY_AUTH, token);
};

export const removeAccessToken = (): void => {
    localStorage.removeItem(LOCAL_STORAGE_KEY_AUTH);
};
