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

import {
    Box,
    Button,
    Card,
    CardContent,
    Grid,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { DataGridV1, useDialogProviderV1 } from "../../../platform/components";
import {
    DataGridColumnV1,
    DataGridSelectionModelV1,
    DataGridV1Props,
} from "../../../platform/components/data-grid-v1/data-grid-v1/data-grid-v1.interfaces";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { getMapFromList } from "../../../utils/subscription-groups/subscription-groups.util";
import { AddDimensionsDialog } from "./add-dimension-dialog/add-dimension-dialog.component";
import { AlertsDimensionsProps } from "./alerts-dimensions.interface";
import { getEnumerationItemName } from "./alerts-dimensions.utils";

export const AlertsDimensions: FunctionComponent<AlertsDimensionsProps> = ({
    alerts,
    enumerationItems,
    associations,
    handleChangeAssociations,
}) => {
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const alertItemsMap = getMapFromList(alerts);
    const enumerationItemsMap = getMapFromList(enumerationItems);

    const dataGridRows = associations
        .map(({ alertId, enumerationId }) => ({
            id: `${alertId}${enumerationId ? `-${enumerationId}` : ""}`,
            alertId,
            enumerationId,
        }))
        .map(({ id, alertId, enumerationId }) => ({
            id,
            alertId,
            enumerationId,
            alertName: alertItemsMap.get(alertId)?.name,
            enumerationName:
                (enumerationId &&
                    getEnumerationItemName(
                        enumerationItemsMap.get(enumerationId)
                    )) ||
                t("label.overall-entity", { entity: t("label.alert") }),
        }));

    type DataRow = typeof dataGridRows[0];

    const [selectedRow, setSelectedRow] = useState<
        DataGridSelectionModelV1<DataRow>
    >(() => ({
        rowKeyValues: [],
        rowKeyValueMap: new Map(),
    }));

    const dataGridColumns: DataGridColumnV1<DataRow>[] = [
        {
            key: "alertName",
            dataKey: "alertName",
            header: t("label.alert-name"),
            minWidth: 150,
            flex: 1,
        },
        {
            key: "enumerationName",
            dataKey: "enumerationName",
            header: t("label.dimensions"),
            minWidth: 150,
            flex: 1,
        },
    ];

    const dataGridProps: DataGridV1Props<DataRow> = {
        data: dataGridRows,
        rowKey: "id",
        // hideBorder: true,
        columns: dataGridColumns,
        selectionModel: selectedRow,
        onSelectionChange: setSelectedRow,
        // TODO: Add delete button
        toolbarComponent: (
            <Button
                // data-testid="button-delete"
                // disabled={isActionButtonDisable}
                variant="contained"
                // onClick={handleAnomalyDelete}
            >
                {t("label.delete")}
            </Button>
        ),
    };

    const handleAddDimensions = (): void => {
        showDialog({
            type: DialogType.CUSTOM,
            width: "md",
            contents: (
                <AddDimensionsDialog
                    alerts={alerts}
                    associations={associations}
                />
            ),
            okButtonText: t("label.add"),
            cancelButtonText: t("label.cancel"),
            // onOk: () => {},
            headerText: t("message.add-alert-dimensions"),
        });
    };

    return (
        <Grid item xs={12}>
            <Box
                alignItems="center"
                display="flex"
                gridGap={12}
                justifyContent="space-between"
                py={2}
            >
                <Box>
                    <Typography variant="h5">
                        {t("label.alerts-and-dimensions")}
                    </Typography>
                    <Typography variant="subtitle1">
                        Add description here
                    </Typography>
                </Box>
                <Button
                    color="primary"
                    // disabled={isActionButtonDisable}
                    variant="contained"
                    onClick={handleAddDimensions}
                >
                    {t("message.add-alert-dimensions")}
                </Button>
            </Box>
            <Card variant="outlined">
                <CardContent>
                    <Box height={500}>
                        <DataGridV1<DataRow> {...dataGridProps} />
                    </Box>
                </CardContent>
            </Card>
        </Grid>
    );
};
