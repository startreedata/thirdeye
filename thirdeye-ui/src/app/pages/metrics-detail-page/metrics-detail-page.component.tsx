import { Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { MetricCard } from "../../components/entity-cards/metric-card/metric-card.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { Metric } from "../../rest/dto/metric.interfaces";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { deleteMetric, getMetric } from "../../rest/metrics/metrics.rest";
import { getUiMetric } from "../../utils/metrics/metrics.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getMetricsAllPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { MetricsDetailPageParams } from "./metrics-detail-page.interfaces";

export const MetricsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [uiMetric, setUiMetric] = useState<UiMetric>();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<MetricsDetailPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchMetric();
    }, []);

    const onDeleteMetric = (uiMetric: UiMetric): void => {
        if (!uiMetric) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: uiMetric.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteMetricConfirmation(uiMetric);
            },
        });
    };

    const onDeleteMetricConfirmation = (uiMetric: UiMetric): void => {
        if (!uiMetric) {
            return;
        }

        deleteMetric(uiMetric.id)
            .then((): void => {
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.metric") }),
                    getSuccessSnackbarOption()
                );

                // Redirect to metrics all path
                history.push(getMetricsAllPath());
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.metric") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchMetric = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.metric"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );
            setLoading(false);

            return;
        }

        getMetric(toNumber(params.id))
            .then((metric: Metric): void => {
                setUiMetric(getUiMetric(metric));
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

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents
            centered
            hideTimeRange
            title={uiMetric ? uiMetric.name : ""}
        >
            {uiMetric && (
                <Grid container>
                    {/* Metric */}
                    <Grid item sm={12}>
                        <MetricCard
                            uiMetric={uiMetric}
                            onDelete={onDeleteMetric}
                        />
                    </Grid>
                </Grid>
            )}

            {/* No data available message */}
            {!uiMetric && <NoDataIndicator />}
        </PageContents>
    );
};
