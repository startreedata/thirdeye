import { ReactNode } from "react";

export interface DialogProviderProps {
    children?: ReactNode;
}

export interface UseDialogProps {
    visible: boolean;
    showDialog: (dialogData: DialogData) => void;
    hideDialog: () => void;
}

export interface DialogContextProps extends UseDialogProps {
    dialogData: DialogData;
}

export interface DialogData {
    type: DialogType;
    title?: string;
    text?: string;
    okButtonLabel?: string;
    cancelButtonLabel?: string;
    onOk?: () => void;
    onCancel?: () => void;
}

export enum DialogType {
    ALERT = "ALERT",
}
