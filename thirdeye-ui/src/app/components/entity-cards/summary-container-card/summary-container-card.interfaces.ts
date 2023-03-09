/*
 * Copyright 2022 StarTree Inc
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
import { GridProps } from "@material-ui/core";
import { ReactNode } from "react";
import { PageContentsCardV1Props } from "../../../platform/components/page-v1/page-contents-grid-v1/page-contents-card-v1/page-contents-card-v1.interfaces";

export interface SummaryContainerCardItem extends GridProps {
    key?: string;
    content: ReactNode;
}

export interface SummaryContainerCardProps {
    gridContainerProps?: Partial<GridProps>;
    rootCardProps?: Partial<PageContentsCardV1Props>;
    items: SummaryContainerCardItem[];
}
