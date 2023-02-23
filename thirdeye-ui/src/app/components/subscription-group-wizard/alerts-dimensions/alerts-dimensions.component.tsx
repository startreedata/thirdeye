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
import { capitalize, sortBy } from "lodash";
import React, { FunctionComponent, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { DataGridV1, useDialogProviderV1 } from "../../../platform/components";
import {
    DataGridColumnV1,
    DataGridSelectionModelV1,
    DataGridV1Props,
} from "../../../platform/components/data-grid-v1/data-grid-v1/data-grid-v1.interfaces";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { getMapFromList } from "../../../utils/subscription-groups/subscription-groups.util";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { Association } from "../subscription-group-wizard.interfaces";
import { AddDimensionsDialog } from "./add-dimension-dialog/add-dimension-dialog.component";
import { AlertsDimensionsProps } from "./alerts-dimensions.interfaces";
import {
    getAssociationId,
    getEnumerationItemName,
} from "./alerts-dimensions.utils";

export const AlertsDimensions: FunctionComponent<AlertsDimensionsProps> = ({
    alerts,
    enumerationItems,
    associations,
    setAssociations,
}) => {
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const alertItemsMap = getMapFromList(alerts);
    const enumerationItemsMap = getMapFromList(enumerationItems);

    const dataGridRows = sortBy(
        associations
            .map(({ alertId, enumerationId }) => ({
                id: getAssociationId({ alertId, enumerationId }),
                alertId,
                enumerationId,
            }))
            .map(({ id, alertId, enumerationId }) => ({
                id,
                alertId,
                enumerationId,
                alertName: alertItemsMap.get(alertId)?.name,
                enumerationName: enumerationId
                    ? getEnumerationItemName(
                          enumerationItemsMap.get(enumerationId)
                      )
                    : t("label.overall-entity", { entity: t("label.alert") }),
            })),
        "id"
    );
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

    // This value needs to be used in the dialog's props and onOk callback,
    // so a ref is being used instead of a state
    const associationsFromDialog = useRef<Association[]>(associations);
    const setAssociationsFromDialog = (newValue: Association[]): void => {
        associationsFromDialog.current = newValue;
    };

    const handleDeleteAssociation = (): void => {
        const idsToDelete = selectedRow.rowKeyValues;

        setAssociations((associations) =>
            associations.filter((item) => !idsToDelete.includes(item.id))
        );

        setSelectedRow((stateProp) => {
            const newRowKeyValues = stateProp.rowKeyValues.filter(
                (id) => !idsToDelete.includes(id)
            );

            const newRowKeyValueMap = new Map(
                stateProp.rowKeyValueMap?.entries() || []
            );

            idsToDelete.forEach((id) => {
                newRowKeyValueMap.delete(id);
            });

            return {
                rowKeyValues: newRowKeyValues,
                rowKeyValueMap: newRowKeyValueMap,
            };
        });
    };

    const dataGridProps: DataGridV1Props<DataRow> = {
        data: dataGridRows,
        rowKey: "id",
        columns: dataGridColumns,
        selectionModel: selectedRow,
        onSelectionChange: setSelectedRow,
        hideBorder: true,
        toolbarComponent: (
            <Button
                disabled={selectedRow.rowKeyValues.length === 0}
                variant="contained"
                onClick={handleDeleteAssociation}
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
                    updateAssociations={setAssociationsFromDialog}
                />
            ),
            okButtonText: t("label.add"),
            cancelButtonText: t("label.cancel"),
            onOk: () => {
                setAssociations(associationsFromDialog.current);
            },
            headerText: t("message.add-alert-dimensions"),
        });
    };

    const isDataEmpty = dataGridRows.length === 0;

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
                </Box>
                <Button
                    color="primary"
                    variant="contained"
                    onClick={handleAddDimensions}
                >
                    {t("message.add-alert-dimensions")}
                </Button>
            </Box>
            <Card variant="outlined">
                <CardContent>
                    <Box height={isDataEmpty ? 100 : 500}>
                        <DataGridV1<DataRow> {...dataGridProps} />
                    </Box>
                    <EmptyStateSwitch emptyState={null} isEmpty={!isDataEmpty}>
                        <Box
                            alignItems="center"
                            display="flex"
                            flexDirection="column"
                            justifyContent="center"
                            py={8}
                        >
                            <Typography variant="body2">
                                {capitalize(
                                    t(
                                        "message.no-children-present-for-this-parent",
                                        {
                                            children: t("label.active-entity", {
                                                entity: t("label.dimensions"),
                                            }),
                                            parent: t(
                                                "label.subscription-group"
                                            ),
                                        }
                                    )
                                )}
                            </Typography>
                            <br />
                            <Button
                                color="primary"
                                variant="contained"
                                onClick={handleAddDimensions}
                            >
                                {t("message.add-alert-dimensions")}
                            </Button>
                        </Box>
                    </EmptyStateSwitch>
                </CardContent>
            </Card>
        </Grid>
    );
};
