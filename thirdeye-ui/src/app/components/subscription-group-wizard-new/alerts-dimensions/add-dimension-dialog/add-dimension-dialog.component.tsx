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

import { Box, Divider, TextField, Typography } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    AppLoadingIndicatorV1,
    DataGridSelectionModelV1,
    DataGridV1,
} from "../../../../platform/components";
import { DataGridV1Props } from "../../../../platform/components/data-grid-v1/data-grid-v1/data-grid-v1.interfaces";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { Alert } from "../../../../rest/dto/alert.interfaces";
import { useGetEnumerationItems } from "../../../../rest/enumeration-items/enumeration-items.actions";
import { getMapFromList } from "../../../../utils/subscription-groups/subscription-groups.util";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { LoadingErrorStateSwitch } from "../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    AddDimensionDialogProps,
    DimensionRow,
} from "./add-dimension-dialog.interface";
import {
    ALL_DIMENSIONS_INDEX,
    getDimensionRows,
    getSelectedRows,
} from "./add-dimension-dialog.utils";

export const AddDimensionsDialog: FunctionComponent<AddDimensionDialogProps> =
    ({ alerts, associations }) => {
        const { t } = useTranslation();
        const { getEnumerationItems, enumerationItems, status } =
            useGetEnumerationItems();

        const AllDimensionsSelectedRow: DimensionRow = {
            id: ALL_DIMENSIONS_INDEX,
            name: t("label.overall-entity", {
                entity: t("label.alert"),
            }),
        };

        const [selectedRow, setSelectedRow] = useState<
            DataGridSelectionModelV1<DimensionRow>
        >(() => getSelectedRows({ AllDimensionsSelectedRow }));

        const [selectedAlertId, setSelectedAlertId] = useState<number | null>(
            null
        );
        const alertsMap = getMapFromList(alerts);

        const selectedAlert =
            (selectedAlertId && alertsMap.get(selectedAlertId)) || null;

        const allSelected =
            selectedRow.rowKeyValues.includes(ALL_DIMENSIONS_INDEX);

        useEffect(() => {
            getEnumerationItems().then((enumerationItemsProp) => {
                setSelectedRow(() =>
                    getSelectedRows({
                        associations,
                        enumerationItems: enumerationItemsProp,
                        allSelected,
                        AllDimensionsSelectedRow,
                    })
                );
            });
        }, []);

        const dataRows: DimensionRow[] = [
            AllDimensionsSelectedRow,
            ...(allSelected
                ? []
                : getDimensionRows(
                      (enumerationItems || []).filter((enumerationItem) =>
                          (enumerationItem.alerts || [])?.some(
                              (a) => a.id === selectedAlertId
                          )
                      )
                  )),
        ];
        const handleSelectionChange = (
            newRows: DataGridSelectionModelV1<DimensionRow>
        ): void => {
            setSelectedRow(newRows);
        };

        const dataGridProps: DataGridV1Props<DimensionRow> = {
            data: dataRows,
            rowKey: "id",
            hideBorder: true,
            columns: [
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
                <>
                    <Typography variant="h6">
                        {t("message.select-entity", {
                            entity: t("label.dimensions"),
                        })}
                    </Typography>
                </>
            ),
        };

        return (
            <Box>
                <Typography variant="h6">
                    {t("message.select-entity", { entity: t("label.alert") })}
                </Typography>

                <InputSection
                    fullWidth
                    inputComponent={
                        <Autocomplete<Alert>
                            getOptionLabel={(option) => option.name}
                            options={alerts}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    InputProps={{
                                        ...params.InputProps,
                                    }}
                                    placeholder={t("message.select-entity", {
                                        entity: t("label.alert"),
                                    })}
                                    variant="outlined"
                                />
                            )}
                            value={selectedAlert}
                            onChange={(_, alertProp) => {
                                if (!alertProp) {
                                    return;
                                }

                                setSelectedAlertId(alertProp.id);
                            }}
                            // error={Boolean(
                            //     errors && errors.name
                            // )}
                            // helperText={
                            //     errors?.name?.message
                            // }
                        />
                    }
                    label={t("message.select-entity", {
                        entity: t("label.alert"),
                    })}
                />

                <Box pb={2} pt={2}>
                    <Divider />
                </Box>

                <Box height={500}>
                    <LoadingErrorStateSwitch
                        isError={status === ActionStatus.Error}
                        isLoading={status === ActionStatus.Working}
                        loadingState={<AppLoadingIndicatorV1 />}
                    >
                        <DataGridV1<DimensionRow> {...dataGridProps} />
                    </LoadingErrorStateSwitch>
                </Box>
            </Box>
        );
    };
