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
import { Outlet, useSearchParams } from "react-router-dom";
import { AnomaliesPageHeader } from "../../components/anomalies-page-header/anomalies-page-header.component";
import { AnomalyFilterQueryStringKey } from "../../components/anomaly-quick-filters/anomaly-quick-filter.interface";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    SkeletonV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { GetAnomaliesProps } from "../../rest/anomalies/anomaly.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [searchParams] = useSearchParams();
    // Use state so we can remove anomalies locally without having to fetch again
    const [anomalies, setAnomalies] = useState<Anomaly[] | null>(null);
    const {
        getAnomalies,
        status: getAnomaliesRequestStatus,
        errorMessages: anomaliesRequestErrors,
    } = useGetAnomalies();
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch anomalies
        fetchAnomaliesByTime();
    }, [searchParams]);

    useEffect(() => {
        if (
            getAnomaliesRequestStatus === ActionStatus.Done &&
            anomalies &&
            anomalies.length === 0
        ) {
            notify(
                NotificationTypeV1.Info,
                t("message.no-data-for-entity-for-date-range", {
                    entity: t("label.anomalies"),
                })
            );
        }
    }, [getAnomaliesRequestStatus, anomalies]);

    const fetchAnomaliesByTime = (): void => {
        setAnomalies(null);

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

        getAnomalies(params).then((anomalies) => {
            if (anomalies && anomalies.length > 0) {
                setAnomalies(anomalies);
            }
        });
    };

    const handleAnomalyDelete = (uiAnomalies: UiAnomaly[]): void => {
        let promptMsg = t("message.delete-confirmation", {
            name: uiAnomalies[0].name,
        });

        if (uiAnomalies.length > 1) {
            promptMsg = t("message.delete-confirmation", {
                name: `${uiAnomalies.length} ${t("label.anomalies")}`,
            });
        }

        showDialog({
            type: DialogType.ALERT,
            contents: promptMsg,
            okButtonText: t("label.confirm"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleAnomalyDeleteOk(uiAnomalies),
        });
    };

    const handleAnomalyDeleteOk = (uiAnomalies: UiAnomaly[]): void => {
        Promise.allSettled(
            uiAnomalies.map((uiAnomaly) => deleteAnomaly(uiAnomaly.id))
        ).then((completedRequests) => {
            let numSuccessful = 0;
            let errored = 0;

            completedRequests.forEach((settled) => {
                if (settled.status === "fulfilled") {
                    numSuccessful = numSuccessful + 1;
                    removeUiAnomaly(settled.value);
                } else {
                    errored = errored + 1;
                }
            });

            if (uiAnomalies.length === 1 && numSuccessful === 1) {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", { entity: t("label.anomaly") })
                );
            } else {
                if (numSuccessful > 0) {
                    notify(
                        NotificationTypeV1.Success,
                        t("message.num-delete-success", {
                            entity: t("label.anomalies"),
                            num: numSuccessful,
                        })
                    );
                }
                if (errored > 0) {
                    notify(
                        NotificationTypeV1.Error,
                        t("message.num-delete-error", {
                            entity: t("label.anomalies"),
                            num: errored,
                        })
                    );
                }
            }
        });
    };

    const removeUiAnomaly = (anomalyToRemove: Anomaly): void => {
        if (!anomalyToRemove) {
            return;
        }

        setAnomalies(
            (currentAnomalies) =>
                currentAnomalies &&
                currentAnomalies.filter(
                    (candidate) => candidate.id !== anomalyToRemove.id
                )
        );
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
            <AnomaliesPageHeader />

            <PageContentsGridV1 fullHeight>
                {getAnomaliesRequestStatus === ActionStatus.Working && (
                    <Grid xs={12}>
                        <PageContentsCardV1>
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                        </PageContentsCardV1>
                    </Grid>
                )}

                {getAnomaliesRequestStatus === ActionStatus.Done && (
                    <Outlet
                        context={{
                            anomalies,
                            handleAnomalyDelete,
                        }}
                    />
                )}
            </PageContentsGridV1>
        </PageV1>
    );
};
