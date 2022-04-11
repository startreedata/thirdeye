import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { AnomalyListV1 } from "../../components/anomaly-list-v1/anomaly-list-v1.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { PageHeader } from "../../components/page-header/page-header.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { getAnomalyName } from "../../utils/anomalies/anomalies.util";
import { SEARCH_TERM_QUERY_PARAM_KEY } from "../../utils/params/params.util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const {
        getAnomalies,
        status: getAnomaliesRequestStatus,
        errorMessages: anomaliesRequestErrors,
    } = useGetAnomalies();
    const [anomalies, setAnomalies] = useState<Anomaly[] | null>(null);
    const { showDialog } = useDialog();
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

        getAnomalies({ startTime: Number(start), endTime: Number(end) }).then(
            (anomalies) => {
                if (anomalies && anomalies.length) {
                    setAnomalies(anomalies);
                }
            }
        );
    };

    const handleAnomalyDelete = (anomaly: Anomaly): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: getAnomalyName(anomaly),
            }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleAnomalyDeleteOk(anomaly),
        });
    };

    const handleAnomalyDeleteOk = (anomaly: Anomaly): void => {
        deleteAnomaly(anomaly.id).then((anomaly): void => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.anomaly") })
            );

            // Remove deleted anomaly from fetched anomalies
            removeAnomaly(anomaly);
        });
    };

    const removeAnomaly = (anomaly: Anomaly): void => {
        if (!anomaly) {
            return;
        }

        setAnomalies(
            (anomalies) =>
                anomalies &&
                anomalies.filter((currAnomaly) => currAnomaly.id !== anomaly.id)
        );
    };

    const onSearchFilterValueChange = (value: string): void => {
        if (value) {
            searchParams.set(SEARCH_TERM_QUERY_PARAM_KEY, value);
        } else {
            searchParams.delete(SEARCH_TERM_QUERY_PARAM_KEY);
        }
        setSearchParams(searchParams);
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
            <PageHeader
                showCreateButton
                showTimeRange
                title={t("label.anomalies")}
            />

            <PageContentsGridV1 fullHeight>
                <Grid item xs={12}>
                    {/* Anomaly list */}
                    <PageContentsCardV1 disablePadding fullHeight>
                        <AnomalyListV1
                            anomalies={anomalies}
                            searchFilterValue={searchParams.get(
                                SEARCH_TERM_QUERY_PARAM_KEY
                            )}
                            onDelete={handleAnomalyDelete}
                            onSearchFilterValueChange={
                                onSearchFilterValueChange
                            }
                        />
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
