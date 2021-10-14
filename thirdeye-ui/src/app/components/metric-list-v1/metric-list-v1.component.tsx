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
    const [
        selectedMetric,
        setSelectedMetric,
    ] = useState<DataGridSelectionModelV1>();
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

    const renderLink = ({ rowData }: { rowData: UiMetric }): ReactElement => {
        return (
            <Link onClick={() => handleMetricViewDetailsById(rowData.id)}>
                {rowData.name}
            </Link>
        );
    };

    const renderMetricStatus = ({
        rowData,
    }: {
        rowData: UiMetric;
    }): ReactElement => {
        const { active } = rowData;

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
            width: 0,
            flexGrow: 1.5,
            sortable: true,
            cellRenderer: renderLink,
        },
        {
            key: "datasetName",
            dataKey: "datasetName",
            sortable: true,
            title: t("label.dataset"),
            width: 0,
            flexGrow: 1.5,
        },
        {
            key: "active",
            dataKey: "active",
            sortable: true,
            title: t("label.active"),
            width: 0,
            flexGrow: 0.5,
            cellRenderer: renderMetricStatus,
        },
        {
            key: "aggregationColumn",
            dataKey: "aggregationColumn",
            sortable: true,
            title: t("label.aggregation-column"),
            width: 0,
            flexGrow: 1,
        },
        {
            key: "aggregationFunction",
            dataKey: "aggregationFunction",
            sortable: true,
            title: t("label.aggregation-function"),
            width: 0,
            flexGrow: 1,
        },
        {
            key: "viewCount",
            dataKey: "viewCount",
            sortable: true,
            title: t("label.views"),
            width: 0,
            flexGrow: 1,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1
                    disableBorder
                    columns={metricColumns}
                    data={props.metrics}
                    rowKey="id"
                    scroll={DataGridScrollV1.Contents}
                    selection={selectedMetric}
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
