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
import { ReactNode } from "react";
import { Crumb } from "../breadcrumbs/breadcrumbs.interfaces";

export interface PageHeaderProps {
    title?: string;
    subtitle?: ReactNode;
    showTimeRange?: boolean;
    showCreateButton?: boolean;
    transparentBackground?: boolean;
    breadcrumbs?: Crumb[];
    children?: ReactNode;
    customActions?: ReactNode;
    subNavigation?: SubNavigationConfiguration[];
    subNavigationSelected?: number;
}

export interface SubNavigationConfiguration extends Crumb {
    link: string;
}
