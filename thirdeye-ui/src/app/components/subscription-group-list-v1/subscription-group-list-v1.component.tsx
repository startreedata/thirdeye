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
import { Box, Button, Grid, Link, useTheme } from "@material-ui/core";
import React, {
    FunctionComponent,
    ReactElement,
    useCallback,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { PageContentsCardV1 } from "../../platform/components";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import {
    getSubscriptionGroupsUpdatePath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { subscriptionGroupChannelIconsMap } from "../subscription-group-view/notification-channels-card/notification-channels-card.utils";
import {
    SubscriptionGroupListV1Props,
    TEST_IDS,
} from "./subscription-group-list-v1.interfaces";
import {
    GridSelectionModel,
    GridColumns,
    GridRenderCellParams,
} from "@mui/x-data-grid";
import { StyledDataGrid } from "../data-grid/styled-data-grid.component";
import {
    NotificationSpec,
    SpecType,
} from "../../rest/dto/subscription-group.interfaces";

export const SubscriptionGroupListV1: FunctionComponent<SubscriptionGroupListV1Props> =
    ({ onDelete, subscriptionGroups }) => {
        const { t } = useTranslation();
        const [selectedSubscriptionGroupIds, setSelectedSubscriptionGroupIds] =
            useState<GridSelectionModel>();
        const navigate = useNavigate();
        const theme = useTheme();

        const handleSubscriptionGroupDelete = (): void => {
            if (
                !selectedSubscriptionGroupIds ||
                !selectedSubscriptionGroupIds.length
            ) {
                return;
            }

            const selectedSubscriptionGroup = subscriptionGroups?.filter(
                (subGroup) => {
                    return selectedSubscriptionGroupIds.includes(subGroup.id);
                }
            );
            onDelete &&
                selectedSubscriptionGroup &&
                onDelete(selectedSubscriptionGroup);
        };

        const handleSubscriptionGroupEdit = (): void => {
            if (!selectedSubscriptionGroupIds) {
                return;
            }
            const selectedSubscriptionGroupId =
                selectedSubscriptionGroupIds[0] as number;

            navigate(
                getSubscriptionGroupsUpdatePath(selectedSubscriptionGroupId)
            );
        };

        const isActionButtonDisable = !(
            selectedSubscriptionGroupIds &&
            selectedSubscriptionGroupIds.length === 1
        );

        const handleSubscriptionGroupViewDetailsById = (id: number): void => {
            navigate(getSubscriptionGroupsViewPath(id));
        };

        const renderLink = (params: GridRenderCellParams): ReactElement => {
            return (
                <Link
                    onClick={() =>
                        handleSubscriptionGroupViewDetailsById(params.row.id)
                    }
                >
                    {params.row.name}
                </Link>
            );
        };

        const activeChannelsRenderer = useCallback(
            (params: GridRenderCellParams) => {
                return (
                    <Box display="flex" gridGap={6} justifyContent="center">
                        {[
                            ...new Set(
                                params.row.activeChannels.map(
                                    (c: NotificationSpec) => c.type
                                )
                            ),
                        ]
                            .sort()
                            .map(
                                (iconType: unknown) =>
                                    subscriptionGroupChannelIconsMap[
                                        iconType as SpecType
                                    ]
                            )
                            .map((iconName) => (
                                <Icon
                                    color={theme.palette.primary.dark}
                                    fontSize={24}
                                    icon={iconName}
                                    key={iconName}
                                />
                            ))}
                    </Box>
                );
            },
            []
        );

        const alertsCountRenderer = useCallback(
            (params: GridRenderCellParams) => {
                return params.row.alerts.length;
            },
            []
        );

        const subscriptionGroupColumns: GridColumns = [
            {
                field: "name",
                headerName: t("label.group-name"),
                flex: 2,
                sortable: true,
                renderCell: renderLink,
            },
            {
                field: "activeChannels",
                headerName: t("label.active-channels"),
                flex: 1,
                sortable: false,
                renderCell: activeChannelsRenderer,
            },
            {
                field: "alertCount",
                headerName: t("label.subscribed-alerts"),
                flex: 1,
                sortable: true,
                renderCell: alertsCountRenderer,
            },
            {
                field: "dimensionCount",
                headerName: t("label.subscribed-dimensions"),
                flex: 1,
                sortable: true,
            },
            {
                field: "cron",
                headerName: t("label.schedule"),
                flex: 1,
            },
        ];

        return (
            <Grid item xs={12}>
                <PageContentsCardV1 disablePadding>
                    <StyledDataGrid
                        autoHeight
                        autoPageSize
                        checkboxSelection
                        disableColumnFilter
                        disableColumnSelector
                        disableSelectionOnClick
                        columns={subscriptionGroupColumns}
                        data-testid={TEST_IDS.TABLE}
                        rows={subscriptionGroups as UiSubscriptionGroup[]}
                        searchBarProps={{
                            searchKey: "name",
                            placeholder: "Search by name",
                        }}
                        selectionModel={selectedSubscriptionGroupIds}
                        toolbar={
                            <Grid container alignItems="center" spacing={2}>
                                <Grid item>
                                    <Button
                                        data-testid={TEST_IDS.EDIT_BUTTON}
                                        disabled={isActionButtonDisable}
                                        size="large"
                                        variant="contained"
                                        onClick={handleSubscriptionGroupEdit}
                                    >
                                        {t("label.edit")}
                                    </Button>
                                </Grid>

                                <Grid>
                                    <Button
                                        data-testid={TEST_IDS.DELETE_BUTTON}
                                        disabled={
                                            !selectedSubscriptionGroupIds ||
                                            selectedSubscriptionGroupIds.length ===
                                                0
                                        }
                                        size="large"
                                        variant="contained"
                                        onClick={handleSubscriptionGroupDelete}
                                    >
                                        {t("label.delete")}
                                    </Button>
                                </Grid>
                            </Grid>
                        }
                        onSelectionModelChange={setSelectedSubscriptionGroupIds}
                    />
                </PageContentsCardV1>
            </Grid>
        );
    };
