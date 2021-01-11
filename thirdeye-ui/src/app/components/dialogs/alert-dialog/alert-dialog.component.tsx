import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Grid,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent, useContext } from "react";
import { useTranslation } from "react-i18next";
import { DialogContext } from "../dialog-provider/dialog-provider.component";
import { useAlertDialogStyles } from "./alert-dialog.styles";

export const AlertDialog: FunctionComponent = () => {
    const alertDialogClasses = useAlertDialogStyles();
    const { visible, hideDialog, dialogData } = useContext(DialogContext);
    const { t } = useTranslation();

    const onClose = (): void => {
        dialogData && dialogData.onCancel && dialogData.onCancel();

        hideDialog();
    };

    const onOk = (): void => {
        dialogData && dialogData.onOk && dialogData.onOk();

        hideDialog();
    };

    return (
        <>
            {dialogData && (
                <Dialog disableBackdropClick open={visible} onClose={onClose}>
                    {/* Header */}
                    {dialogData.title && (
                        <DialogTitle disableTypography>
                            <Typography variant="h5">
                                {dialogData.title}
                            </Typography>
                        </DialogTitle>
                    )}

                    {/* Contents */}
                    <DialogContent
                        className={alertDialogClasses.contentsContainer}
                    >
                        <DialogContentText variant="body1">
                            {dialogData.text}
                        </DialogContentText>
                    </DialogContent>

                    {/* Controls */}
                    <DialogActions>
                        <Box margin={1} marginTop={0}>
                            <Grid container justify="flex-end">
                                {/* Cancel button */}
                                <Grid item>
                                    <Button
                                        color="primary"
                                        size="large"
                                        variant="outlined"
                                        onClick={onClose}
                                    >
                                        {dialogData.cancelButtonLabel ||
                                            t("label.cancel")}
                                    </Button>
                                </Grid>

                                {/* Ok button */}
                                <Grid item>
                                    <Button
                                        color="primary"
                                        size="large"
                                        variant="contained"
                                        onClick={onOk}
                                    >
                                        {dialogData.okButtonLabel ||
                                            t("label.ok")}
                                    </Button>
                                </Grid>
                            </Grid>
                        </Box>
                    </DialogActions>
                </Dialog>
            )}
        </>
    );
};
