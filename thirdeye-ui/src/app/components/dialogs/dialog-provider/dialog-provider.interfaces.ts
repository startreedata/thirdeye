import { ReactNode } from "react";

export interface DialogProviderProps {
    children: ReactNode;
}

export interface UseDialogProps {
    visible: boolean;
    showDialog: (dialogData: AlertDialogData | CustomDialogData) => void;
    hideDialog: () => void;
}

export interface DialogContextProps extends UseDialogProps {
    dialogData: AlertDialogData | CustomDialogData;
}

export interface DialogData {
    type: DialogType;
    title?: string;
    width?: "xs" | "sm" | "md" | "lg" | "xl";
    disableBackdropClick?: boolean;
    hideOkButton?: boolean;
    hideCancelButton?: boolean;
    okButtonLabel?: string;
    cancelButtonLabel?: string;
    onOk?: () => void;
    onCancel?: () => void;
}

export interface AlertDialogData extends DialogData {
    type: DialogType.ALERT;
    text: string;
}

export interface CustomDialogData extends DialogData {
    type: DialogType.CUSTOM;
    children: ReactNode;
}

export enum DialogType {
    ALERT = "ALERT",
    CUSTOM = "CUSTOM",
}
