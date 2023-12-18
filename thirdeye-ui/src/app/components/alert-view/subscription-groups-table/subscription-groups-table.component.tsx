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
import { Box, Button, Link, Typography, useTheme } from "@material-ui/core";
import React, {
    FunctionComponent,
    ReactElement,
    useCallback,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1 } from "../../../platform/components";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { useQuery } from "@tanstack/react-query";
import {
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsCreatePath,
    getSubscriptionGroupsViewPath,
} from "../../../utils/routes/routes.util";
import {
    GridColumns,
    GridRenderCellParams,
    GridSortModel,
} from "@mui/x-data-grid";
import { StyledDataGrid } from "../../data-grid/styled-data-grid.component";
import { SubscriptionGroupsTableProps } from "./subscription-groups-table.interfaces";
import {
    AlertAssociation,
    NotificationSpec,
    SpecType,
    SubscriptionGroup,
} from "../../../rest/dto/subscription-group.interfaces";
import { AxiosError } from "axios/index";
import { getAllSubscriptionGroups } from "../../../rest/subscription-groups/subscription-groups.rest";
import { useNavigate } from "react-router-dom";
import { subscriptionGroupChannelIconsMap } from "../../subscription-group-view/notification-channels-card/notification-channels-card.utils";
import { Icon } from "@iconify/react";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";

export const SubscriptionGroupsTable: FunctionComponent<SubscriptionGroupsTableProps> =
    ({ alertId, headerName }) => {
        const { t } = useTranslation();
        const navigate = useNavigate();
        const theme = useTheme();
        const [sortModel, setSortModel] = useState<GridSortModel>([
            {
                field: "name",
                sort: "asc",
            },
        ]);

        const {
            data: subscriptionGroups,
            isInitialLoading,
            isError,
        } = useQuery<SubscriptionGroup[], AxiosError>({
            queryKey: ["subscriptiongroups"],
            queryFn: () => {
                return getAllSubscriptionGroups();
            },
        });

        const subscriptionGroupsForAlert = useMemo(() => {
            if (!subscriptionGroups) {
                return [];
            }

            return subscriptionGroups.filter((subscriptionGroup) => {
                return subscriptionGroup.alertAssociations?.some(
                    (association) => {
                        return association.alert.id === alertId;
                    }
                );
            });
        }, [subscriptionGroups]);

        const renderLink = (params: GridRenderCellParams): ReactElement => {
            return (
                <Link
                    onClick={() =>
                        navigate(getSubscriptionGroupsViewPath(params.row.id))
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
                                params.row.specs.map(
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

        const dimensionCountRenderer = (
            params: GridRenderCellParams
        ): ReactElement => {
            const dimensionsSubscribedToForAlert =
                params.row.alertAssociations?.filter(
                    (association: AlertAssociation) => {
                        return association.alert.id === alertId;
                    }
                );

            if (dimensionsSubscribedToForAlert.length === 1) {
                if (
                    dimensionsSubscribedToForAlert[0].enumerationItem ===
                    undefined
                ) {
                    return t("label.whole-alert");
                }
            }

            return dimensionsSubscribedToForAlert.length;
        };

        const columns: GridColumns = [
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
                field: "dimensionCount",
                headerName: t("label.subscribed-dimensions-for-this-alert"),
                flex: 2,
                renderCell: dimensionCountRenderer,
            },
            {
                field: "cron",
                headerName: t("label.schedule"),
                flex: 1,
            },
        ];

        /**
         * if statement to prevent the infinite loop by confirming model is
         * different than the current sortModel state
         */
        const handleSortChange = (model: GridSortModel): void => {
            if (JSON.stringify(model) !== JSON.stringify(sortModel)) {
                setSortModel(model);
            }
        };

        return (
            <>
                <Typography variant="h5">{headerName}</Typography>
                <Box paddingTop={2} />
                <LoadingErrorStateSwitch
                    errorState={
                        <Box
                            alignItems="center"
                            display="flex"
                            height="100%"
                            justifyContent="center"
                        >
                            <Box>
                                <NoDataIndicator
                                    text={t(
                                        "message.experienced-issues-fetching-data"
                                    )}
                                />
                            </Box>
                        </Box>
                    }
                    isError={isError}
                    isLoading={isInitialLoading}
                    loadingState={
                        <>
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                        </>
                    }
                >
                    <EmptyStateSwitch
                        emptyState={
                            <Box pb={5} pt={5}>
                                <Box textAlign="center">
                                    <Typography variant="h6">
                                        {t(
                                            "message.no-subscription-groups-for-alert"
                                        )}
                                    </Typography>
                                </Box>
                                <Box marginTop={4} textAlign="center">
                                    <Button
                                        color="primary"
                                        href={getSubscriptionGroupsCreatePath()}
                                    >
                                        {t("label.create-entity", {
                                            entity: t(
                                                "label.subscription-group"
                                            ),
                                        })}
                                    </Button>
                                </Box>
                                {subscriptionGroups &&
                                    subscriptionGroups.length > 0 && (
                                        <>
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
                                                or
                                            </Box>
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
                                                Configure existing subscription
                                                groups to notify on alert
                                            </Box>
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
                                                <Button
                                                    color="primary"
                                                    href={getSubscriptionGroupsAllPath()}
                                                >
                                                    {t("label.view-entity", {
                                                        entity: t(
                                                            "label.subscription-groups"
                                                        ),
                                                    })}
                                                </Button>
                                            </Box>
                                        </>
                                    )}
                            </Box>
                        }
                        isEmpty={subscriptionGroupsForAlert.length === 0}
                    >
                        <Box width="100%">
                            <StyledDataGrid
                                autoHeight
                                disableColumnFilter
                                disableColumnSelector
                                disableSelectionOnClick
                                columns={columns}
                                pageSize={10}
                                rows={
                                    subscriptionGroupsForAlert as SubscriptionGroup[]
                                }
                                sortModel={sortModel}
                                onSortModelChange={handleSortChange}
                            />
                        </Box>
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </>
        );
    };
