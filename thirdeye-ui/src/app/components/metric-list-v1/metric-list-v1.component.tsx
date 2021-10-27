import { Button, Grid, Link, useTheme } from "@material-ui/core";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, ReactElement, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import {
    getMetricsUpdatePath,
    getMetricsViewPath,
} from "../../utils/routes/routes.util";
import { MetricListV1Props } from "./metric-list-v1.interfaces";

export const MetricListV1: FunctionComponent<MetricListV1Props> = (
    props: MetricListV1Props
) => {
    const { t } = useTranslation();
    const [selectedMetric, setSelectedMetric] =
        useState<DataGridSelectionModelV1>();
    const history = useHistory();
    const theme = useTheme();

    const handleMetricDelete = (): void => {
        if (!selectedMetric) {
            return;
        }

        const selectedSubScriptionGroupId = selectedMetric
            .rowKeyValues[0] as number;
        const uiMetric = getUiMetric(selectedSubScriptionGroupId);
        if (!uiMetric) {
            return;
        }

        props.onDelete && props.onDelete(uiMetric);
    };

    const getUiMetric = (id: number): UiMetric | null => {
        if (!props.metrics) {
            return null;
        }

        return props.metrics.find((metric) => metric.id === id) || null;
    };

    const handleMetricEdit = (): void => {
        if (!selectedMetric) {
            return;
        }
        const selectedSubScriptionGroupId = selectedMetric
            .rowKeyValues[0] as number;

        history.push(getMetricsUpdatePath(selectedSubScriptionGroupId));
    };

    const isActionButtonDisable = !(
        selectedMetric && selectedMetric.rowKeyValues.length === 1
    );

    const handleMetricViewDetailsById = (id: number): void => {
        history.push(getMetricsViewPath(id));
    };

    const renderLink = (renderProps: Record<string, unknown>): ReactElement => {
        return (
            <Link
                onClick={() =>
                    handleMetricViewDetailsById(
                        (renderProps.rowData as UiMetric).id
                    )
                }
            >
                {(renderProps.rowData as UiMetric).name}
            </Link>
        );
    };

    const renderMetricStatus = (
        renderProps: Record<string, unknown>
    ): ReactElement => {
        const active = (renderProps.rowData as UiMetric).active;

        return (
            <>
                {/* Active */}
                {active && (
                    <CheckIcon
                        fontSize="small"
                        htmlColor={theme.palette.success.main}
                    />
                )}

                {/* Inactive */}
                {!active && (
                    <CloseIcon
                        fontSize="small"
                        htmlColor={theme.palette.error.main}
                    />
                )}
            </>
        );
    };

    const metricColumns = [
        {
            key: "name",
            dataKey: "name",
            title: t("label.name"),
            minWidth: 0,
            flexGrow: 1.5,
            sortable: true,
            cellRenderer: renderLink,
        },
        {
            key: "datasetName",
            dataKey: "datasetName",
            sortable: true,
            title: t("label.dataset"),
            minWidth: 0,
            flexGrow: 1.5,
        },
        {
            key: "active",
            dataKey: "active",
            sortable: true,
            title: t("label.active"),
            minWidth: 0,
            flexGrow: 0.5,
            cellRenderer: renderMetricStatus,
        },
        {
            key: "aggregationColumn",
            dataKey: "aggregationColumn",
            sortable: true,
            title: t("label.aggregation-column"),
            minWidth: 0,
            flexGrow: 1,
        },
        {
            key: "aggregationFunction",
            dataKey: "aggregationFunction",
            sortable: true,
            title: t("label.aggregation-function"),
            minWidth: 0,
            flexGrow: 1,
        },
        {
            key: "viewCount",
            dataKey: "viewCount",
            sortable: true,
            title: t("label.views"),
            minWidth: 0,
            flexGrow: 1,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1
                    hideBorder
                    columns={metricColumns}
                    data={props.metrics as unknown as Record<string, unknown>[]}
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
                                    disabled={isActionButtonDisable}
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
