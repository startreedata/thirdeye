import { Grid } from "@material-ui/core";
import { PageContentsGridV1, PageV1 } from "@startree-ui/platform-ui";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyList } from "../../components/anomaly-list/anomaly-list.component";
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
import { getSuccessSnackbarOption } from "../../utils/snackbar/snackbar.util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

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
            enqueueSnackbar(
                t("message.delete-success", { entity: t("label.anomaly") }),
                getSuccessSnackbarOption()
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
            <PageHeader title={t("label.anomalies")} />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {/* Anomaly list */}
                    <AnomalyList
                        anomalies={uiAnomalies}
                        onDelete={handleAnomalyDelete}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
