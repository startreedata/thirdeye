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
import { TourProps, useTour } from "@reactour/tour";
import { useEffect, useMemo } from "react";
import create from "zustand";
import {
    getRcaAnomalyTourSteps,
    RcaAnomalyStepsProps,
} from "./data/rca-anomaly-tour.data";
import {
    getRcaInvestigateTourSteps,
    RcaInvestigateStepsProps,
} from "./data/rca-investigate-tour.data";
import { TOURS } from "./data/tour-ids.data";
import { CommonStepProps, ExtendedStepType } from "./data/tour-utils.data";
// import create from "zustand"

export type SelectorType = "tour" | "tour-observe";

export const getTourSelector = (
    p: string,
    selectorType: SelectorType = "tour"
): string => `[data-${selectorType}-id='${p}']`;

export type TourType = keyof typeof TOURS;

export type StepPropsType = RcaInvestigateStepsProps | RcaAnomalyStepsProps;

// export type GetSpecificTourSteps = <T, K>(
//     p: T & CommonStepProps
// ) => ExtendedStepType<K>[];

const tourStepsMap = {
    [TOURS.RCA_ANOMALY as TourType]: getRcaAnomalyTourSteps,
    [TOURS.RCA_INVESTIGATE as TourType]: getRcaInvestigateTourSteps,
};

console.log({
    getRcaAnomalyTourSteps,
    getRcaInvestigateTourSteps,
    tourStepsMap,
});

export const getTourSteps = <T,>(
    tourType: TourType,
    stepProps: T & CommonStepProps
): ExtendedStepType[] => {
    const getSelectedSteps = tourStepsMap?.[tourType];
    const selectedSteps = getSelectedSteps?.(stepProps) || [];

    console.log("Tour Steps", [tourStepsMap, tourType, tourStepsMap[tourType]]);

    if (!(selectedSteps && selectedSteps.length)) {
        console.log("No Steps found", [
            tourStepsMap,
            tourType,
            tourStepsMap[tourType],
        ]);

        return [];
    }

    // TODO: Remove if no better use is found
    stepProps && console.log({ stepProps });

    console.log({ selectedSteps });

    return (selectedSteps || []).map((s) => ({
        ...s,
        ...(s.selector && {
            selector: getTourSelector(s.selector as string),
        }),
        ...(s.highlightedSelectors && {
            highlightedSelectors: s.highlightedSelectors.map((v) =>
                getTourSelector(v, "tour-observe")
            ),
        }),
        ...(s.highlightedSelectors && {
            highlightedSelectors: s.highlightedSelectors.map((v) =>
                getTourSelector(v, "tour-observe")
            ),
        }),
        ...(s.resizeObservables && {
            resizeObservables: s.resizeObservables.map((v) =>
                getTourSelector(v, "tour-observe")
            ),
        }),
        ...(s.mutationObservables && {
            mutationObservables: s.mutationObservables.map((v) =>
                getTourSelector(v, "tour-observe")
            ),
        }),
    }));
};

export interface TourStore {
    stepProps: Partial<Record<TourType, unknown>>;
    setStepProps: <T extends unknown>(k: TourType, v: Partial<T>) => void;
}

const useTourStore = create<TourStore>((set) => ({
    stepProps: {},
    setStepProps: (k, v) =>
        set((state) => ({
            ...state,
            stepProps: { ...state.stepProps, [k]: v },
        })),
}));

export type UseAppTourReturn<T> = {
    startTour: () => void;
    stepProps: T;
    setStepProps: (p: T) => void;
} & TourProps;

// Use this hook throughout the app, as this will expose the minimal interface required
// to use the in-app tour while handling all the common abstraction behind the scenes
export const useAppTour = <T extends Partial<StepPropsType>>(
    tourType: TourType
): UseAppTourReturn<T> => {
    const tourContext = useTour();

    const tourStore = useTourStore((s) => s);

    const { setIsOpen, setSteps, setCurrentStep, currentStep } = tourContext;

    const baseStepProps = (tourStore.stepProps?.[tourType] || {}) as T;

    const commonStepProps: CommonStepProps = {
        setCurrentStep,
        currentStep,
    };

    const setStepProps = (tourProps: Partial<T>): void => {
        tourStore.setStepProps<T>(tourType, tourProps);
    };

    const tourSteps = useMemo(
        () =>
            getTourSteps<T>(tourType, { ...baseStepProps, ...commonStepProps }),
        [baseStepProps]
    );

    useEffect(() => {
        setSteps(tourSteps);
        console.log({ tourSteps });
    }, [tourSteps]);

    const startTour = (): void => {
        setIsOpen(true);
    };

    return {
        startTour,
        stepProps: baseStepProps,
        setStepProps,
        ...tourContext,
    };
};
