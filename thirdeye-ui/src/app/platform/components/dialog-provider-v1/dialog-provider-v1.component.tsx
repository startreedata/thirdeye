import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from "@material-ui/core";
import classNames from "classnames";
import * as React from "react";
import { createContext, FunctionComponent, useContext, useState } from "react";
import { SyntheticEvent } from "react-transition-group/node_modules/@types/react";
import {
    DialogDataV1,
    DialogProviderV1ContextProps,
    DialogProviderV1Props,
    DialogType,
} from "./dialog-provider-v1.interfaces";

export const DialogProviderV1: FunctionComponent<DialogProviderV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const [visible, setVisible] = useState(false);
    const [dialogData, setDialogData] = useState<DialogDataV1 | null>(null);

    const showDialog = (dialogData: DialogDataV1): void => {
        setVisible(true);
        setDialogData(dialogData);
    };

    const hideDialog = (): void => {
        setVisible(false);
        setDialogData(null);
    };

    const handleClose = (_event: SyntheticEvent, reason: string): void => {
        if (!dialogData) {
            return;
        }

        if (reason === "backdropClick") {
            // Close on back drop click to be disabled
            return;
        }

        dialogData.onCancel && dialogData.onCancel();
        hideDialog();
    };

    const handleCancelClick = (): void => {
        if (!dialogData) {
            return;
        }

        dialogData.onCancel && dialogData.onCancel();
        hideDialog();
    };

    const handleOkClick = (): void => {
        if (!dialogData) {
            return;
        }

        const proceed = !dialogData.onBeforeOk || dialogData.onBeforeOk();
        if (!proceed) {
            return;
        }

        dialogData.onOk && dialogData.onOk();
        hideDialog();
    };

    const dialogContext = {
        visible: visible,
        showDialog: showDialog,
        hideDialog: hideDialog,
    };

    return (
        <DialogProviderV1Context.Provider value={dialogContext}>
            {children}

            {/* Dialog */}
            {dialogData && visible && (
                <Dialog
                    {...otherProps}
                    fullWidth
                    className={classNames(className, "dialog-provider-v1")}
                    maxWidth={dialogData.width || "xs"}
                    open={visible}
                    scroll="body"
                    onClose={handleClose}
                >
                    {/* Header */}
                    {dialogData.headerText && (
                        <DialogTitle className="dialog-provider-v1-header">
                            {dialogData.headerText}
                        </DialogTitle>
                    )}

                    {/* Contents */}
                    {dialogData.contents && (
                        <>
                            {/* Custom contents */}
                            {dialogData.customContents && (
                                <>{dialogData.customContents}</>
                            )}

                            {/* Default contents */}
                            {!dialogData.customContents && (
                                <DialogContent>
                                    {dialogData.type === DialogType.ALERT ? (
                                        <DialogContentText>
                                            {dialogData.contents}
                                        </DialogContentText>
                                    ) : (
                                        dialogData.contents
                                    )}
                                </DialogContent>
                            )}
                        </>
                    )}

                    {/* Controls */}
                    {!dialogData.customContents &&
                        (!dialogData.hideOkButton ||
                            !dialogData.hideCancelButton) && (
                            <DialogActions>
                                {/* Cancel button */}
                                {!dialogData.hideCancelButton && (
                                    <Button
                                        className="dialog-provider-v1-cancel-button"
                                        disabled={
                                            dialogData.disableCancelButton
                                        }
                                        onClick={handleCancelClick}
                                    >
                                        {dialogData.cancelButtonText}
                                    </Button>
                                )}

                                {/* Ok button */}
                                {!dialogData.hideOkButton && (
                                    <Button
                                        className="dialog-provider-v1-ok-button"
                                        color="primary"
                                        disabled={dialogData.disableOkButton}
                                        onClick={handleOkClick}
                                    >
                                        {dialogData.okButtonText}
                                    </Button>
                                )}
                            </DialogActions>
                        )}
                </Dialog>
            )}
        </DialogProviderV1Context.Provider>
    );
};

const DialogProviderV1Context = createContext<DialogProviderV1ContextProps>(
    {} as DialogProviderV1ContextProps
);

export const useDialogProviderV1 = (): DialogProviderV1ContextProps => {
    return useContext(DialogProviderV1Context);
};
