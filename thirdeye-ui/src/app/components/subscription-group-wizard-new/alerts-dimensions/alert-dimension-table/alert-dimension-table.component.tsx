/*
 * Copyright 2022 StarTree Inc
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

import { Box, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridSelectionModelV1,
    DataGridV1,
} from "../../../../platform/components";
import { DataGridV1Props } from "../../../../platform/components/data-grid-v1/data-grid-v1/data-grid-v1.interfaces";
import { DimensionRow } from "../add-dimension-dialog/add-dimension-dialog.interface";
import {
    areAllRowsSelected,
    getDataRowsForAlert,
    getSelectedRows,
} from "../add-dimension-dialog/add-dimension-dialog.utils";
import { getAssociationId } from "../alerts-dimensions.utils";
import { AlertDimensionTableProps } from "./alert-dimension-table.interface";

export const AlertsTable: FunctionComponent<AlertDimensionTableProps> = ({
    selectedAlert,
    associations,
    enumerationItemsForAlert,
    onChangeAssociations,
}) => {
    const { t } = useTranslation();

    const AllDimensionsSelectedRow: DimensionRow = {
        id: getAssociationId({ alertId: selectedAlert.id }),
        name: t("label.overall-entity", {
            entity: t("label.alert"),
        }),
    };

    const [selectedRow, setSelectedRow] = useState<
        DataGridSelectionModelV1<DimensionRow>
    >(() =>
        getSelectedRows({
            AllDimensionsSelectedRow,
            selectedAlertId: selectedAlert.id,
        })
    );

    useEffect(() => {
        setSelectedRow(() =>
            getSelectedRows({
                associations,
                enumerationItems: enumerationItemsForAlert,
                allSelected,
                AllDimensionsSelectedRow,
                selectedAlertId: selectedAlert.id,
            })
        );
    }, [selectedAlert]);

    const allSelected = areAllRowsSelected({
        selectedRow,
        alertId: selectedAlert.id,
    });

    const dataRows: DimensionRow[] = getDataRowsForAlert({
        AllDimensionsSelectedRow,
        enumerationItemsForAlert,
        allSelected,
        alertId: selectedAlert.id,
    });
    const handleSelectionChange = (
        newRows: DataGridSelectionModelV1<DimensionRow>
    ): void => {
        setSelectedRow(newRows);

        const areAllSelectedForNewData = areAllRowsSelected({
            selectedRow: newRows,
            alertId: selectedAlert.id,
        });

        onChangeAssociations(
            (areAllSelectedForNewData
                ? [getAssociationId({ alertId: selectedAlert.id })]
                : newRows.rowKeyValues) as string[]
        );
    };

    const dataGridProps: DataGridV1Props<DimensionRow> = {
        data: dataRows,
        rowKey: "id",
        hideBorder: true,
        columns: [
            {
                key: "id",
                dataKey: "id",
                header: t("label.id"),
                minWidth: 150,
                flex: 1,
            },
            {
                key: "name",
                dataKey: "name",
                header: t("label.dimensions"),
                minWidth: 150,
                flex: 1,
            },
        ],
        selectionModel: selectedRow,
        onSelectionChange: handleSelectionChange,
        toolbarComponent: (
            <Typography variant="h6">
                {t("message.select-entity", {
                    entity: t("label.dimensions"),
                })}
            </Typography>
        ),
    };

    return (
        <Box height={500}>
            <DataGridV1<DimensionRow> {...dataGridProps} />
        </Box>
    );
};
