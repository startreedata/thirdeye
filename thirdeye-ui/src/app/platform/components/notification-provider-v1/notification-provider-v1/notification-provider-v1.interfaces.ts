// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ReactNode } from "react";

export interface NotificationProviderV1Props {
    children?: ReactNode;
}

export interface NotificationProviderV1ContextProps {
    notifications: NotificationV1[];
    notify: (
        type: NotificationTypeV1,
        message: string,
        nonDismissible?: boolean,
        scope?: NotificationScopeV1,
        onDismiss?: () => void
    ) => NotificationV1;
    remove: (notification: NotificationV1) => void;
}

export interface NotificationV1 {
    id: number;
    type: NotificationTypeV1;
    message: string;
    nonDismissible: boolean;
    scope: NotificationScopeV1;
    onDismiss?: () => void;
}

export enum NotificationTypeV1 {
    Error = "error",
    Warning = "warning",
    Info = "info",
    Success = "success",
}

export enum NotificationScopeV1 {
    Page,
    Global,
}
