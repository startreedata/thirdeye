import { Box, CardContent } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAnomalyDimensionAnalysis } from "../../rest/rca/rca.actions";
import { AnomalyDimensionAnalysisTable } from "./algorithm-table/algorithm-table.component";
import { AnomalyDimensionAnalysisProps } from "./anomaly-dimension-analysis.interfaces";

export const AnomalyDimensionAnalysis: FunctionComponent<
    AnomalyDimensionAnalysisProps
> = ({ anomalyId, anomaly, comparisonOffset }) => {
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const {
        anomalyDimensionAnalysisData,
        getDimensionAnalysisData,
        status: anomalyDimensionAnalysisReqStatus,
        errorMessages,
    } = useGetAnomalyDimensionAnalysis();
    useEffect(() => {
        getDimensionAnalysisData(anomalyId, {
            baselineOffset: comparisonOffset,
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
        <>
            <CardContent>
                {/* Loading Indicator when request is in flight */}
                {anomalyDimensionAnalysisReqStatus === ActionStatus.Working && (
                    <Box pb={20} pt={20}>
                        <AppLoadingIndicatorV1 />
                    </Box>
                )}

                {anomalyDimensionAnalysisReqStatus === ActionStatus.Done &&
                    anomalyDimensionAnalysisData && (
                        <AnomalyDimensionAnalysisTable
                            anomaly={anomaly}
                            anomalyDimensionAnalysisData={
                                anomalyDimensionAnalysisData
                            }
                            comparisonOffset={comparisonOffset}
                        />
                    )}
            </CardContent>
        </>
    );
};
