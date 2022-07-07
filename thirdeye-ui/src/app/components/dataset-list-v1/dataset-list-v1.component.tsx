/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Button, Grid, Link } from "@material-ui/core";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, ReactElement, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import {
    getDatasetsUpdatePath,
    getDatasetsViewPath,
} from "../../utils/routes/routes.util";
import { DatasetListV1Props } from "./dataset-list-v1.interfaces";

export const DatasetListV1: FunctionComponent<DatasetListV1Props> = (
    props: DatasetListV1Props
) => {
    const { t } = useTranslation();
    const [selectedDataset, setSelectedDataset] =
        useState<DataGridSelectionModelV1<UiDataset>>();
    const navigate = useNavigate();

    const handleDatasetDelete = (): void => {
        if (!selectedDataset) {
            return;
        }

        const selectedSubScriptingGroupId = selectedDataset
            .rowKeyValues[0] as number;
        const uiDataset = getUiDataset(selectedSubScriptingGroupId);
        if (!uiDataset) {
            return;
        }

        props.onDelete && props.onDelete(uiDataset);
    };

    const getUiDataset = (id: number): UiDataset | null => {
        if (!props.datasets) {
            return null;
        }

        return props.datasets.find((dataset) => dataset.id === id) || null;
    };

    const handleDatasetEdit = (): void => {
        if (!selectedDataset) {
            return;
        }
        const selectedSubScriptionGroupId = selectedDataset
            .rowKeyValues[0] as number;

        navigate(getDatasetsUpdatePath(selectedSubScriptionGroupId));
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
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1<UiDataset>
                    hideBorder
                    columns={datasetColumns}
                    data={props.datasets as UiDataset[]}
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
                                    disabled={isActionButtonDisable}
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
