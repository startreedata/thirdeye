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
    Box,
    Checkbox,
    TableBody,
    TableCell,
    TableRow,
} from "@material-ui/core";
import IconButton from "@material-ui/core/IconButton";
import KeyboardArrowDownIcon from "@material-ui/icons/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@material-ui/icons/KeyboardArrowUp";
import React, { FunctionComponent, useMemo } from "react";
import { formatDateV1 } from "../../../../../platform/utils";
import {
    getAssociationId,
    hasAlertAssociation,
} from "../../alerts-dimensions.utils";
import { AlertRowProps } from "./alert-row.interfaces";
import { EnumerationItemsTable } from "./enumeration-items-table/enumeration-items-table.component";

export const AlertRow: FunctionComponent<AlertRowProps> = ({
    alert,
    enumerationItems,
    associations,
    onAssociationChange,
}) => {
    const isDimensionExplorationAlert = enumerationItems.length > 0;
    const [isOpen, setIsOpen] = React.useState(false);

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

    const [isDimensionSelectOn, setIsDimensionSelectOn] = React.useState(() => {
        return associatedEnumerationItems.length > 0;
    });

    /**
     * Alert checked means that there are dimensions subscribed to
     * or the entire alert is subscribed to
     */
    const [isAlertChecked, setIsAlertChecked] = React.useState(() => {
        return (
            associatedEnumerationItems.length > 0 ||
            hasAlertAssociation(alert, associations)
        );
    });

    const handleAlertRowCheckedChange = (
        _: unknown,
        checked: boolean
    ): void => {
        // Remove all associations with enumeration items and alert level when unchecked
        onAssociationChange((currentAssociations) => {
            let copied = [...currentAssociations];

            // Remove any associations with this alert id by default
            copied = copied.filter(
                (association) => association.alertId !== alert.id
            );

            // Add alert level if checked
            if (checked) {
                copied.push({
                    alertId: alert.id,
                    id: getAssociationId({
                        alertId: alert.id,
                        enumerationId: undefined,
                    }),
                });
            }

            return copied;
        });
        setIsAlertChecked(checked);

        // If alert is unchecked, reset isDimensionSelectOn
        if (!checked) {
            setIsDimensionSelectOn(false);
        }
    };

    const handleSelectDimensionSwitchChange = (isOn: boolean): void => {
        setIsDimensionSelectOn(isOn);

        if (isOn) {
            setIsAlertChecked(true);
        } else {
            if (!hasAlertAssociation(alert, associations)) {
                setIsAlertChecked(false);
            }
        }
    };

    return (
        <TableBody>
            <TableRow>
                <TableCell align="left" padding="checkbox">
                    {enumerationItems.length > 0 && (
                        <IconButton
                            color="primary"
                            size="small"
                            onClick={() => setIsOpen(!isOpen)}
                        >
                            {isOpen ? (
                                <KeyboardArrowUpIcon />
                            ) : (
                                <KeyboardArrowDownIcon />
                            )}
                        </IconButton>
                    )}
                </TableCell>
                <TableCell align="left" padding="checkbox">
                    <Checkbox
                        checked={isAlertChecked}
                        color="primary"
                        onChange={handleAlertRowCheckedChange}
                    />
                </TableCell>
                <TableCell component="th" scope="row">
                    {alert.name}
                </TableCell>
                <TableCell>
                    {isDimensionExplorationAlert
                        ? enumerationItems.length
                        : "-"}
                </TableCell>
                <TableCell>
                    {isDimensionExplorationAlert
                        ? isDimensionSelectOn
                            ? associatedEnumerationItems.length
                            : isAlertChecked
                            ? enumerationItems.length
                            : 0
                        : "-"}
                </TableCell>
                <TableCell>{formatDateV1(alert.created)}</TableCell>
            </TableRow>
            {isOpen && (
                <TableRow>
                    <TableCell padding="checkbox" />
                    <TableCell padding="checkbox" />
                    <TableCell
                        colSpan={3}
                        style={{
                            paddingBottom: 0,
                            paddingTop: 0,
                        }}
                    >
                        <Box pb={2} pl={3} pr={3} pt={2}>
                            <EnumerationItemsTable
                                alert={alert}
                                associations={associations}
                                enumerationItems={enumerationItems}
                                isDimensionSelectOn={isDimensionSelectOn}
                                onAssociationChange={onAssociationChange}
                                onSelectDimensionSwitchChange={
                                    handleSelectDimensionSwitchChange
                                }
                            />
                        </Box>
                    </TableCell>
                </TableRow>
            )}
        </TableBody>
    );
};
