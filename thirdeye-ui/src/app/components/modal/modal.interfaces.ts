/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import { ReactNode } from "react";

export interface ModalProps {
    trigger: (callBack: () => void) => ReactNode;
    submitButtonLabel?: ReactNode;
    cancelButtonLabel?: ReactNode;
    footerActions?: ReactNode;
    onSubmit?: (closeCallback: () => void) => boolean | void;
    onCancel?: () => void;
    onOpen?: () => void;
    title?: ReactNode;
    customTitle?: ReactNode;
    children: ReactNode;
    maxWidth?: "xs" | "sm" | "md" | "lg" | "xl" | false;
    dividers?: boolean;
    initiallyOpen?: boolean;
    disableCancelButton?: boolean;
    disableSubmitButton?: boolean;
}
