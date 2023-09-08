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

import {
    Checkbox,
    FormControlLabel,
    Grid,
    Switch,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Typography,
} from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
    getAssociationId,
    hasAlertAssociation,
} from "../../../alerts-dimensions.utils";
import { EnumerationItemRow } from "../../enumeration-item-row/enumeration-item-row.component";
import { EnumerationItemsTableProps } from "./enumeration-items-table.interfaces";

export const EnumerationItemsTable: FunctionComponent<EnumerationItemsTableProps> =
    ({
        alert,
        enumerationItems,
        associations,
        onAssociationChange,
        isDimensionSelectOn,
        onSelectDimensionSwitchChange,
    }) => {
        const { t } = useTranslation();

        const associatedEnumerationItems = useMemo(() => {
            return enumerationItems.filter((enumerationItem) =>
                associations.find(
                    (candidate) =>
                        candidate.id ===
                        getAssociationId({
                            alertId: alert.id,
                            enumerationId: enumerationItem?.id,
                        })
                )
            );
        }, [alert, enumerationItems, associations]);

        const handleColumnCheckedChange = (shouldCheckAll: boolean): void => {
            onAssociationChange((currentAssociations) => {
                let copied = [...currentAssociations];

                // Remove any enumeration items associations with this alert id by default
                copied = copied.filter(
                    (association) =>
                        !(
                            association.enumerationId !== undefined &&
                            association.alertId === alert.id
                        )
                );

                if (shouldCheckAll) {
                    enumerationItems.forEach((enumerationItem) => {
                        copied.push({
                            alertId: alert.id,
                            enumerationId: enumerationItem.id,
                            id: getAssociationId({
                                alertId: alert.id,
                                enumerationId: enumerationItem.id,
                            }),
                        });
                    });
                }

                return copied;
            });
        };

        /**
         * Being checked means that all dimension explorations match if alert is
         * a dimension exploration alert or the alert itself is in the association
         */
        const isAllEnumerationItemsSelected =
            associatedEnumerationItems.length > 0 &&
            associatedEnumerationItems.length === enumerationItems.length;
        const shouldShowAllSelected =
            isAllEnumerationItemsSelected ||
            hasAlertAssociation(alert, associations);

        const handleSubscribeToDimensionsChange = (): void => {
            const isSelectedDimensionModeOn = !isDimensionSelectOn;

            if (!isSelectedDimensionModeOn) {
                onAssociationChange((currentAssociations) => {
                    let copied = [...currentAssociations];

                    // Remove any enumeration item associations with this alert id by default
                    copied = copied.filter(
                        (association) =>
                            !(
                                association.enumerationId !== undefined &&
                                association.alertId === alert.id
                            )
                    );

                    return copied;
                });
            }

            onSelectDimensionSwitchChange(isSelectedDimensionModeOn);
        };

        return (
            <>
                {isDimensionSelectOn &&
                    associatedEnumerationItems.length === 0 && (
                        <Alert severity="error" variant="standard">
                            Select at least one dimension
                        </Alert>
                    )}
                <Grid container justifyContent="space-between">
                    <Grid item>
                        <Typography gutterBottom component="div" variant="h6">
                            {t("label.dimensions")}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <FormControlLabel
                            checked={isDimensionSelectOn}
                            control={<Switch color="primary" />}
                            label="Subscribe to individual dimensions"
                            onChange={handleSubscribeToDimensionsChange}
                        />
                    </Grid>
                </Grid>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell padding="checkbox">
                                <Checkbox
                                    checked={
                                        (isDimensionSelectOn &&
                                            isAllEnumerationItemsSelected) ||
                                        (!isDimensionSelectOn &&
                                            shouldShowAllSelected)
                                    }
                                    color={
                                        isDimensionSelectOn
                                            ? "primary"
                                            : undefined
                                    }
                                    disabled={!isDimensionSelectOn}
                                    indeterminate={
                                        isDimensionSelectOn &&
                                        associatedEnumerationItems.length > 0 &&
                                        associatedEnumerationItems.length !==
                                            enumerationItems.length
                                    }
                                    onChange={() => {
                                        handleColumnCheckedChange(
                                            associatedEnumerationItems.length ===
                                                0 ||
                                                associatedEnumerationItems.length !==
                                                    enumerationItems.length
                                        );
                                    }}
                                />
                            </TableCell>
                            <TableCell>
                                <strong>{t("label.subscribe-to")}</strong>
                            </TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {enumerationItems.map((enumerationItem) => (
                            <EnumerationItemRow
                                alert={alert}
                                associations={associations}
                                enumerationItem={enumerationItem}
                                isDimensionSelectOn={isDimensionSelectOn}
                                key={enumerationItem.id}
                                overrideSelected={
                                    !isDimensionSelectOn &&
                                    shouldShowAllSelected
                                }
                                onAssociationChange={onAssociationChange}
                            />
                        ))}
                    </TableBody>
                </Table>
            </>
        );
    };
