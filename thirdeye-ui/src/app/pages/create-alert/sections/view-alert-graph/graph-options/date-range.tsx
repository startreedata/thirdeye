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
import React, { useEffect } from "react";
import { Box, Typography } from "@material-ui/core";
import { useTranslation } from "react-i18next";
import { cloneDeep } from "lodash";

// app components
import { TimeRangeButtonWithContext } from "../../../../../components/time-range/time-range-button-with-context-v2/time-range-button.component";
import { useNotificationProviderV1 } from "../../../../../platform/components";

// styles
import { graphOptionsStyles } from "./styles";

// state
import { useCreateAlertStore } from "../../../hooks/state";

// utils
import {
    createAlertEvaluation,
    determineTimezoneFromAlertInEvaluation,
} from "../../../../../utils/alerts/alerts.util";
import { notifyIfErrors } from "../../../../../utils/notifications/notifications.util";

// apis
import { useGetEvaluation } from "../../../../../rest/alerts/alerts.actions";

// types
import { EditableAlert } from "../../../../../rest/dto/alert.interfaces";
import { TimeRange } from "../../../../../components/time-range/time-range-provider/time-range-provider.interfaces";

export const DateRange = (): JSX.Element => {
    const { t } = useTranslation();
    const componentStyles = graphOptionsStyles();
    const { notify } = useNotificationProviderV1();
    const {
        alertInsight,
        workingAlert,
        setWorkingAlertEvaluation,
        setSelectedTimeRange,
        apiState,
        setApiState,
    } = useCreateAlertStore();
    const { evaluation, getEvaluation, status, errorMessages } =
        useGetEvaluation();

    useEffect(() => {
        setApiState({
            ...apiState,
            evaluationState: {
                ...apiState.evaluationState,
                status,
            },
        });
    }, [status]);

    const fetchAlertEvaluation = (startTime: number, endTime: number): void => {
        const clonedAlert = cloneDeep(workingAlert) as EditableAlert;
        setSelectedTimeRange({ startTime: startTime, endTime: endTime });
        const hasEnumerationItems =
            !!clonedAlert.templateProperties?.enumeratorQuery ||
            !!clonedAlert.templateProperties?.enumerationItems;
        getEvaluation(
            createAlertEvaluation(clonedAlert, startTime, endTime, {
                listEnumerationItemsOnly: hasEnumerationItems,
            })
        );
    };

    useEffect(() => {
        evaluation && setWorkingAlertEvaluation(evaluation);
    }, [evaluation]);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alert-evaluation"),
            })
        );
    }, [status]);

    return (
        <>
            <Box>
                <Typography
                    className={componentStyles.inputHeader}
                    variant="caption"
                >
                    {t("label.date-range")}:
                </Typography>
            </Box>
            <TimeRangeButtonWithContext
                hideQuickExtend
                btnGroupColor="default"
                defaultTimeRange={{
                    startTime: alertInsight!.defaultStartTime!,
                    endTime: alertInsight!.defaultEndTime!,
                    timeRange: TimeRange.CUSTOM,
                }}
                maxDate={alertInsight?.datasetEndTime}
                minDate={alertInsight?.datasetStartTime}
                timezone={determineTimezoneFromAlertInEvaluation(
                    alertInsight?.templateWithProperties
                )}
                onTimeRangeChange={(newStart, newEnd) => {
                    fetchAlertEvaluation(newStart, newEnd);
                }}
            />
        </>
    );
};
