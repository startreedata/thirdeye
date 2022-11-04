import { Button, Grid, Link } from "@material-ui/core";
import React, { FunctionComponent, ReactElement, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "../../platform/components";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import {
    getMetricsUpdatePath,
    getMetricsViewPath,
} from "../../utils/routes/routes.util";
import { ActiveIndicator } from "../active-indicator/active-indicator.component";
import { MetricListV1Props } from "./metric-list-v1.interfaces";

export const MetricListV1: FunctionComponent<MetricListV1Props> = ({
    metrics,
    onDelete,
}) => {
    const { t } = useTranslation();
    const [selectedMetric, setSelectedMetric] =
        useState<DataGridSelectionModelV1<UiMetric>>();
    const navigate = useNavigate();

    const handleMetricDelete = (): void => {
        if (!selectedMetric || !selectedMetric.rowKeyValueMap) {
            return;
        }

        onDelete &&
            onDelete(Array.from(selectedMetric.rowKeyValueMap.values()));
    };

    const handleMetricEdit = (): void => {
        if (!selectedMetric) {
            return;
        }
        const selectedMetricId = selectedMetric.rowKeyValues[0] as number;

        navigate(getMetricsUpdatePath(selectedMetricId));
    };

    const isActionButtonDisable = !(
        selectedMetric && selectedMetric.rowKeyValues.length === 1
    );

    const handleMetricViewDetailsById = (id: number): void => {
        navigate(getMetricsViewPath(id));
    };

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: UiMetric
    ): ReactElement => {
        return (
            <Link onClick={() => handleMetricViewDetailsById(data.id)}>
                {cellValue}
            </Link>
        );
    };

    const renderMetricStatus = (
        _: Record<string, unknown>,
        data: UiMetric
    ): ReactElement => {
        const active = data.active;

        return <ActiveIndicator active={active} />;
    };

    const metricColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 0,
            flex: 1.5,
            sortable: true,
            customCellRenderer: renderLink,
        },
        {
            key: "datasetName",
            dataKey: "datasetName",
            sortable: true,
            header: t("label.dataset"),
            minWidth: 0,
            flex: 1.5,
        },
        {
            key: "active",
            dataKey: "active",
            sortable: true,
            header: t("label.active"),
            minWidth: 0,
            flex: 0.5,
            customCellRenderer: renderMetricStatus,
        },
        {
            key: "aggregationColumn",
            dataKey: "aggregationColumn",
            sortable: true,
            header: t("label.aggregation-column"),
            minWidth: 0,
            flex: 1,
        },
        {
            key: "aggregationFunction",
            dataKey: "aggregationFunction",
            sortable: true,
            header: t("label.aggregation-function"),
            minWidth: 0,
            flex: 1,
        },
        {
            key: "viewCount",
            dataKey: "viewCount",
            sortable: true,
            header: t("label.views"),
            minWidth: 0,
            flex: 1,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1<UiMetric>
                    hideBorder
                    columns={metricColumns}
                    data={metrics as UiMetric[]}
                    rowKey="id"
                    scroll={DataGridScrollV1.Contents}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.metrics"),
                    })}
                    toolbarComponent={
                        <Grid container alignItems="center" spacing={2}>
                            <Grid item>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleMetricEdit}
                                >
                                    {t("label.edit")}
                                </Button>
                            </Grid>

                            <Grid>
                                <Button
                                    disabled={
                                        !selectedMetric ||
                                        selectedMetric.rowKeyValues.length === 0
                                    }
                                    variant="contained"
                                    onClick={handleMetricDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionChange={setSelectedMetric}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
