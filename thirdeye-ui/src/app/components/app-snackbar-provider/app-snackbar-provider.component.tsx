import { IconButton } from "@material-ui/core";
import { Close } from "@material-ui/icons";
import { SnackbarKey, SnackbarProvider } from "notistack";
import React, { createRef, FunctionComponent, ReactNode } from "react";
import { AppSnackbarProviderProps } from "./app-snackbar-provider.interfaces";
import { useAppSnackbarProviderStyles } from "./app-snackbar-provider.styles";

export const AppSnackbarProvider: FunctionComponent<AppSnackbarProviderProps> = (
    props: AppSnackbarProviderProps
) => {
    const appSnackbarProviderClasses = useAppSnackbarProviderStyles();
    const snackbarRef = createRef<SnackbarProvider>();

    const onCloseSnackbar = (key: SnackbarKey): void => {
        if (!snackbarRef || !snackbarRef.current) {
            return;
        }

        snackbarRef.current.closeSnackbar(key);
    };

    return (
        <SnackbarProvider
            hideIconVariant
            action={(key: SnackbarKey): ReactNode => {
                // Close button
                return (
                    <IconButton
                        onClick={(): void => {
                            onCloseSnackbar(key);
                        }}
                    >
                        <Close />
                    </IconButton>
                );
            }}
            anchorOrigin={{
                // Snackbar to appear in top right corner
                horizontal: "right",
                vertical: "top",
            }}
            autoHideDuration={3500}
            className={appSnackbarProviderClasses.container}
            classes={{
                variantSuccess: appSnackbarProviderClasses.success,
                variantError: appSnackbarProviderClasses.error,
                variantWarning: appSnackbarProviderClasses.warning,
                variantInfo: appSnackbarProviderClasses.info,
            }}
            ref={snackbarRef}
        >
            {/* Include children */}
            {props.children}
        </SnackbarProvider>
    );
};
