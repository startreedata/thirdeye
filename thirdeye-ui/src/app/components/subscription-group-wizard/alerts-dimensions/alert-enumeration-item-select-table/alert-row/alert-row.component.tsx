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
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Typography,
} from "@material-ui/core";
import IconButton from "@material-ui/core/IconButton";
import KeyboardArrowDownIcon from "@material-ui/icons/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@material-ui/icons/KeyboardArrowUp";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { formatDateV1 } from "../../../../../platform/utils";
import { getAssociationId } from "../../alerts-dimensions.utils";
import { EnumerationItemRow } from "../enumeration-item-row/enumeration-item-row.component";
import { AlertRowProps } from "./alert-row.interfaces";

export const AlertRow: FunctionComponent<AlertRowProps> = ({
    alert,
    enumerationItems,
    associations,
    onAssociationChange,
}) => {
    const { t } = useTranslation();
    const [isOpen, setIsOpen] = React.useState(false);
    const isDimensionExplorationAlert = enumerationItems.length > 0;

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

    const handleAlertRowCheckedChange = (
        _: unknown,
        checked: boolean
    ): void => {
        onAssociationChange((currentAssociations) => {
            let copied = [...currentAssociations];

            // Remove any associations with this alert id by default
            copied = copied.filter(
                (association) => association.alertId !== alert.id
            );

            if (checked) {
                if (isDimensionExplorationAlert) {
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
                } else {
                    copied.push({
                        alertId: alert.id,
                        id: getAssociationId({
                            alertId: alert.id,
                            enumerationId: undefined,
                        }),
                    });
                }
            }

            return copied;
        });
    };

    /**
     * Being checked means that all dimension explorations match if alert is
     * a dimension exploration alert or the alert itself is in the association
     */
    const isChecked = isDimensionExplorationAlert
        ? associatedEnumerationItems.length === enumerationItems.length
        : associations.find(
              (candidate) =>
                  candidate.alertId === alert.id &&
                  candidate.enumerationId === undefined
          ) !== undefined;

    return (
        <TableBody>
            <TableRow>
                <TableCell align="left" padding="checkbox">
                    <Checkbox
                        checked={isChecked}
                        onChange={handleAlertRowCheckedChange}
                    />
                </TableCell>
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
                        ? associatedEnumerationItems.length
                        : "-"}
                </TableCell>
                <TableCell>{formatDateV1(alert.created)}</TableCell>
            </TableRow>
            {isOpen && (
                <TableRow>
                    <TableCell
                        colSpan={6}
                        style={{
                            paddingBottom: 0,
                            paddingTop: 0,
                        }}
                    >
                        <Box pb={2} pl={3} pr={3} pt={2}>
                            <Typography
                                gutterBottom
                                component="div"
                                variant="h6"
                            >
                                {t("label.dimensions")}
                            </Typography>
                            <Table size="small">
                                <TableHead>
                                    <TableRow>
                                        <TableCell>
                                            <strong>
                                                {t("label.subscribe")}
                                            </strong>
                                        </TableCell>
                                        <TableCell>
                                            <strong>{t("label.name")}</strong>
                                        </TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {enumerationItems.map((enumerationItem) => (
                                        <EnumerationItemRow
                                            alert={alert}
                                            associations={associations}
                                            enumerationItem={enumerationItem}
                                            key={enumerationItem.id}
                                            onAssociationChange={
                                                onAssociationChange
                                            }
                                        />
                                    ))}
                                </TableBody>
                            </Table>
                        </Box>
                    </TableCell>
                </TableRow>
            )}
        </TableBody>
    );
};
