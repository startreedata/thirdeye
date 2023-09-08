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

import { Table, TableCell, TableHead, TableRow } from "@material-ui/core";
import { sortBy } from "lodash";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { EnumerationItem } from "../../../../rest/dto/enumeration-item.interfaces";
import { AlertEnumerationItemSelectTableProps } from "./alert-enuemration-item-select-table.interfaces";
import { AlertRow } from "./alert-row/alert-row.component";

export const AlertEnumerationItemSelectTable: FunctionComponent<AlertEnumerationItemSelectTableProps> =
    ({
        alerts,
        enumerationItems,
        associations,
        onAssociationChange,
        showOnlyDimensionExploration,
        showOnlyBasic,
        filterTerm,
    }) => {
        const { t } = useTranslation();

        const allRows = useMemo(() => {
            const alertToEnumerationItems: {
                [key: number]: EnumerationItem[];
            } = {};

            enumerationItems.forEach((enumerationItem) => {
                if (enumerationItem.alert?.id) {
                    const bucket =
                        alertToEnumerationItems[enumerationItem.alert.id] || [];
                    bucket.push(enumerationItem);

                    alertToEnumerationItems[enumerationItem.alert.id] = bucket;
                }
            });

            return sortBy(alerts, "created")
                .reverse()
                .map((alert) => {
                    return {
                        alert,
                        enumerationItems:
                            alertToEnumerationItems[alert.id] || [],
                    };
                });
        }, [alerts, enumerationItems]);

        const filteredRows = useMemo(() => {
            let rows = [...allRows];

            if (showOnlyDimensionExploration) {
                rows = rows.filter((row) => row.enumerationItems.length > 0);
            } else if (showOnlyBasic) {
                rows = rows.filter((row) => row.enumerationItems.length === 0);
            }

            if (filterTerm) {
                const lowered = filterTerm.toLowerCase();
                rows = rows.filter((row) =>
                    row.alert.name.toLowerCase().includes(lowered)
                );
            }

            return rows;
        }, [allRows, filterTerm, showOnlyDimensionExploration, showOnlyBasic]);

        return (
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell padding="checkbox" />
                        <TableCell padding="checkbox">
                            <strong>{t("label.subscribe")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.alert-name")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.dimensions")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.subscribed-to")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.created")}</strong>
                        </TableCell>
                    </TableRow>
                </TableHead>
                {filteredRows.map((row) => (
                    <AlertRow
                        alert={row.alert}
                        associations={associations}
                        enumerationItems={row.enumerationItems}
                        key={row.alert.name}
                        onAssociationChange={onAssociationChange}
                    />
                ))}
            </Table>
        );
    };
