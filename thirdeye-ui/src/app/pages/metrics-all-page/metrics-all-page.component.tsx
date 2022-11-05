import { Box, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { MetricListV1 } from "../../components/metric-list-v1/metric-list-v1.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { deleteMetric, getAllMetrics } from "../../rest/metrics/metrics.rest";
import {
    makeDeleteRequest,
    promptDeleteConfirmation,
} from "../../utils/bulk-delete/bulk-delete.util";
import { getUiMetrics } from "../../utils/metrics/metrics.util";

export const MetricsAllPage: FunctionComponent = () => {
    const [uiMetrics, setUiMetrics] = useState<UiMetric[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isError, setIsError] = useState(false);
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch metrics
        fetchAllMetrics();
    }, []);

    const fetchAllMetrics = (): void => {
        setIsError(false);
        setIsLoading(true);
        setUiMetrics([]);

        getAllMetrics()
            .then((metrics) => {
                setUiMetrics(getUiMetrics(metrics));
            })
            .catch(() => {
                setIsError(true);
            })
            .finally(() => {
                setIsLoading(false);
            });
    };

    const handleMetricDelete = (uiMetricsToDelete: UiMetric[]): void => {
        promptDeleteConfirmation(
            uiMetricsToDelete,
            () => {
                uiMetrics &&
                    makeDeleteRequest(
                        uiMetricsToDelete,
                        deleteMetric,
                        t,
                        notify,
                        t("label.metric"),
                        t("label.metrics")
                    ).then((deleted) => {
                        setUiMetrics(() => {
                            return [...uiMetrics].filter((candidate) => {
                                return (
                                    deleted.findIndex(
                                        (d) => d.id === candidate.id
                                    ) === -1
                                );
                            });
                        });
                    });
            },
            t,
            showDialog,
            t("label.metrics")
        );
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={2} />
            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch
                    isError={isError}
                    isLoading={isLoading}
                >
                    <EmptyStateSwitch
                        emptyState={
                            <Grid item xs={12}>
                                <PageContentsCardV1>
                                    <Box padding={20}>
                                        <NoDataIndicator>
                                            <Box textAlign="center">
                                                {t(
                                                    "message.no-entity-created",
                                                    {
                                                        entity: t(
                                                            "label.metrics"
                                                        ),
                                                    }
                                                )}
                                            </Box>
                                        </NoDataIndicator>
                                    </Box>
                                </PageContentsCardV1>
                            </Grid>
                        }
                        isEmpty={uiMetrics.length === 0}
                    >
                        <MetricListV1
                            metrics={uiMetrics}
                            onDelete={handleMetricDelete}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
