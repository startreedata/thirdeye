/*
 * Copyright 2025 StarTree Inc
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
import type { TypographyProps } from "@material-ui/core";
import type { ReactElement } from "react";
import { SkeletonV1Props } from "../../../../../platform/components/skeleton-v1/skeleton-v1.interfaces";

export interface AlertAccuracyColoredProps {
    alertId: number;
    renderCustomLoading?: ReactElement;
    defaultSkeletonProps?: SkeletonV1Props;
    typographyProps?: Partial<TypographyProps>;
    label?: string;
    start?: number;
    end?: number;
    alertStats?: any;
    triggerViewCallback?: any;
}
