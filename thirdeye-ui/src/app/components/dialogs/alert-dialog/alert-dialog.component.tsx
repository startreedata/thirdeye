import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from "@material-ui/core";
import React, { FunctionComponent, useContext } from "react";
import { useTranslation } from "react-i18next";
import { DialogContext } from "../dialog-provider/dialog-provider.component";

export const AlertDialog: FunctionComponent = () => {
    const { visible, hideDialog, dialogData } = useContext(DialogContext);
    const { t } = useTranslation();

    const handleClose = (): void => {
        dialogData && dialogData.onCancel && dialogData.onCancel();
        hideDialog();
    };

    const handleOk = (): void => {
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
                    onClose={handleClose}
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
                        {/* Cancel button */}
                        <Button
                            color="primary"
                            variant="outlined"
                            onClick={handleClose}
                        >
                            {dialogData.cancelButtonLabel || t("label.cancel")}
                        </Button>

                        {/* Ok button */}
                        <Button
                            color="primary"
                            variant="contained"
                            onClick={handleOk}
                        >
                            {dialogData.okButtonLabel || t("label.ok")}
                        </Button>
                    </DialogActions>
                </Dialog>
            )}
        </>
    );
};
