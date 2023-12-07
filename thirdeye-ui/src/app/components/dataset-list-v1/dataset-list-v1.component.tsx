/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Button, Grid, Link } from "@material-ui/core";
import React, { FunctionComponent, ReactElement, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { PageContentsCardV1 } from "../../platform/components";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import {
    getDatasetsUpdatePath,
    getDatasetsViewPath,
} from "../../utils/routes/routes.util";
import { ActiveIndicator } from "../active-indicator/active-indicator.component";
import { DatasetListV1Props, TEST_IDS } from "./dataset-list-v1.interfaces";
import { StyledDataGrid } from "../data-grid/styled-data-grid.component";
import {
    GridColumns,
    GridRenderCellParams,
    GridSelectionModel,
} from "@mui/x-data-grid";

export const DatasetListV1: FunctionComponent<DatasetListV1Props> = ({
    datasets,
    onDelete,
}) => {
    const { t } = useTranslation();
    const [selectedDatasetIds, setSelectedDatasetIds] =
        useState<GridSelectionModel>();
    const navigate = useNavigate();

    const handleDatasetDelete = (): void => {
        if (!selectedDatasetIds || selectedDatasetIds.length === 0) {
            return;
        }

        const selectedDatasets = datasets?.filter((dataset) => {
            return selectedDatasetIds.includes(dataset.id);
        });

        onDelete && selectedDatasets && onDelete(selectedDatasets);
    };

    const handleDatasetEdit = (): void => {
        if (!selectedDatasetIds) {
            return;
        }
        const selectedDatasetId = selectedDatasetIds[0];

        navigate(getDatasetsUpdatePath(selectedDatasetId as number));
    };

    const isActionButtonDisable = !(
        selectedDatasetIds && selectedDatasetIds.length === 1
    );

    const handleDatasetViewDetailsById = (id: number): void => {
        navigate(getDatasetsViewPath(id));
    };

    const renderLink = (params: GridRenderCellParams): ReactElement => {
        return (
            <Link onClick={() => handleDatasetViewDetailsById(params.row.id)}>
                {params.row.name}
            </Link>
        );
    };

    const renderMetricStatus = (params: GridRenderCellParams): ReactElement => {
        // Default to true of the active status is missing
        const active =
            params.row.active === undefined ? true : params.row.active;

        return <ActiveIndicator active={active} />;
    };

    const datasetColumns: GridColumns = [
        {
            field: "name",
            headerName: t("label.name"),
            flex: 1,
            sortable: true,
            renderCell: renderLink,
        },
        {
            field: "datasourceName",
            headerName: t("label.datasource"),
            sortable: true,
            flex: 1,
        },
        {
            field: "active",
            sortable: true,
            headerName: t("label.active"),
            flex: 0.5,
            renderCell: renderMetricStatus,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding>
                <StyledDataGrid
                    autoHeight
                    autoPageSize
                    checkboxSelection
                    disableColumnFilter
                    disableColumnSelector
                    disableSelectionOnClick
                    columns={datasetColumns}
                    data-testid={TEST_IDS.TABLE}
                    rows={datasets as UiDataset[]}
                    searchBarProps={{
                        searchKey: "name",
                        placeholder: t("label.search-entity", {
                            entity: t("label.datasets"),
                        }),
                    }}
                    selectionModel={selectedDatasetIds}
                    toolbar={
                        <Grid container alignItems="center" spacing={2}>
                            <Grid item>
                                <Button
                                    data-testId={TEST_IDS.EDIT_BUTTON}
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleDatasetEdit}
                                >
                                    {t("label.edit")}
                                </Button>
                            </Grid>

                            <Grid>
                                <Button
                                    data-testId={TEST_IDS.DELETE_BUTTON}
                                    disabled={
                                        !selectedDatasetIds ||
                                        selectedDatasetIds.length === 0
                                    }
                                    variant="contained"
                                    onClick={handleDatasetDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionModelChange={setSelectedDatasetIds}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
