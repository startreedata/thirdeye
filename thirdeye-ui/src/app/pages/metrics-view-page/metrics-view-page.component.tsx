import { Grid } from "@material-ui/core";
import { PageContentsGridV1, PageV1 } from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { MetricCard } from "../../components/entity-cards/metric-card/metric-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { deleteMetric, getMetric } from "../../rest/metrics/metrics.rest";
import { getUiMetric } from "../../utils/metrics/metrics.util";
import { isValidNumberId } from "../../utils/params/params.util";
import {
    getMetricsAllPath,
    getMetricsUpdatePath,
} from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { MetricsViewPageParams } from "./metrics-view-page.interfaces";

export const MetricsViewPage: FunctionComponent = () => {
    const [uiMetric, setUiMetric] = useState<UiMetric | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<MetricsViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch metric
        fetchMetric();
    }, [timeRangeDuration]);

    const fetchMetric = (): void => {
        setUiMetric(null);
        let fetchedUiMetric = {} as UiMetric;

        if (!isValidNumberId(params.id)) {
            // Invalid id
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.metric"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );

            setUiMetric(fetchedUiMetric);

            return;
        }

        getMetric(toNumber(params.id))
            .then((metric) => {
                fetchedUiMetric = getUiMetric(metric);
            })
            .finally(() => setUiMetric(fetchedUiMetric));
    };

    const handleMetricDelete = (uiMetric: UiMetric): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiMetric.name }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleMetricDeleteOk(uiMetric),
        });
    };

    const handleMetricDeleteOk = (uiMetric: UiMetric): void => {
        deleteMetric(uiMetric.id).then(() => {
            enqueueSnackbar(
                t("message.delete-success", { entity: t("label.metric") }),
                getSuccessSnackbarOption()
            );

            // Redirect to metrics all path
            history.push(getMetricsAllPath());
        });
    };

    const handleMetricEdit = (id: number): void => {
        history.push(getMetricsUpdatePath(id));
    };

    return (
        <PageV1>
            <PageHeader title={uiMetric ? uiMetric.name : ""} />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {/* Metric */}
                    <MetricCard
                        metric={uiMetric}
                        onDelete={handleMetricDelete}
                        onEdit={handleMetricEdit}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
