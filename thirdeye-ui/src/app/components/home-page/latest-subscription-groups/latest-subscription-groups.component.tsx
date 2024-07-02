/*
 * Copyright 2024 StarTree Inc
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
    Link,
    TableBody,
    TableCell,
    TableRow,
    Typography,
} from "@material-ui/core";
import * as React from "react";
import { useTranslation } from "react-i18next";
// eslint-disable-next-line no-restricted-imports
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    TitleCardTable,
    TitleCardTableHead,
} from "../title-card-table/title-card-table.component";

import {
    ArrowForward,
    KeyboardArrowDown,
    KeyboardArrowUp,
} from "@material-ui/icons";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import {
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsViewPath,
} from "../../../utils/routes/routes.util";
import {
    LatestSubscriptionChangeCreatedSort,
    LatestSubscriptionGroupsProps,
} from "./latest-subscription-groups.interfaces";
import { useLatestSubscriptionGroupsStyles } from "./latest-subscription-groups.styles";
import { SortableTableHeader } from "../../rca/top-contributors-table/top-contributors-table.component";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";

export const LatestSubscriptionGroups: React.FunctionComponent<LatestSubscriptionGroupsProps> =
    ({ subscriptionGroupsQuery }) => {
        const { t } = useTranslation();
        const styles = useLatestSubscriptionGroupsStyles();
        const [changeCreatedSort, setChangeCreatedSort] =
            React.useState<LatestSubscriptionChangeCreatedSort | null>(null);

        const toggleSort = (): void => {
            if (changeCreatedSort === LatestSubscriptionChangeCreatedSort.ASC) {
                setChangeCreatedSort(LatestSubscriptionChangeCreatedSort.DESC);
            } else {
                setChangeCreatedSort(LatestSubscriptionChangeCreatedSort.ASC);
            }
        };

        const sortFunction = (
            a: SubscriptionGroup,
            b: SubscriptionGroup
        ): number => {
            if (changeCreatedSort === LatestSubscriptionChangeCreatedSort.ASC) {
                return (
                    new Date(a.created).getTime() -
                    new Date(b.created).getTime()
                );
            }

            return (
                new Date(b.created).getTime() - new Date(a.created).getTime()
            );
        };

        return (
            <>
                <Typography className={styles.title} variant="h5">
                    {t("label.latest-entity", {
                        entity: t("label.subscription-groups"),
                    })}
                </Typography>
                <LoadingErrorStateSwitch
                    errorState={
                        <NoDataIndicator>
                            {t("message.experienced-issues-fetching-data")}
                        </NoDataIndicator>
                    }
                    isError={subscriptionGroupsQuery.isError}
                    isLoading={subscriptionGroupsQuery.isLoading}
                    loadingState={
                        <Box height={311} width="100%">
                            <SkeletonV1 animation="pulse" height={31} />
                            <SkeletonV1 animation="pulse" height={31} />
                            <SkeletonV1 animation="pulse" height={31} />
                            <SkeletonV1 animation="pulse" height={31} />
                            <SkeletonV1 animation="pulse" height={31} />
                        </Box>
                    }
                >
                    <TitleCardTable>
                        <TitleCardTableHead>
                            <TableCell>
                                {t("label.subscription-name")}
                            </TableCell>
                            <SortableTableHeader onClick={toggleSort}>
                                <strong>{t("label.created")}</strong>
                                {changeCreatedSort === null ? (
                                    <></>
                                ) : changeCreatedSort ===
                                  LatestSubscriptionChangeCreatedSort.ASC ? (
                                    <KeyboardArrowUp />
                                ) : (
                                    <KeyboardArrowDown />
                                )}
                            </SortableTableHeader>
                        </TitleCardTableHead>
                        <TableBody>
                            {subscriptionGroupsQuery?.data?.length ? (
                                subscriptionGroupsQuery.data
                                    .sort(sortFunction)
                                    .slice(0, 5)
                                    .map((subscriptionGroup) => (
                                        <TableRow
                                            className={styles.tableRow}
                                            key={subscriptionGroup.id}
                                        >
                                            <TableCell>
                                                <Link
                                                    component={RouterLink}
                                                    to={getSubscriptionGroupsViewPath(
                                                        subscriptionGroup.id
                                                    )}
                                                >
                                                    {subscriptionGroup.name}
                                                </Link>
                                            </TableCell>
                                            <TableCell>
                                                {formatDateAndTimeV1(
                                                    subscriptionGroup.created
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    ))
                            ) : (
                                <TableRow className={styles.tableRow}>
                                    <TableCell>
                                        {t("message.no-subscription-groups")}
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </TitleCardTable>
                </LoadingErrorStateSwitch>
                <Link
                    className={styles.allAlertsLink}
                    component={RouterLink}
                    to={getSubscriptionGroupsAllPath()}
                >
                    {t("label.show-entities", {
                        entity: t("label.subscriptions"),
                    })}
                    <ArrowForward className={styles.alertsLinkIcon} />
                </Link>
            </>
        );
    };
