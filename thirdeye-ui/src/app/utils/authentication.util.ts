const LOCAL_STORAGE_KEY_AUTHENTICATION = "LOCAL_STORAGE_KEY_AUTHENTICATION";

// Returns true if the session is authenticated
export const isAuthenticated = (): boolean => {
    return localStorage.getItem(LOCAL_STORAGE_KEY_AUTHENTICATION)
        ? true
        : false;
};

// Removes session authentication
export const removeAuthentication = (): void => {
    localStorage.removeItem(LOCAL_STORAGE_KEY_AUTHENTICATION);
};

// Sets the session as authenticated
export const setAuthentication = (token: string): void => {
    localStorage.setItem(LOCAL_STORAGE_KEY_AUTHENTICATION, token);
};

export const getAccessToken = (): string => {
    return localStorage.getItem(LOCAL_STORAGE_KEY_AUTHENTICATION) as string;
};
