import { Grid } from "@material-ui/core";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyListV1 } from "../../components/anomaly-list-v1/anomaly-list-v1.component";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { PageHeader } from "../../components/page-header/page-header.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteAnomaly,
    getAnomaliesByTime,
} from "../../rest/anomalies/anomalies.rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { getUiAnomalies } from "../../utils/anomalies/anomalies.util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch anomalies
        fetchAnomaliesByTime();
    }, [timeRangeDuration]);

    const fetchAnomaliesByTime = (): void => {
        setUiAnomalies(null);

        let fetchedUiAnomalies: UiAnomaly[] = [];
        getAnomaliesByTime(
            timeRangeDuration.startTime,
            timeRangeDuration.endTime
        )
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

    return (
        <PageV1>
            <PageHeader showTimeRange title={t("label.anomalies")} />

            <PageContentsGridV1 fullHeight>
                <Grid item xs={12}>
                    <PageContentsCardV1 disablePadding fullHeight>
                        {/* Anomaly list */}
                        <AnomalyListV1
                            anomalies={uiAnomalies}
                            onDelete={handleAnomalyDelete}
                        />
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
