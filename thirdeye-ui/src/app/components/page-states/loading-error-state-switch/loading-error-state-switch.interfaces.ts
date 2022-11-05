import { ReactNode } from "react";

export interface LoadingErrorStateSwitchProps {
    children: ReactNode;
    errorState?: ReactNode;
    loadingState?: ReactNode;
    isLoading: boolean;
    isError: boolean;
}
