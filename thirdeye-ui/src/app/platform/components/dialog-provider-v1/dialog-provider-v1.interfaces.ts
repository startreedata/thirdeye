// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ReactNode } from "react";

export enum DialogType {
    ALERT = "ALERT",
    CUSTOM = "CUSTOM",
}

export interface DialogProviderV1Props {
    className?: string;
    children?: ReactNode;
}

export interface DialogProviderV1ContextProps {
    visible: boolean;
    showDialog: (dialogData: DialogDataV1) => void;
    hideDialog: () => void;
}

export interface DialogDataV1 {
    width?: "xs" | "sm" | "md" | "lg" | "xl";
    headerText?: string;
    type: DialogType;
    contents?: ReactNode;
    customContents?: boolean;
    hideOkButton?: boolean;
    disableOkButton?: boolean;
    hideCancelButton?: boolean;
    disableCancelButton?: boolean;
    okButtonText?: string;
    cancelButtonText?: string;
    onBeforeOk?: () => boolean;
    onOk?: () => void;
    onCancel?: () => void;
}
