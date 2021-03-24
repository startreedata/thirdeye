export type AuthStore = {
    authDisabled: boolean;
    authenticated: boolean;
    accessToken: string;
    redirectPath: string;
    disableAuth: () => void;
    setAccessToken: (token: string) => void;
    clearAccessToken: () => void;
    setRedirectPath: (path: string) => void;
    clearRedirectPath: () => void;
};
