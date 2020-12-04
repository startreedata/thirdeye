import { IconButton } from "@material-ui/core";
import { Close } from "@material-ui/icons";
import { SnackbarKey, SnackbarProvider } from "notistack";
import React, { createRef, FunctionComponent, ReactNode } from "react";
import { ApplicationSnackbarProviderProps } from "./application-snackbar-provider.interfaces";
import { useApplicationSnackbarProviderStyles } from "./application-snackbar-provider.styles";

export const ApplicationSnackbarProvider: FunctionComponent<ApplicationSnackbarProviderProps> = (
    props: ApplicationSnackbarProviderProps
) => {
    const applicationSnackbarProviderClasses = useApplicationSnackbarProviderStyles();
    const snackbarRef = createRef<SnackbarProvider>();

    const onCloseSnackbar = (key: SnackbarKey): void => {
        snackbarRef.current?.closeSnackbar(key);
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
            className={applicationSnackbarProviderClasses.container}
            classes={{
                variantSuccess: applicationSnackbarProviderClasses.success,
                variantError: applicationSnackbarProviderClasses.error,
                variantWarning: applicationSnackbarProviderClasses.warning,
                variantInfo: applicationSnackbarProviderClasses.info,
            }}
            ref={snackbarRef}
        >
            {/* Include children */}
            {props.children}
        </SnackbarProvider>
    );
};
