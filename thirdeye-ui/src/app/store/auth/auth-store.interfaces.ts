export type AuthStore = {
    auth: boolean;
    accessToken: string;
    setAccessToken: (accessToken: string) => void;
    removeAccessToken: () => void;
};
