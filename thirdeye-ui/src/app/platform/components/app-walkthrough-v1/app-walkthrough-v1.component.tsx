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

import { useTheme } from "@material-ui/core";
import {
    PopoverContentProps,
    ProviderProps,
    TourProvider,
} from "@reactour/tour";
import React, { ComponentType, FunctionComponent, useMemo } from "react";
import { DimensionV1 } from "../../utils";
import { getSteps } from "./app-walkthrough-v1.utils";
import { TourCard } from "./tour-card/tour-card.component";

export const AppWalkthroughV1: FunctionComponent = ({ children }) => {
    const theme = useTheme();

    const tourProps = useMemo<Omit<ProviderProps, "children">>(
        () => ({
            scrollSmooth: true,
            onClickMask: (): void => undefined,
            disableInteraction: true, // Disable interaction for the first iteration
            steps: getSteps(),
            ContentComponent: TourCard as ComponentType<PopoverContentProps>,
            styles: {
                badge: (base) => ({
                    ...base,
                    background: theme.palette.primary.main,
                }),
                dot: (base, options) => ({
                    ...base,
                    background: options?.current
                        ? theme.palette.primary.main
                        : theme.palette.grey["500"],
                }),
                popover: (base) => ({
                    ...base,
                    padding: "6px 8px",
                    borderRadius: DimensionV1.BorderRadiusDefault,
                    minWidth: 300,
                    maxWidth: 600,
                }),
            },
        }),
        []
    );

    return <TourProvider {...tourProps}>{children}</TourProvider>;
};
