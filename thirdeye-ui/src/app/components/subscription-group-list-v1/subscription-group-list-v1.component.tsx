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
import {
    DataGridColumnV1,
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
} from "../../platform/components";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import {
    getSubscriptionGroupsUpdatePath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { getUiAssociation } from "../subscription-group-view/alert-associations-view-table/alert-associations-view-table.utils";
import { subscriptionGroupChannelIconsMap } from "../subscription-group-view/notification-channels-card/notification-channels-card.utils";
import { SubscriptionGroupListV1Props } from "./subscription-group-list-v1.interfaces";

export const SubscriptionGroupListV1: FunctionComponent<SubscriptionGroupListV1Props> =
    ({ onDelete, subscriptionGroups }) => {
        const { t } = useTranslation();
        const [selectedSubscriptionGroup, setSelectedSubscriptionGroup] =
            useState<DataGridSelectionModelV1<UiSubscriptionGroup>>();
        const navigate = useNavigate();
        const theme = useTheme();

        const handleSubscriptionGroupDelete = (): void => {
            if (
                !selectedSubscriptionGroup ||
                !selectedSubscriptionGroup.rowKeyValueMap
            ) {
                return;
            }

            onDelete &&
                onDelete(
                    Array.from(
                        selectedSubscriptionGroup.rowKeyValueMap.values()
                    )
                );
        };

        const handleSubscriptionGroupEdit = (): void => {
            if (!selectedSubscriptionGroup) {
                return;
            }
            const selectedSubscriptionGroupId = selectedSubscriptionGroup
                .rowKeyValues[0] as number;

            navigate(
                getSubscriptionGroupsUpdatePath(selectedSubscriptionGroupId)
            );
        };

        const isActionButtonDisable = !(
            selectedSubscriptionGroup &&
            selectedSubscriptionGroup.rowKeyValues.length === 1
        );

        const handleSubscriptionGroupViewDetailsById = (id: number): void => {
            navigate(getSubscriptionGroupsViewPath(id));
        };

        const renderLink = (
            cellValue: Record<string, unknown>,
            data: UiSubscriptionGroup
        ): ReactElement => {
            return (
                <Link
                    onClick={() =>
                        handleSubscriptionGroupViewDetailsById(data.id)
                    }
                >
                    {cellValue}
                </Link>
            );
        };

        const activeChannelsRenderer = useCallback(
            (_, data: UiSubscriptionGroup) => {
                return (
                    <Box display="flex" gridGap={6} justifyContent="center">
                        {[...new Set(data.activeChannels.map((c) => c.type))]
                            .sort()
                            .map(
                                (iconType) =>
                                    subscriptionGroupChannelIconsMap[iconType]
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
            (_, data: UiSubscriptionGroup) => {
                return data.alerts.length;
            },
            []
        );

        const alertsCountTooltipRenderer = useCallback(
            (_, data: UiSubscriptionGroup) => {
                return data.alerts
                    .map((a) => a.name)
                    .reduce(
                        (sum, val) => (
                            <>
                                {sum}
                                {val}
                                <br />
                            </>
                        ),
                        <></>
                    );
            },
            []
        );

        const dimensionCountTooltipRenderer = useCallback(
            (_, data: UiSubscriptionGroup) => {
                return getUiAssociation(data.alerts || [], t)
                    .filter((a) => a.enumerationId)
                    .map((a) => a.enumerationName)
                    .reduce(
                        (sum, val) => (
                            <>
                                {sum}
                                {val}
                                <br />
                            </>
                        ),
                        <></>
                    );
            },
            []
        );

        const subscriptionGroupColumns: DataGridColumnV1<UiSubscriptionGroup>[] =
            [
                {
                    key: "name",
                    dataKey: "name",
                    header: t("label.group-name"),
                    minWidth: 0,
                    flex: 1.5,
                    sortable: true,
                    customCellRenderer: renderLink,
                },
                {
                    key: "activeChannels",
                    dataKey: "activeChannels",
                    header: t("label.active-channels"),
                    minWidth: 0,
                    flex: 1,
                    sortable: true,
                    cellTooltip: false,
                    customCellRenderer: activeChannelsRenderer,
                },
                {
                    key: "alertCount",
                    dataKey: "alertCount",
                    header: t("label.subscribed-alerts"),
                    minWidth: 0,
                    flex: 1,
                    sortable: true,
                    customCellRenderer: alertsCountRenderer,
                    customCellTooltipRenderer: alertsCountTooltipRenderer,
                },
                {
                    key: "dimensionCount",
                    dataKey: "dimensionCount",
                    header: t("label.subscribed-dimensions"),
                    minWidth: 0,
                    flex: 1,
                    sortable: true,
                    customCellTooltipRenderer: dimensionCountTooltipRenderer,
                },
                {
                    key: "cron",
                    dataKey: "cron",
                    header: t("label.schedule"),
                    minWidth: 0,
                    flex: 1,
                },
            ];

        return (
            <Grid item xs={12}>
                <PageContentsCardV1 disablePadding fullHeight>
                    <DataGridV1<UiSubscriptionGroup>
                        hideBorder
                        columns={subscriptionGroupColumns}
                        data={subscriptionGroups as UiSubscriptionGroup[]}
                        rowKey="id"
                        scroll={DataGridScrollV1.Contents}
                        searchPlaceholder={t("label.search-entity", {
                            entity: t("label.subscription-groups"),
                        })}
                        toolbarComponent={
                            <Grid container alignItems="center" spacing={2}>
                                <Grid item>
                                    <Button
                                        disabled={isActionButtonDisable}
                                        variant="contained"
                                        onClick={handleSubscriptionGroupEdit}
                                    >
                                        {t("label.edit")}
                                    </Button>
                                </Grid>

                                <Grid>
                                    <Button
                                        disabled={
                                            !selectedSubscriptionGroup ||
                                            selectedSubscriptionGroup
                                                .rowKeyValues.length === 0
                                        }
                                        variant="contained"
                                        onClick={handleSubscriptionGroupDelete}
                                    >
                                        {t("label.delete")}
                                    </Button>
                                </Grid>
                            </Grid>
                        }
                        onSelectionChange={setSelectedSubscriptionGroup}
                    />
                </PageContentsCardV1>
            </Grid>
        );
    };
