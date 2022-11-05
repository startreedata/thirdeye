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
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import {
    getDatasetsUpdatePath,
    getDatasetsViewPath,
} from "../../utils/routes/routes.util";
import { ActiveIndicator } from "../active-indicator/active-indicator.component";
import { DatasetListV1Props } from "./dataset-list-v1.interfaces";

export const DatasetListV1: FunctionComponent<DatasetListV1Props> = ({
    datasets,
    onDelete,
}) => {
    const { t } = useTranslation();
    const [selectedDataset, setSelectedDataset] =
        useState<DataGridSelectionModelV1<UiDataset>>();
    const navigate = useNavigate();

    const handleDatasetDelete = (): void => {
        if (!selectedDataset || !selectedDataset.rowKeyValueMap) {
            return;
        }

        onDelete &&
            onDelete(Array.from(selectedDataset.rowKeyValueMap.values()));
    };

    const handleDatasetEdit = (): void => {
        if (!selectedDataset) {
            return;
        }
        const selectedDatasetId = selectedDataset.rowKeyValues[0] as number;

        navigate(getDatasetsUpdatePath(selectedDatasetId));
    };

    const isActionButtonDisable = !(
        selectedDataset && selectedDataset.rowKeyValues.length === 1
    );

    const handleDatasetViewDetailsById = (id: number): void => {
        navigate(getDatasetsViewPath(id));
    };

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: UiDataset
    ): ReactElement => {
        return (
            <Link onClick={() => handleDatasetViewDetailsById(data.id)}>
                {cellValue}
            </Link>
        );
    };

    const renderMetricStatus = (
        _: Record<string, unknown>,
        data: UiDataset
    ): ReactElement => {
        // Default to true of the active status is missing
        const active = data.active === undefined ? true : data.active;

        return <ActiveIndicator active={active} />;
    };

    const datasetColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 0,
            flex: 1,
            sortable: true,
            customCellRenderer: renderLink,
        },
        {
            key: "datasourceName",
            dataKey: "datasourceName",
            header: t("label.datasource"),
            minWidth: 0,
            sortable: true,
            flex: 1,
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
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1<UiDataset>
                    hideBorder
                    columns={datasetColumns}
                    data={datasets as UiDataset[]}
                    rowKey="id"
                    scroll={DataGridScrollV1.Contents}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.datasets"),
                    })}
                    toolbarComponent={
                        <Grid container alignItems="center" spacing={2}>
                            <Grid item>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleDatasetEdit}
                                >
                                    {t("label.edit")}
                                </Button>
                            </Grid>

                            <Grid>
                                <Button
                                    disabled={
                                        !selectedDataset ||
                                        selectedDataset.rowKeyValues.length ===
                                            0
                                    }
                                    variant="contained"
                                    onClick={handleDatasetDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionChange={setSelectedDataset}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
