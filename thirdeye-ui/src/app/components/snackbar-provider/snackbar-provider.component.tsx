import { IconButton } from "@material-ui/core";
import { Close } from "@material-ui/icons";
import {
    SnackbarKey,
    SnackbarProvider as NotistackSnackbarProvider,
} from "notistack";
import React, { createRef, FunctionComponent } from "react";
import { SnackbarProviderProps } from "./snackbar-provider.interfaces";
import { useSnackbarProviderStyles } from "./snackbar-provider.styles";

export const SnackbarProvider: FunctionComponent<SnackbarProviderProps> = (
    props: SnackbarProviderProps
) => {
    const snackbarProviderClasses = useSnackbarProviderStyles();
    const snackbarProviderRef = createRef<NotistackSnackbarProvider>();

    const onCloseSnackbar = (key: SnackbarKey): void => {
        snackbarProviderRef &&
            snackbarProviderRef.current &&
            snackbarProviderRef.current.closeSnackbar(key);
    };

    return (
        <NotistackSnackbarProvider
            hideIconVariant
            action={(key) => {
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
            className={snackbarProviderClasses.container}
            classes={{
                variantSuccess: snackbarProviderClasses.success,
                variantError: snackbarProviderClasses.error,
                variantWarning: snackbarProviderClasses.warning,
                variantInfo: snackbarProviderClasses.info,
            }}
            ref={snackbarProviderRef}
        >
            {/* Contents */}
            {props.children}
        </NotistackSnackbarProvider>
    );
};
