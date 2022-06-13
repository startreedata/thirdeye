// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

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
