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
import { MetricCardData } from "../../components/entity-cards/metric-card/metric-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { Metric } from "../../rest/dto/metric.interfaces";
import { deleteMetric, getMetric } from "../../rest/metrics/metrics.rest";
import { getMetricCardData } from "../../utils/metrics/metrics.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getMetricsAllPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { MetricsDetailPageParams } from "./metrics-detail-page.interfaces";

export const MetricsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [metricCardData, setMetricCardData] = useState<MetricCardData>();
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

    const onDeleteMetric = (metricCardData: MetricCardData): void => {
        if (!metricCardData) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: metricCardData.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteMetricConfirmation(metricCardData);
            },
        });
    };

    const onDeleteMetricConfirmation = (
        metricCardData: MetricCardData
    ): void => {
        if (!metricCardData) {
            return;
        }

        deleteMetric(metricCardData.id)
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
                setMetricCardData(getMetricCardData(metric));
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
            title={metricCardData ? metricCardData.name : ""}
        >
            {metricCardData && (
                <Grid container>
                    {/* Metric */}
                    <Grid item sm={12}>
                        <MetricCard
                            metricCardData={metricCardData}
                            onDelete={onDeleteMetric}
                        />
                    </Grid>
                </Grid>
            )}

            {/* No data available message */}
            {!metricCardData && <NoDataIndicator />}
        </PageContents>
    );
};
