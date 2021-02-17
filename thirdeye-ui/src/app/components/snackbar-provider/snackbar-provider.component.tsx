import { IconButton } from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import {
    SnackbarKey,
    SnackbarProvider as NotistackSnackbarProvider,
} from "notistack";
import React, { FunctionComponent, useRef } from "react";
import { SnackbarProviderProps } from "./snackbar-provider.interfaces";
import { useSnackbarProviderStyles } from "./snackbar-provider.styles";

export const SnackbarProvider: FunctionComponent<SnackbarProviderProps> = (
    props: SnackbarProviderProps
) => {
    const snackbarProviderClasses = useSnackbarProviderStyles();
    const snackbarProviderRef = useRef<NotistackSnackbarProvider>(null);

    const handleSnackbarClose = (key: SnackbarKey): void => {
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
                            handleSnackbarClose(key);
                        }}
                    >
                        <CloseIcon />
                    </IconButton>
                );
            }}
            anchorOrigin={{
                // Snackbar to appear in top right corner
                horizontal: "right",
                vertical: "top",
            }}
            autoHideDuration={3500}
            className={snackbarProviderClasses.snackbarProvider}
            classes={{
                variantSuccess: snackbarProviderClasses.success,
                variantError: snackbarProviderClasses.error,
                variantWarning: snackbarProviderClasses.warning,
                variantInfo: snackbarProviderClasses.info,
            }}
            ref={snackbarProviderRef}
        >
            {props.children}
        </NotistackSnackbarProvider>
    );
};
