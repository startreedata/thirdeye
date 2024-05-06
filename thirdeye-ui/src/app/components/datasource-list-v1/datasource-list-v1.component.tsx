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
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import {
    getDatasourcesUpdatePath,
    getDatasourcesViewPath,
} from "../../utils/routes/routes.util";
import {
    DatasourceListV1Props,
    TEST_IDS,
} from "./datasource-list-v1.interfaces";
import { DatasourceVerification } from "./datasource-verification/datasource-verification.component";
import { StyledDataGrid } from "../data-grid/styled-data-grid.component";
import {
    GridColumns,
    GridRenderCellParams,
    GridSelectionModel,
} from "@mui/x-data-grid";

export const DatasourceListV1: FunctionComponent<DatasourceListV1Props> = ({
    datasources,
    onDelete,
}) => {
    const { t } = useTranslation();
    const [selectedDatasourceIds, setSelectedDatasourceIds] =
        useState<GridSelectionModel>();
    const navigate = useNavigate();

    const handleDatasourceDelete = (): void => {
        if (!selectedDatasourceIds) {
            return;
        }

        const selectedSubScriptingGroupId = selectedDatasourceIds[0] as number;
        const uiDatasource = getUiDatasource(selectedSubScriptingGroupId);
        if (!uiDatasource) {
            return;
        }

        onDelete && onDelete(uiDatasource);
    };

    const getUiDatasource = (id: number): UiDatasource | null => {
        if (!datasources) {
            return null;
        }

        return datasources.find((datasource) => datasource.id === id) || null;
    };

    const handleDatasourceEdit = (): void => {
        if (!selectedDatasourceIds) {
            return;
        }
        const selectedSubScriptionGroupId = selectedDatasourceIds[0] as number;

        navigate(getDatasourcesUpdatePath(selectedSubScriptionGroupId));
    };

    const isActionButtonDisable = !(
        selectedDatasourceIds && selectedDatasourceIds.length === 1
    );

    const handleDatasourceViewDetailsById = (id: number): void => {
        navigate(getDatasourcesViewPath(id));
    };

    const renderLink = (params: GridRenderCellParams): ReactElement => {
        return (
            <Link
                onClick={() => handleDatasourceViewDetailsById(params.row.id)}
            >
                {params.row.name}
            </Link>
        );
    };

    const renderStatusCheck = (params: GridRenderCellParams): ReactElement => {
        return <DatasourceVerification datasourceId={params.row.id} />;
    };

    const datasourceColumns: GridColumns = [
        {
            field: "name",
            headerName: t("label.name"),
            flex: 1.5,
            sortable: true,
            renderCell: renderLink,
        },
        {
            field: "type",
            sortable: true,
            headerName: t("label.type"),
            flex: 1,
        },
        {
            field: "status",
            headerName: t("label.health-status"),
            flex: 1,
            sortable: false,
            renderCell: renderStatusCheck,
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
                    columns={datasourceColumns}
                    data-testid={TEST_IDS.TABLE}
                    rows={datasources as UiDatasource[]}
                    searchBarProps={{
                        searchKey: "name",
                        placeholder: t("label.search-entity", {
                            entity: t("label.datasources"),
                        }),
                    }}
                    selectionModel={selectedDatasourceIds}
                    toolbar={
                        <Grid container alignItems="center" spacing={2}>
                            <Grid item>
                                <Button
                                    data-testId={TEST_IDS.EDIT_BUTTON}
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleDatasourceEdit}
                                >
                                    {t("label.edit")}
                                </Button>
                            </Grid>

                            <Grid>
                                <Button
                                    data-testId={TEST_IDS.DELETE_BUTTON}
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleDatasourceDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionModelChange={setSelectedDatasourceIds}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
