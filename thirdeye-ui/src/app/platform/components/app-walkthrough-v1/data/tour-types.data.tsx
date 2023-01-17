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

import { Box, Typography } from "@material-ui/core";
import { StepType, TourProps } from "@reactour/tour";
import { Observables } from "@reactour/utils";
import React, { ReactNode } from "react";
import { getTourSelector } from "../app-walkthrough-v1.utils";
import { RCA_ANOMALY_TOUR_IDS } from "./rca-anomaly-tour.data";
import { RCA_INVESTIGATE_TOUR_IDS } from "./rca-investigate-tour.data";

export const TOURS = {
    RCA_ANOMALY: "RCA_ANOMALY",
    RCA_INVESTIGATE: "RCA_INVESTIGATE",
} as const;

export type ValueOf<T> = T[keyof T];

// * Keep adding tour ids here to get ts support across the in-app tour implementation for them
export type AllTourKeys = typeof RCA_ANOMALY_TOUR_IDS &
    typeof RCA_INVESTIGATE_TOUR_IDS;

export type StepId<T = AllTourKeys> = ValueOf<T>;

export interface ExtendedStepType<T = AllTourKeys> extends StepType {
    id: StepId<T>;
    nextLabel?: string;
    disableNext?: boolean; // Disable manual "next" navigation. Useful if the user needs to interact
}

export type CommonStepProps = Pick<TourProps, "setCurrentStep" | "currentStep">;

export const getContentElement = (
    params:
        | string
        | {
              title?: string;
              body?: string | ReactNode;
              forceRedraw?: boolean;
              forceRedrawProps?: {
                  mutationObservable: string[];
                  setCurrentStep: (p: React.SetStateAction<number>) => void;
              };
          }
): ReactNode => {
    if (typeof params === "string") {
        return <Typography variant="body2">{params}</Typography>;
    }

    return (
        <Box>
            {params.title ? (
                <Typography paragraph variant="h5">
                    {params.title}
                </Typography>
            ) : null}
            {params.body ? (
                <Typography paragraph={false} variant="body2">
                    {params.body}
                </Typography>
            ) : null}
            {params.forceRedraw && params?.forceRedrawProps ? (
                <Observables
                    mutationObservables={params.forceRedrawProps?.mutationObservable.map(
                        (v) => getTourSelector(v)
                    )}
                    refresh={(isInViewNow: boolean) => {
                        if (isInViewNow) {
                            params?.forceRedrawProps?.setCurrentStep?.(
                                (n) => n - 1
                            );
                            params?.forceRedrawProps?.setCurrentStep?.(
                                (n) => n + 1
                            );
                        }
                    }}
                />
            ) : null}
        </Box>
    );
};

export const transformTourSteps =
    <T, P = unknown>(
        stepsList: (p: Partial<P> & CommonStepProps) => StepType[]
    ): ((q: Parameters<typeof stepsList>[0]) => ExtendedStepType<T>[]) =>
    (stepProps) =>
        stepsList(stepProps).map<ExtendedStepType<T>>((step) => ({
            ...step,
            id: step.selector as StepId<T>,
        }));
