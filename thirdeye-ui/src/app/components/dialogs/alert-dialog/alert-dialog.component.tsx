import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Grid,
} from "@material-ui/core";
import React, { FunctionComponent, useContext } from "react";
import { useTranslation } from "react-i18next";
import { DialogContext } from "../dialog-provider/dialog-provider.component";

export const AlertDialog: FunctionComponent = () => {
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
                <Dialog
                    disableBackdropClick
                    fullWidth
                    maxWidth="xs"
                    open={visible}
                    onClose={onClose}
                >
                    {/* Header */}
                    {dialogData.title && (
                        <DialogTitle>{dialogData.title}</DialogTitle>
                    )}

                    {/* Contents */}
                    <DialogContent>
                        <DialogContentText>{dialogData.text}</DialogContentText>
                    </DialogContent>

                    {/* Controls */}
                    <DialogActions>
                        <Box margin={1} marginTop={0}>
                            <Grid container justify="flex-end">
                                {/* Cancel button */}
                                <Grid item>
                                    <Button
                                        color="primary"
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
