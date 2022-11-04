// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
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
    id: string;
    type: NotificationTypeV1;
    message: string;
    nonDismissible: boolean;
    scope: NotificationScopeV1;
    onDismiss?: () => void;
    createdAt: number;
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
