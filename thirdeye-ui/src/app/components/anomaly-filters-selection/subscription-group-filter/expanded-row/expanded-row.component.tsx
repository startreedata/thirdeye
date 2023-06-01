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
    TableBody,
    TableCell,
    TableHead,
    TableRow,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { EnumerationItem } from "../../../../rest/dto/enumeration-item.interfaces";
import { UiSubscriptionGroupAlert } from "../../../../rest/dto/ui-subscription-group.interfaces";
import { ExpandedRowProps } from "./expanded-row.interface";

export const ExpandedRow: FunctionComponent<ExpandedRowProps> = ({
    uiSubscriptionGroup,
}) => {
    const { t } = useTranslation();

    return (
        <Box padding={2} paddingBottom={1} paddingTop={1}>
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>
                            <strong>{t("label.alert-name")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.dimensions")}</strong>
                        </TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {uiSubscriptionGroup.alerts.map(
                        (row: UiSubscriptionGroupAlert) => (
                            <TableRow key={row.name}>
                                <TableCell component="th" scope="row">
                                    {row.name}
                                </TableCell>
                                <TableCell>
                                    {row.enumerationItems &&
                                        row.enumerationItems
                                            .map((e: EnumerationItem) => e.name)
                                            .join("")}
                                    {row.enumerationItems === undefined &&
                                        t(
                                            "message.non-dimension-exploration-alert"
                                        )}
                                </TableCell>
                            </TableRow>
                        )
                    )}
                </TableBody>
            </Table>
        </Box>
    );
};
