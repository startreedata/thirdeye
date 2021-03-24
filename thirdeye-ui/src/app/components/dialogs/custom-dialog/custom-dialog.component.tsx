import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
} from "@material-ui/core";
import { isNil } from "lodash";
import React, { FunctionComponent, useContext } from "react";
import { useTranslation } from "react-i18next";
import { DialogContext } from "../dialog-provider/dialog-provider.component";
import { CustomDialogData } from "../dialog-provider/dialog-provider.interfaces";

export const CustomDialog: FunctionComponent = () => {
    const { visible, hideDialog, dialogData } = useContext(DialogContext);
    const { t } = useTranslation();

    const handleDialogClose = (): void => {
        dialogData && dialogData.onCancel && dialogData.onCancel();
        hideDialog();
    };

    const handleOkClick = (): void => {
        dialogData && dialogData.onOk && dialogData.onOk();
        hideDialog();
    };

    return (
        <>
            {dialogData && (
                <Dialog
                    fullWidth
                    disableBackdropClick={
                        !isNil(dialogData.disableBackdropClick)
                            ? dialogData.disableBackdropClick
                            : true
                    }
                    maxWidth={dialogData.width || "xs"}
                    open={visible}
                    onClose={handleDialogClose}
                >
                    {/* Header */}
                    {dialogData.title && (
                        <DialogTitle>{dialogData.title}</DialogTitle>
                    )}

                    {/* Contents */}
                    <DialogContent>
                        {(dialogData as CustomDialogData).children}
                    </DialogContent>

                    {/* Controls */}
                    {(!dialogData.hideOkButton ||
                        !dialogData.hideCancelButton) && (
                        <DialogActions>
                            {/* Cancel button */}
                            {!dialogData.hideCancelButton && (
                                <Button
                                    color="primary"
                                    variant="outlined"
                                    onClick={handleDialogClose}
                                >
                                    {dialogData.cancelButtonLabel ||
                                        t("label.cancel")}
                                </Button>
                            )}

                            {/* Ok button */}
                            {!dialogData.hideOkButton && (
                                <Button
                                    color="primary"
                                    variant="contained"
                                    onClick={handleOkClick}
                                >
                                    {dialogData.okButtonLabel || t("label.ok")}
                                </Button>
                            )}
                        </DialogActions>
                    )}
                </Dialog>
            )}
        </>
    );
};
