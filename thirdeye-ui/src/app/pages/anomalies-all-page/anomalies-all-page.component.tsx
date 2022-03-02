import { Grid } from "@material-ui/core";
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
import {
    deleteAnomaly,
    getAnomaliesByTime,
} from "../../rest/anomalies/anomalies.rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { getUiAnomalies } from "../../utils/anomalies/anomalies.util";
import { SEARCH_TERM_QUERY_PARAM_KEY } from "../../utils/params/params.util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(null);
    const { showDialog } = useDialog();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch anomalies
        fetchAnomaliesByTime();
    }, [searchParams]);

    const fetchAnomaliesByTime = (): void => {
        setUiAnomalies(null);

        const start = searchParams.get(TimeRangeQueryStringKey.START_TIME);
        const end = searchParams.get(TimeRangeQueryStringKey.END_TIME);

        let fetchedUiAnomalies: UiAnomaly[] = [];
        getAnomaliesByTime(Number(start), Number(end))
            .then((anomalies) => {
                fetchedUiAnomalies = getUiAnomalies(anomalies);
            })
            .finally(() => setUiAnomalies(fetchedUiAnomalies));
    };

    const handleAnomalyDelete = (uiAnomaly: UiAnomaly): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiAnomaly.name }),
            okButtonLabel: t("label.delete"),
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

    return (
        <PageV1>
            <PageHeader
                showCreateButton
                showTimeRange
                title={t("label.anomalies")}
            />

            <PageContentsGridV1 fullHeight>
                <Grid item xs={12}>
                    <PageContentsCardV1 disablePadding fullHeight>
                        {/* Anomaly list */}
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
