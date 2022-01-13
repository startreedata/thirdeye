import { Grid } from "@material-ui/core";
import {
    AppLoadingIndicatorV1,
    PageContentsGridV1,
    PageV1,
} from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { MetricsWizard } from "../../components/metrics-wizard/metrics-wizard.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { getAllDatasets } from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { LogicalMetric, Metric } from "../../rest/dto/metric.interfaces";
import { getMetric, updateMetric } from "../../rest/metrics/metrics.rest";
import { isValidNumberId } from "../../utils/params/params.util";
import { getMetricsViewPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { MetricsUpdatePageParams } from "./metrics-update-page.interfaces";

export const MetricsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [metric, setMetric] = useState<Metric>();
    const [datasets, setDatasets] = useState<Dataset[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<MetricsUpdatePageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Fetched metric changed, set breadcrumbs
        setPageBreadcrumbs([
            {
                text: metric ? metric.name : "",
                onClick: (): void => {
                    if (metric) {
                        history.push(getMetricsViewPath(metric.id));
                    }
                },
            },
        ]);
    }, [metric]);

    useEffect(() => {
        fetchMetric();
    }, []);

    const onMetricWizardFinish = (newMetric: LogicalMetric): void => {
        if (!metric) {
            return;
        }

        updateMetric(newMetric).then((newMetric: LogicalMetric): void => {
            enqueueSnackbar(
                t("message.update-success", {
                    entity: t("label.metric"),
                }),
                getSuccessSnackbarOption()
            );

            // Redirect to metric detail path
            history.push(getMetricsViewPath(newMetric.id || 0));
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

        Promise.allSettled([getMetric(toNumber(params.id)), getAllDatasets()])
            .then(([metricResponse, datasetsResponse]): void => {
                // Determine if any of the calls failed
                if (
                    metricResponse.status === "rejected" ||
                    datasetsResponse.status === "rejected"
                ) {
                    enqueueSnackbar(
                        t("message.fetch-error"),
                        getErrorSnackbarOption()
                    );
                }

                // Attempt to gather data
                if (metricResponse.status === "fulfilled") {
                    setMetric(metricResponse.value);
                }
                if (datasetsResponse.status === "fulfilled") {
                    setDatasets(datasetsResponse.value);
                }
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageV1>
            <PageHeader
                title={t("label.update-entity", {
                    entity: t("label.metric"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {metric && (
                        <MetricsWizard
                            datasets={datasets}
                            metric={metric}
                            onFinish={onMetricWizardFinish}
                        />
                    )}
                    {/* No data available message */}
                    {!metric && <NoDataIndicator />}
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
