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
import {
    AlertEvaluation,
    EditableAlert,
} from "../../../../rest/dto/alert.interfaces";
import { TimeRange } from "../../../../rest/dto/time-range.interfaces";
import { LegendPlacement } from "../../../alert-wizard-v2/alert-template/preview-chart/preview-chart.interfaces";

export interface ChartContentProps {
    alertEvaluation: AlertEvaluation | null;
    onReloadClick: () => void;
    showLoadButton: boolean;
    showOnlyActivity?: boolean;
    hideCallToActionPrompt?: boolean;
    showDeleteIcon?: boolean;
    isSearchEnabled?: boolean;
    alert: EditableAlert;
    onAlertPropertyChange?: (contents: Partial<EditableAlert>) => void;
    evaluationTimeRange: TimeRange;
    legendsPlacement?: `${LegendPlacement}`;
    additionalCTA?: ReactNode;
}
