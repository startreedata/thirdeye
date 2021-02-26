import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyList } from "../../components/anomaly-list/anomaly-list.component";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteAnomaly,
    getAnomaliesByTime,
} from "../../rest/anomalies/anomalies.rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { getUiAnomalies } from "../../utils/anomalies/anomalies.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range changed, fetch anomalies
        fetchAnomaliesByTime();
    }, [timeRangeDuration]);

    const onDeleteAnomaly = (uiAnomaly: UiAnomaly): void => {
        if (!uiAnomaly) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: uiAnomaly.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteAnomalyConfirmation(uiAnomaly);
            },
        });
    };

    const onDeleteAnomalyConfirmation = (uiAnomaly: UiAnomaly): void => {
        if (!uiAnomaly) {
            return;
        }

        deleteAnomaly(uiAnomaly.id)
            .then((anomaly: Anomaly): void => {
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.anomaly") }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted anomaly from fetched anomalies
                removeUiAnomaly(anomaly);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.anomaly") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchAnomaliesByTime = (): void => {
        getAnomaliesByTime(
            timeRangeDuration.startTime,
            timeRangeDuration.endTime
        )
            .then((anomalies: Anomaly[]): void => {
                setUiAnomalies(getUiAnomalies(anomalies));
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    const removeUiAnomaly = (anomaly: Anomaly): void => {
        if (!anomaly) {
            return;
        }

        setUiAnomalies((uiAnomalies) =>
            uiAnomalies.filter((uiAnomaly: UiAnomaly): boolean => {
                return uiAnomaly.id !== anomaly.id;
            })
        );
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents centered hideAppBreadcrumbs title={t("label.anomalies")}>
            <AnomalyList uiAnomalies={uiAnomalies} onDelete={onDeleteAnomaly} />
        </PageContents>
    );
};
