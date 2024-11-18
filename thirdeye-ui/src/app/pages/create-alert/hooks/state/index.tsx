/*
 * Copyright 2024 StarTree Inc
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
// external
import create from "zustand";

// utils
import { DatasetInfo } from "../../../../utils/datasources/datasources.util";
import { GranularityValue } from "../../../../components/alert-wizard-v3/select-metric/select-metric.utils";

// types
import {
    AlertEvaluation,
    AlertInsight,
    EditableAlert,
} from "../../../../rest/dto/alert.interfaces";
import { SelectDimensionsOptions } from "../../../../rest/dto/metric.interfaces";
import { CohortTableRowData } from "../../../../components/cohort-detector/cohorts-table-v2/cohorts-table.interfaces";
import { AlertTemplate } from "../../../../rest/dto/alert-template.interfaces";
import { AvailableAlgorithmOption } from "../../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.interfaces";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { ErrorMessage } from "../../../../platform/components/notification-provider-v1/notification-provider-v1/notification-provider-v1.interfaces";
import { SubscriptionGroup } from "../../../../rest/dto/subscription-group.interfaces";

export type EnumerationItem = { params: { queryFilters: string } };
type DateRange = { startTime: number; endTime: number } | null;
type RequestState = { status?: ActionStatus; errorMessages?: ErrorMessage[] };
type ApiState = {
    evaluationState: RequestState | null;
    insightState: RequestState | null;
};

export type MultipleDimensionEnumeratorOptions =
    | SelectDimensionsOptions.DIMENSION_RECOMMENDER
    | SelectDimensionsOptions.ENUMERATORS;
export type AlertRecommendations = { alert: EditableAlert }[];

type CreateAlertStore = {
    apiState: ApiState;
    alertTemplates: AlertTemplate[] | null;
    selectedDataset: DatasetInfo | null;
    selectedMetric: string | null;
    aggregationFunction: string | null;
    viewColumnsListDrawer: boolean;
    granularity: GranularityValue | undefined;
    queryFilters: string;
    editedDatasourceFieldValue: string;
    anomalyDetectionType: string | null;
    alertInsight: AlertInsight | null;
    workingAlert: Partial<EditableAlert>;
    workingAlertEvaluation: AlertEvaluation | null;
    multipleDimensionEnumeratorType: MultipleDimensionEnumeratorOptions | null;
    enumeratorQuery: string | "";
    alertRecommendations: AlertRecommendations | null;
    selectedEnumerationItems: EnumerationItem[] | null;
    selectedEnumerationItemsCohortsTable: CohortTableRowData[] | [];
    selectedDetectionAlgorithm: AvailableAlgorithmOption | null;
    selectedTimeRange: DateRange;
    isEvaluationDataStale: boolean;
    selectedExistingSubscriptionGroups: SubscriptionGroup[] | null;
    newSubscriptionGroup: SubscriptionGroup | null;
    setApiState: (apiState: ApiState) => void;
    setAlertTemplates: (alertTemplates: AlertTemplate[] | null) => void;
    setSelectedDataset: (dataset: DatasetInfo) => void;
    setSelectedMetric: (metric: string) => void;
    setAggregationFunction: (aggregationFUntion: string) => void;
    setGranularity: (granularity: GranularityValue | undefined) => void;
    setQueryFilters: (queryFilters: string) => void;
    setViewColumnsListDrawer: (open: boolean) => void;
    setEditedDatasourceFieldValue: (editedDatasourceFieldValue: string) => void;
    setAnomalyDetectionType: (anomalyDetectionType: string) => void;
    setAlertInsight: (alertInsight: AlertInsight | null) => void;
    setWorkingAlert: (workingAlert: Partial<EditableAlert>) => void;
    setWorkingAlertEvaluation: (
        workingAlertEvaluation: AlertEvaluation | null
    ) => void;
    setMultipleDimensionEnumeratorType: (
        multipleDimensionEnumeratorType: MultipleDimensionEnumeratorOptions
    ) => void;
    setEnumeratorQuery: (enumeratorQuery: string) => void;
    setAlertRecommendations: (
        alertRecommendations: AlertRecommendations
    ) => void;
    setSelectedEnumerationItems: (enumerationItems: EnumerationItem[]) => void;
    setSelectedEnumerationItemsCohortsTable: (
        selectedEnumerationItemsCohortsTable: CohortTableRowData[]
    ) => void;
    setSelectedDetectionAlgorithm: (
        detectionAlgorithm: AvailableAlgorithmOption
    ) => void;
    setSelectedTimeRange: (timeRange: DateRange) => void;
    setIsEvaluationDataStale: (isStale: boolean) => void;
    setSelectedExistingSubscriptiongroup: (groups: SubscriptionGroup[]) => void;
    setNewSubscriptionGroup: (group: SubscriptionGroup) => void;
    resetCreateAlertState: () => void;
};

export const useCreateAlertStore = create<CreateAlertStore>((set) => ({
    apiState: {
        evaluationState: null,
        insightState: null,
    },
    alertTemplates: null,
    selectedDataset: null,
    selectedMetric: null,
    aggregationFunction: null,
    granularity: undefined,
    queryFilters: "",
    viewColumnsListDrawer: false,
    editedDatasourceFieldValue: "",
    anomalyDetectionType: null,
    alertInsight: null,
    workingAlert: {},
    workingAlertEvaluation: null,
    multipleDimensionEnumeratorType: null,
    enumeratorQuery: "",
    alertRecommendations: null,
    dimensionRecommendor: {
        selectedDimensions: [],
        queryFilter: "",
        percentageContribution: 5,
    },
    selectedEnumerationItems: [],
    selectedEnumerationItemsCohortsTable: [],
    selectedDetectionAlgorithm: null,
    selectedTimeRange: null,
    isEvaluationDataStale: false,
    selectedExistingSubscriptionGroups: null,
    newSubscriptionGroup: null,
    setAlertTemplates: (alertTemplates: AlertTemplate[] | null) =>
        set({
            alertTemplates,
        }),
    setApiState: (apiState: ApiState) =>
        set({
            apiState,
        }),
    setSelectedDataset: (dataset: DatasetInfo) =>
        set({
            selectedDataset: dataset,
            selectedMetric: null,
            aggregationFunction: null,
            granularity: undefined,
            anomalyDetectionType: null,
            workingAlert: {},
            selectedDetectionAlgorithm: null,
            enumeratorQuery: "",
            selectedEnumerationItems: null,
            workingAlertEvaluation: null,
        }),
    setSelectedMetric: (metric: string) =>
        set({
            selectedMetric: metric,
            aggregationFunction: null,
            granularity: undefined,
            editedDatasourceFieldValue: "",
            anomalyDetectionType: null,
            workingAlert: {},
            selectedDetectionAlgorithm: null,
            enumeratorQuery: "",
            selectedEnumerationItems: null,
            workingAlertEvaluation: null,
        }),
    setAggregationFunction: (aggregationFunction: string) =>
        set({
            aggregationFunction: aggregationFunction,
            granularity: undefined,
            anomalyDetectionType: null,
            workingAlert: {},
            selectedDetectionAlgorithm: null,
            enumeratorQuery: "",
            selectedEnumerationItems: null,
            workingAlertEvaluation: null,
        }),
    setGranularity: (granulariy: GranularityValue | undefined) =>
        set({
            granularity: granulariy,
            anomalyDetectionType: null,
            selectedDetectionAlgorithm: null,
            enumeratorQuery: "",
            selectedEnumerationItems: null,
            workingAlertEvaluation: null,
        }),
    setQueryFilters: (queryFilters: string) =>
        set({
            queryFilters: queryFilters,
            selectedDetectionAlgorithm: null,
            enumeratorQuery: "",
        }),
    setViewColumnsListDrawer: (viewColumnsListDrawer: boolean) =>
        set({ viewColumnsListDrawer: viewColumnsListDrawer }),
    setEditedDatasourceFieldValue: (editedDatasourceFieldValue: string) =>
        set({
            editedDatasourceFieldValue: editedDatasourceFieldValue,
            selectedDetectionAlgorithm: null,
        }),
    setAnomalyDetectionType: (anomalyDetectionType: string) =>
        set({
            anomalyDetectionType: anomalyDetectionType,
            multipleDimensionEnumeratorType: null,
            enumeratorQuery: "",
            selectedDetectionAlgorithm: null,
            selectedEnumerationItems: null,
            workingAlertEvaluation: null,
        }),
    setAlertInsight: (alertInsight: AlertInsight | null) =>
        set({
            alertInsight: alertInsight,
            workingAlertEvaluation: null,
        }),
    setWorkingAlert: (workingAlert: Partial<EditableAlert>) =>
        set({
            workingAlert: workingAlert,
            // workingAlertEvaluation: null
        }),
    setWorkingAlertEvaluation: (evaluation: AlertEvaluation | null) =>
        set({
            workingAlertEvaluation: evaluation,
        }),
    setMultipleDimensionEnumeratorType: (
        multipleDimensionEnumeratorType: MultipleDimensionEnumeratorOptions
    ) =>
        set({
            multipleDimensionEnumeratorType: multipleDimensionEnumeratorType,
            selectedDetectionAlgorithm: null,
            selectedEnumerationItems: null,
            enumeratorQuery: "",
            workingAlertEvaluation: null,
        }),
    setEnumeratorQuery: (enumeratorQuery: string) =>
        set({
            enumeratorQuery: enumeratorQuery,
            selectedDetectionAlgorithm: null,
            selectedEnumerationItems: null,
        }),
    setAlertRecommendations: (alertRecommendations: AlertRecommendations) =>
        set({
            alertRecommendations: alertRecommendations,
        }),
    setSelectedEnumerationItems: (enumerationItems: EnumerationItem[]) =>
        set({
            selectedEnumerationItems: enumerationItems,
        }),
    setSelectedEnumerationItemsCohortsTable: (
        selectedEnumerationItemsCohortsTable: CohortTableRowData[]
    ) =>
        set({
            selectedEnumerationItemsCohortsTable,
        }),
    setSelectedDetectionAlgorithm: (
        detectionAlgorithm: AvailableAlgorithmOption
    ) =>
        set({
            selectedDetectionAlgorithm: detectionAlgorithm,
            workingAlertEvaluation: null,
        }),
    setSelectedTimeRange: (timeRange: DateRange | null) =>
        set({
            selectedTimeRange: timeRange,
        }),
    setIsEvaluationDataStale: (isStale: boolean) =>
        set({
            isEvaluationDataStale: isStale,
        }),
    setSelectedExistingSubscriptiongroup: (groups: SubscriptionGroup[]) =>
        set({
            selectedExistingSubscriptionGroups: groups,
        }),
    setNewSubscriptionGroup: (group: SubscriptionGroup) =>
        set({
            newSubscriptionGroup: group,
        }),
    resetCreateAlertState: () =>
        set({
            apiState: {
                evaluationState: null,
                insightState: null,
            },
            alertTemplates: null,
            selectedDataset: null,
            selectedMetric: null,
            aggregationFunction: null,
            granularity: undefined,
            queryFilters: "",
            viewColumnsListDrawer: false,
            editedDatasourceFieldValue: "",
            anomalyDetectionType: null,
            alertInsight: null,
            workingAlert: {},
            workingAlertEvaluation: null,
            multipleDimensionEnumeratorType: null,
            enumeratorQuery: "",
            alertRecommendations: null,
            selectedEnumerationItems: [],
            selectedEnumerationItemsCohortsTable: [],
            selectedDetectionAlgorithm: null,
            selectedTimeRange: null,
            isEvaluationDataStale: false,
            selectedExistingSubscriptionGroups: null,
            newSubscriptionGroup: null,
        }),
}));
