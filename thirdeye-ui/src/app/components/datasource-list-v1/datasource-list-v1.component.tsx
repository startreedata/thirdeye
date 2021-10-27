import { Button, Grid, Link } from "@material-ui/core";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, ReactElement, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
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
        useState<DataGridSelectionModelV1>();
    const history = useHistory();

    const handleDatasourceDelete = (): void => {
        if (!selectedDatasource) {
            return;
        }

        const selectedSubScriptionGroupId = selectedDatasource
            .rowKeyValues[0] as number;
        const uiDatasource = getUiDatasource(selectedSubScriptionGroupId);
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

        history.push(getDatasourcesUpdatePath(selectedSubScriptionGroupId));
    };

    const isActionButtonDisable = !(
        selectedDatasource && selectedDatasource.rowKeyValues.length === 1
    );

    const handleDatasourceViewDetailsById = (id: number): void => {
        history.push(getDatasourcesViewPath(id));
    };

    const renderLink = (renderProps: Record<string, unknown>): ReactElement => {
        return (
            <Link
                onClick={() =>
                    handleDatasourceViewDetailsById(
                        (renderProps.rowData as UiDatasource).id
                    )
                }
            >
                {(renderProps.rowData as UiDatasource).name}
            </Link>
        );
    };

    const datasourceColumns = [
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
            key: "type",
            dataKey: "type",
            sortable: true,
            title: t("label.type"),
            minWidth: 0,
            flexGrow: 1,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1
                    hideBorder
                    columns={datasourceColumns}
                    data={
                        props.datasources as unknown as Record<
                            string,
                            unknown
                        >[]
                    }
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
