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
import { Alert } from "../../../../rest/dto/alert.interfaces";
import { useGetEnumerationItems } from "../../../../rest/enumeration-items/enumeration-items.actions";
import { getMapFromList } from "../../../../utils/subscription-groups/subscription-groups.util";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { EmptyStateSwitch } from "../../../page-states/empty-state-switch/empty-state-switch.component";
import { Association } from "../../subscription-group-wizard.interfaces";
import { AlertsTable } from "../alert-dimension-table/alert-dimension-table.component";
import { getAssociationId } from "../alerts-dimensions.utils";
import { AddDimensionDialogProps } from "./add-dimension-dialog.interfaces";
import {
    getAssociationIdsForAlert,
    getEnumerationItemsForAlert,
} from "./add-dimension-dialog.utils";

export const AddDimensionsDialog: FunctionComponent<AddDimensionDialogProps> =
    ({ alerts, associations, updateAssociations }) => {
        const [associationsSelected, setAssociationsSelected] = useState<
            Record<string, boolean>
        >(
            Object.assign(
                {},
                ...associations.map((item) => ({ [item.id]: true }))
            )
        );

        const [selectedAlertId, setSelectedAlertId] = useState<number | null>(
            null
        );
        const { t } = useTranslation();

        const alertsMap = getMapFromList(alerts);

        const { getEnumerationItems, enumerationItems } =
            useGetEnumerationItems();

        useEffect(() => {
            getEnumerationItems();
        }, []);

        useEffect(() => {
            const associationsList: Association[] = Object.entries(
                associationsSelected
            )
                .filter(([, isChecked]) => isChecked)
                .map(([associationId]) => associationId)
                .map((associationId) => {
                    const [stringAlertId, stringEnumerationId] =
                        associationId.split("-");

                    const alertId = Number(stringAlertId);
                    const enumerationId = stringEnumerationId
                        ? Number(stringEnumerationId)
                        : undefined;

                    const association: Association = {
                        id: getAssociationId({
                            alertId,
                            enumerationId,
                        }),
                        alertId,
                        ...(enumerationId && {
                            enumerationId,
                        }),
                    };

                    return association;
                }) as Association[];

            updateAssociations(associationsList);
        }, [associationsSelected]);

        const selectedAlert =
            (selectedAlertId && alertsMap.get(selectedAlertId)) || null;

        const associationsForAlert = associations.filter(
            (item) => item.alertId === selectedAlertId
        );

        const enumerationItemsForAlert =
            selectedAlert && enumerationItems
                ? getEnumerationItemsForAlert(enumerationItems, selectedAlert)
                : [];

        const associationIdsForAlert = selectedAlertId
            ? getAssociationIdsForAlert({
                  enumerationIds: enumerationItemsForAlert.map(
                      (item) => item.id
                  ),
                  alertId: selectedAlertId,
              })
            : [];

        const handleChangeAssociations = (
            newAssociationIds: string[]
        ): void => {
            associationIdsForAlert.forEach((associationId) => {
                setAssociationsSelected((stateProp) => ({
                    ...stateProp,
                    [associationId]: newAssociationIds.includes(associationId),
                }));
            });
        };

        return (
            <Box>
                <Typography variant="h6">{t("label.alert")}</Typography>

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
                        />
                    }
                    label={t("message.select-entity", {
                        entity: t("label.alert"),
                    })}
                />

                <EmptyStateSwitch
                    emptyState={null}
                    isEmpty={!(selectedAlert && enumerationItems)}
                >
                    <>
                        <Box pb={2} pt={2}>
                            <Divider />
                        </Box>

                        <AlertsTable
                            associations={associationsForAlert}
                            enumerationItemsForAlert={enumerationItemsForAlert}
                            selectedAlert={selectedAlert as Alert}
                            onChangeAssociations={handleChangeAssociations}
                        />
                    </>
                </EmptyStateSwitch>
            </Box>
        );
    };
