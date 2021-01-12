import { ReactNode } from "react";

export interface AuthProviderProps {
    children?: ReactNode;
}

export interface UseAuthProps {
    auth: boolean;
    accessToken: string;
    signIn: (accessToken: string) => void;
    signOut: () => void;
}
