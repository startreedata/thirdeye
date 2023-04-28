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

import { Icon } from "@iconify/react";
import { Box, Typography, useTheme } from "@material-ui/core";
import { capitalize, sortBy } from "lodash";
import React, { FunctionComponent, ReactNode, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { DataGridColumnV1, DataGridV1 } from "../../../platform/components";
import { DataGridV1Props } from "../../../platform/components/data-grid-v1/data-grid-v1/data-grid-v1.interfaces";
import { linkRendererV1 } from "../../../platform/utils";
import { getAlertsAlertPath } from "../../../utils/routes/routes.util";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import {
    AlertAssociationsViewTableProps,
    UiAssociation,
} from "./alert-associations-view-table.interfaces";
import { getUiAssociation } from "./alert-associations-view-table.utils";

export const AlertAssociationsViewTable: FunctionComponent<AlertAssociationsViewTableProps> =
    ({ uiSubscriptionGroup, customToolbar }) => {
        const { t } = useTranslation();
        const theme = useTheme();
        const uiAssociations = getUiAssociation(
            uiSubscriptionGroup.alerts || [],
            t
        );

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
            toolbarComponent: customToolbar,
        };

        return (
            <>
                <EmptyStateSwitch
                    emptyState={
                        <Box
                            alignItems="center"
                            display="flex"
                            flexDirection="column"
                            justifyContent="center"
                            py={8}
                        >
                            <Icon
                                color={theme.palette.primary.main}
                                fontSize={32}
                                icon="mdi:chart-line-variant"
                            />
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
                            <Typography color="textSecondary" variant="caption">
                                {capitalize(
                                    t(
                                        "message.active-entity-will-be-listed-here",
                                        {
                                            entity: t("label.dimensions"),
                                        }
                                    )
                                )}
                            </Typography>
                        </Box>
                    }
                    isEmpty={uiAssociations.length === 0}
                >
                    <Box height={400}>
                        <DataGridV1<UiAssociation> {...dataGridProps} />
                    </Box>
                </EmptyStateSwitch>
            </>
        );
    };
