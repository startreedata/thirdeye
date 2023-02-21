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

import { Box } from "@material-ui/core";
import { sortBy } from "lodash";
import React, { FunctionComponent, ReactNode, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { DataGridColumnV1, DataGridV1 } from "../../../platform/components";
import { DataGridV1Props } from "../../../platform/components/data-grid-v1/data-grid-v1/data-grid-v1.interfaces";
import { linkRendererV1 } from "../../../platform/utils";
import { getAlertsAlertPath } from "../../../utils/routes/routes.util";
import {
    AlertAssociationsViewTableProps,
    UiAssociation,
} from "./alert-associations-view-table.interface";
import { getUiAssociation } from "./alert-associations-view-table.utils";

export const AlertAssociationsViewTable: FunctionComponent<AlertAssociationsViewTableProps> =
    ({ uiSubscriptionGroup }) => {
        const { t } = useTranslation();
        const uiAssociations = getUiAssociation(uiSubscriptionGroup.alerts, t);

        const alertNameRenderer = useCallback(
            (
                cellValue: Record<string, unknown>,
                data: UiAssociation
            ): ReactNode =>
                linkRendererV1(
                    cellValue,
                    getAlertsAlertPath(data.alertId),
                    false,
                    `${t("label.view")} ${t("label.alert")}:${cellValue}`
                ),
            []
        );

        const dataGridColumns: DataGridColumnV1<UiAssociation>[] = [
            {
                key: "alertName",
                dataKey: "alertName",
                header: t("label.alert-name"),
                minWidth: 150,
                flex: 1,
                customCellRenderer: alertNameRenderer,
            },
            {
                key: "enumerationName",
                dataKey: "enumerationName",
                header: t("label.dimensions"),
                minWidth: 150,
                flex: 1,
            },
        ];

        const dataGridProps: DataGridV1Props<UiAssociation> = {
            data: sortBy(uiAssociations, "id"),
            columns: dataGridColumns,
            rowKey: "id",
            hideBorder: true,
            disableSelection: true,
        };

        return (
            <Box height={400}>
                <DataGridV1<UiAssociation> {...dataGridProps} />
            </Box>
        );
    };
