export type AuthStore = {
    auth: boolean;
    accessToken: string;
    setAccessToken: (token: string) => void;
    removeAccessToken: () => void;
};
