import { IconButton } from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import {
    SnackbarKey,
    SnackbarProvider as NotistackSnackbarProvider,
} from "notistack";
import React, { FunctionComponent, useRef } from "react";
import { SnackbarProviderProps } from "./snackbar-provider.interfaces";
import { useSnackbarProviderStyles } from "./snackbar-provider.styles";

const DURATION_SNACKBAR_AUTO_HIDE = 3500;

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
            action={(key) => (
                <>
                    {/* Close button */}
                    <IconButton onClick={() => handleSnackbarClose(key)}>
                        <CloseIcon />
                    </IconButton>
                </>
            )}
            anchorOrigin={{
                // Snackbar to appear in top right corner
                horizontal: "right",
                vertical: "top",
            }}
            autoHideDuration={DURATION_SNACKBAR_AUTO_HIDE}
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
