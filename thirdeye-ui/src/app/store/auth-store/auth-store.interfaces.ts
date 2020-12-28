export type AuthStore = {
    auth: boolean;
    accessToken: string;
    setAccessToken: (token: string) => void;
    clearAccessToken: () => void;
};
