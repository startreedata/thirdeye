import * as React from "react";
import { createContext, FunctionComponent, useContext, useState } from "react";
import { AlertDialog } from "../alert-dialog/alert-dialog.component";
import {
    DialogContextProps,
    DialogData,
    DialogProviderProps,
    DialogType,
    UseDialogProps,
} from "./dialog-provider.interfaces";

export const DialogProvider: FunctionComponent<DialogProviderProps> = (
    props: DialogProviderProps
) => {
    const [visible, setVisible] = React.useState(false);
    const [dialogData, setDialogData] = useState<DialogData | null>(null);

    const showDialog = (dialogData: DialogData): void => {
        setVisible(true);
        setDialogData(dialogData);
    };

    const hideDialog = (): void => {
        setVisible(false);
        setDialogData(null);
    };

    const dialogContextProps: DialogContextProps = {
        visible: visible,
        showDialog: showDialog,
        hideDialog: hideDialog,
        dialogData: dialogData as DialogData,
    };

    return (
        <DialogContext.Provider value={dialogContextProps}>
            {props.children}

            {/* Alert dialog */}
            {visible && dialogData && dialogData.type === DialogType.ALERT && (
                <AlertDialog />
            )}
        </DialogContext.Provider>
    );
};

export const DialogContext = createContext<DialogContextProps>(
    {} as DialogContextProps
);

export const useDialog = (): UseDialogProps => {
    return useContext(DialogContext);
};
