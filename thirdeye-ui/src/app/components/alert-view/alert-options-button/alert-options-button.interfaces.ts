/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { MouseEvent, ReactNode } from "react";
import { Alert } from "../../../rest/dto/alert.interfaces";

export interface AlertOptionsButtonProps {
    alert: Alert;
    onChange?: (alert: Alert) => void;
    onDelete?: (alert: Alert) => void;
    onReset?: (alert: Alert) => void;
    showViewDetails?: boolean;
    openButtonRenderer?: (
        clickCallback: (event: MouseEvent<HTMLElement>) => void
    ) => ReactNode;
}
