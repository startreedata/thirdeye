import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { AnomalyListV1 } from "../../components/anomaly-list-v1/anomaly-list-v1.component";
import { AnomalyFilterQueryStringKey } from "../../components/anomaly-quick-filters/anomaly-quick-filter.interface";
import { PageHeader } from "../../components/page-header/page-header.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    HelpLinkIconV1,
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    TooltipV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { getUiAnomalies } from "../../utils/anomalies/anomalies.util";
import { SEARCH_TERM_QUERY_PARAM_KEY } from "../../utils/params/params.util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(null);
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
        const alert = searchParams.get(AnomalyFilterQueryStringKey.ALERT);

        let fetchedUiAnomalies: UiAnomaly[] = [];

        getAnomalies({
            startTime: Number(start),
            endTime: Number(end),
            alertId: parseInt(alert || ""),
        })
            .then((anomalies) => {
                if (anomalies && anomalies.length) {
                    fetchedUiAnomalies = getUiAnomalies(anomalies);
                }
            })
            .finally(() => setUiAnomalies(fetchedUiAnomalies));
    };

    const handleAnomalyDelete = (uiAnomaly: UiAnomaly): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiAnomaly.name,
            }),
            okButtonText: t("label.delete"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleAnomalyDeleteOk(uiAnomaly),
        });
    };

    const handleAnomalyDeleteOk = (uiAnomaly: UiAnomaly): void => {
        deleteAnomaly(uiAnomaly.id).then((anomaly): void => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.anomaly") })
            );

            // Remove deleted anomaly from fetched anomalies
            removeUiAnomaly(anomaly);
        });
    };

    const removeUiAnomaly = (anomaly: Anomaly): void => {
        if (!anomaly) {
            return;
        }

        setUiAnomalies(
            (uiAnomalies) =>
                uiAnomalies &&
                uiAnomalies.filter((uiAnomaly) => uiAnomaly.id !== anomaly.id)
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
            >
                <TooltipV1
                    placement="top"
                    title={
                        t(
                            "label.how-to-perform-root-cause-analysis-doc"
                        ) as string
                    }
                >
                    <span>
                        <HelpLinkIconV1
                            displayInline
                            enablePadding
                            externalLink
                            href="https://dev.startree.ai/docs/thirdeye/how-tos/perform-root-cause-analysis"
                        />
                    </span>
                </TooltipV1>
            </PageHeader>

            <PageContentsGridV1 fullHeight>
                <Grid item xs={12}>
                    {/* Anomaly list */}
                    <PageContentsCardV1 disablePadding fullHeight>
                        <AnomalyListV1
                            anomalies={uiAnomalies}
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
