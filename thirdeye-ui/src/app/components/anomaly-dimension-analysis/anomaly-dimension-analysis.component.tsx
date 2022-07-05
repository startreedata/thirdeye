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
import { CardContent } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { AnomalyDimensionAnalysisData } from "../../rest/dto/rca.interfaces";
import { useGetAnomalyDimensionAnalysis } from "../../rest/rca/rca.actions";
import { getFilterDimensionAnalysisData } from "../../utils/anomaly-dimension-analysis/anomaly-dimension-analysis";
import { AnomalyDimensionAnalysisTable } from "./algorithm-table/algorithm-table.component";
import { AnomalyDimensionAnalysisProps } from "./anomaly-dimension-analysis.interfaces";

export const AnomalyDimensionAnalysis: FunctionComponent<
    AnomalyDimensionAnalysisProps
> = ({
    anomalyId,
    anomaly,
    comparisonOffset,
    chartTimeSeriesFilterSet,
    onCheckClick,
}) => {
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const {
        getDimensionAnalysisData,
        status: anomalyDimensionAnalysisReqStatus,
        errorMessages,
    } = useGetAnomalyDimensionAnalysis();

    const [anomalyDimensionAnalysisData, setAnomalyDimensionAnalysisData] =
        useState<AnomalyDimensionAnalysisData | null>();

    useEffect(() => {
        getDimensionAnalysisData(anomalyId, {
            baselineOffset: comparisonOffset,
        }).then((data: AnomalyDimensionAnalysisData | undefined) => {
            if (data) {
                setAnomalyDimensionAnalysisData(
                    getFilterDimensionAnalysisData(data)
                );
            }
        });
    }, [anomalyId, comparisonOffset]);

    useEffect(() => {
        if (anomalyDimensionAnalysisReqStatus === ActionStatus.Error) {
            !isEmpty(errorMessages)
                ? errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.dimension-analysis-data"),
                      })
                  );
        }
    }, [anomalyDimensionAnalysisReqStatus]);

    return (
        <CardContent>
            {/* Loading Indicator when request is in flight */}
            {anomalyDimensionAnalysisReqStatus === ActionStatus.Working && (
                <SkeletonV1 preventDelay height={200} variant="rect" />
            )}

            {anomalyDimensionAnalysisReqStatus === ActionStatus.Done &&
                anomalyDimensionAnalysisData && (
                    <AnomalyDimensionAnalysisTable
                        anomaly={anomaly}
                        anomalyDimensionAnalysisData={
                            anomalyDimensionAnalysisData
                        }
                        chartTimeSeriesFilterSet={chartTimeSeriesFilterSet}
                        comparisonOffset={comparisonOffset}
                        onCheckClick={onCheckClick}
                    />
                )}
        </CardContent>
    );
};
