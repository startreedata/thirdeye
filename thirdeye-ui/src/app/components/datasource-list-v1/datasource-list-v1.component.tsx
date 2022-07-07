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
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import {
    getDatasourcesUpdatePath,
    getDatasourcesViewPath,
} from "../../utils/routes/routes.util";
import { DatasourceListV1Props } from "./datasource-list-v1.interfaces";

export const DatasourceListV1: FunctionComponent<DatasourceListV1Props> = (
    props: DatasourceListV1Props
) => {
    const { t } = useTranslation();
    const [selectedDatasource, setSelectedDatasource] =
        useState<DataGridSelectionModelV1<UiDatasource>>();
    const navigate = useNavigate();

    const handleDatasourceDelete = (): void => {
        if (!selectedDatasource) {
            return;
        }

        const selectedSubScriptingGroupId = selectedDatasource
            .rowKeyValues[0] as number;
        const uiDatasource = getUiDatasource(selectedSubScriptingGroupId);
        if (!uiDatasource) {
            return;
        }

        props.onDelete && props.onDelete(uiDatasource);
    };

    const getUiDatasource = (id: number): UiDatasource | null => {
        if (!props.datasources) {
            return null;
        }

        return (
            props.datasources.find((datasource) => datasource.id === id) || null
        );
    };

    const handleDatasourceEdit = (): void => {
        if (!selectedDatasource) {
            return;
        }
        const selectedSubScriptionGroupId = selectedDatasource
            .rowKeyValues[0] as number;

        navigate(getDatasourcesUpdatePath(selectedSubScriptionGroupId));
    };

    const isActionButtonDisable = !(
        selectedDatasource && selectedDatasource.rowKeyValues.length === 1
    );

    const handleDatasourceViewDetailsById = (id: number): void => {
        navigate(getDatasourcesViewPath(id));
    };

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: UiDatasource
    ): ReactElement => {
        return (
            <Link onClick={() => handleDatasourceViewDetailsById(data.id)}>
                {cellValue}
            </Link>
        );
    };

    const datasourceColumns = [
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
            key: "type",
            dataKey: "type",
            sortable: true,
            header: t("label.type"),
            minWidth: 0,
            flex: 1,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1<UiDatasource>
                    hideBorder
                    columns={datasourceColumns}
                    data={props.datasources as UiDatasource[]}
                    rowKey="id"
                    scroll={DataGridScrollV1.Contents}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.datasources"),
                    })}
                    toolbarComponent={
                        <Grid container alignItems="center" spacing={2}>
                            <Grid item>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleDatasourceEdit}
                                >
                                    {t("label.edit")}
                                </Button>
                            </Grid>

                            <Grid>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleDatasourceDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionChange={setSelectedDatasource}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
