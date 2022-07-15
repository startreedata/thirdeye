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
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import {
    TimeRange,
    TimeRangeQueryStringKey,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { isValidNumberId } from "../../utils/params/params.util";
import { getRootCauseAnalysisForAnomalyInvestigatePath } from "../../utils/routes/routes.util";
import { WEEK_IN_MILLISECONDS } from "../../utils/time/time.util";
import { AnomaliesViewPageParams } from "../anomalies-view-page/anomalies-view-page.interfaces";

export const RootCauseAnalysisForAnomalyIndexPage: FunctionComponent = () => {
    const [searchParams] = useSearchParams();
    const {
        anomaly,
        getAnomaly,
        status: anomalyRequestStatus,
        errorMessages,
    } = useGetAnomaly();
    const params = useParams<AnomaliesViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        !!params.id &&
            isValidNumberId(params.id) &&
            getAnomaly(toNumber(params.id));
    }, []);

    useEffect(() => {
        if (anomaly) {
            searchParams.set(
                TimeRangeQueryStringKey.TIME_RANGE,
                TimeRange.CUSTOM
            );
            searchParams.set(
                TimeRangeQueryStringKey.START_TIME,
                (anomaly.startTime - WEEK_IN_MILLISECONDS * 2).toString()
            );
            searchParams.set(
                TimeRangeQueryStringKey.END_TIME,
                (anomaly.endTime + WEEK_IN_MILLISECONDS * 2).toString()
            );

            navigate(
                `${getRootCauseAnalysisForAnomalyInvestigatePath(
                    toNumber(params.id)
                )}?${searchParams.toString()}`,
                {
                    replace: true,
                }
            );
        }
    }, [anomaly]);

    useEffect(() => {
        if (anomalyRequestStatus === ActionStatus.Error) {
            isEmpty(errorMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.anomaly"),
                      })
                  )
                : errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  );
        }
    }, [anomalyRequestStatus, errorMessages]);

    return <AppLoadingIndicatorV1 />;
};
