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
import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { AnomaliesPageHeader } from "../../components/anomalies-page-header/anomalies-page-header.component";
import { AnomalyFilterQueryStringKey } from "../../components/anomaly-quick-filters/anomaly-quick-filter.interface";
import { MetricsReportList } from "../../components/metrics-report-list/metrics-report-list.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { GetAnomaliesProps } from "../../rest/anomalies/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { getUiAnomalies } from "../../utils/anomalies/anomalies.util";

export const MetricsReportAllPage: FunctionComponent = () => {
    const [searchParams] = useSearchParams();
    const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(null);
    const {
        getAnomalies,
        status: getAnomaliesRequestStatus,
        errorMessages: anomaliesRequestErrors,
    } = useGetAnomalies();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch anomalies
        fetchAnomaliesByTime();
    }, [searchParams]);

    useEffect(() => {
        if (
            getAnomaliesRequestStatus === ActionStatus.Done &&
            uiAnomalies &&
            uiAnomalies.length === 0
        ) {
            notify(
                NotificationTypeV1.Info,
                t("message.no-data-for-entity-for-date-range", {
                    entity: t("label.anomalies"),
                })
            );
        }
    }, [getAnomaliesRequestStatus, uiAnomalies]);

    const fetchAnomaliesByTime = (): void => {
        setUiAnomalies(null);

        const start = searchParams.get(TimeRangeQueryStringKey.START_TIME);
        const end = searchParams.get(TimeRangeQueryStringKey.END_TIME);
        const params: GetAnomaliesProps = {
            startTime: Number(start),
            endTime: Number(end),
        };

        if (searchParams.has(AnomalyFilterQueryStringKey.ALERT)) {
            params.alertId = parseInt(
                searchParams.get(AnomalyFilterQueryStringKey.ALERT) || ""
            );
        }

        if (searchParams.has(AnomalyFilterQueryStringKey.DATASET)) {
            params.dataset = searchParams.get(
                AnomalyFilterQueryStringKey.DATASET
            ) as string;
        }

        if (searchParams.has(AnomalyFilterQueryStringKey.METRIC)) {
            params.metric = searchParams.get(
                AnomalyFilterQueryStringKey.METRIC
            ) as string;
        }

        let fetchedUiAnomalies: UiAnomaly[] = [];

        getAnomalies(params)
            .then((anomalies) => {
                if (anomalies && anomalies.length) {
                    fetchedUiAnomalies = getUiAnomalies(anomalies);
                }
            })
            .finally(() => setUiAnomalies(fetchedUiAnomalies));
    };

    useEffect(() => {
        if (getAnomaliesRequestStatus === ActionStatus.Error) {
            !isEmpty(anomaliesRequestErrors)
                ? anomaliesRequestErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.anomalies"),
                      })
                  );
        }
    }, [getAnomaliesRequestStatus, anomaliesRequestErrors]);

    return (
        <PageV1>
            <AnomaliesPageHeader selectedIndex={1} />

            <PageContentsGridV1 fullHeight>
                <Grid item xs={12}>
                    <PageContentsCardV1 disablePadding fullHeight>
                        <MetricsReportList
                            metricsReport={uiAnomalies}
                            searchParams={searchParams}
                        />
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
