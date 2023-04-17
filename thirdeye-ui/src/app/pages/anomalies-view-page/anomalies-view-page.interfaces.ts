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
import { AlertInsight } from "../../rest/dto/alert.interfaces";
import { Anomaly, AnomalyFeedback } from "../../rest/dto/anomaly.interfaces";
import { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";

export const OFFSET_REGEX_EXTRACT = /[pP](-?\d+)([DWMY])/;

export type AnomaliesViewPageParams = {
    id: string;
};

export const OFFSET_TO_HUMAN_READABLE: { [key: string]: string } = {
    D: "Day",
    W: "Week",
    M: "Month",
    Y: "Year",
};

export interface AnomalyViewContainerPageOutletContext {
    anomaly: Anomaly;
    handleFeedbackUpdateSuccess: (feedback: AnomalyFeedback) => void;
    enumerationItem?: EnumerationItem;
    alertInsight: AlertInsight | null;
}
