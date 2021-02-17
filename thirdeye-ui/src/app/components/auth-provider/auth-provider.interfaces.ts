import { ReactNode } from "react";

export interface AuthProviderProps {
    children: ReactNode;
}

export interface UseAuthProps {
    authDisabled: boolean;
    authenticated: boolean;
    accessToken: string;
    disableAuth: () => void;
    signIn: (accessToken: string) => void;
    signOut: () => void;
}
