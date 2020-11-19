export type Auth = {
    auth: boolean;
    accessToken: string;
    setAccessToken: (accessToken: string) => void;
    removeAccessToken: () => void;
};
