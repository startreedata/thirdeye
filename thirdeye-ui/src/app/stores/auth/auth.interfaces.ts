export type AuthStore = {
    authDisabled: boolean;
    authenticated: boolean;
    accessToken: string;
    disableAuth: () => void;
    setAccessToken: (token: string) => void;
    clearAccessToken: () => void;
};
