import { ReactNode } from "react";

export interface AuthProviderProps {
    children: ReactNode;
}

export interface AuthContextProps {
    authDisabled: boolean;
    authenticated: boolean;
    accessToken: string;
    login: () => Promise<boolean>;
    logout: () => void;
}
