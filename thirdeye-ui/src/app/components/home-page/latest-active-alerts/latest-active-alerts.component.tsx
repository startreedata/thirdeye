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
    Grid,
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

import { ArrowForward, CheckCircle, WarningOutlined } from "@material-ui/icons";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import {
    getAlertsAlertPath,
    getAlertsAllPath,
} from "../../../utils/routes/routes.util";
import { LatestActiveAlertsProps } from "./latest-active-alerts.interfaces";
import { useLatestActiveAlertStyles } from "./latest-active-alerts.styles";

export const LatestActiveAlerts: React.FunctionComponent<LatestActiveAlertsProps> =
    ({ alertsQuery }) => {
        const { t } = useTranslation();
        const styles = useLatestActiveAlertStyles();

        return (
            <>
                <Grid container justifyContent="space-between">
                    <Grid item>
                        <Typography className={styles.title} variant="h5">
                            {t("label.latest-active-alerts")}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <Link
                            className={styles.allAlertsLink}
                            component={RouterLink}
                            to={getAlertsAllPath()}
                        >
                            {t("label.view-all-entities", {
                                entity: t("label.alerts"),
                            })}
                            <ArrowForward className={styles.alertsLinkIcon} />
                        </Link>
                    </Grid>
                </Grid>

                <LoadingErrorStateSwitch
                    errorState={
                        <NoDataIndicator>
                            {t("message.experienced-issues-fetching-data")}
                        </NoDataIndicator>
                    }
                    isError={alertsQuery.isError}
                    isLoading={alertsQuery.isLoading}
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
                            <TableCell>{t("label.alert-name")}</TableCell>
                            <TableCell>{t("label.status")}</TableCell>
                            <TableCell>{t("label.created")}</TableCell>
                        </TitleCardTableHead>
                        <TableBody>
                            {alertsQuery?.data?.length ? (
                                alertsQuery.data
                                    ?.sort((a, b) => {
                                        return (
                                            new Date(b.created).getTime() -
                                            new Date(a.created).getTime()
                                        );
                                    })
                                    .slice(0, 5)
                                    .map((alert) => (
                                        <TableRow
                                            className={styles.tableRow}
                                            key={alert.id}
                                        >
                                            <TableCell>
                                                <Link
                                                    component={RouterLink}
                                                    to={getAlertsAlertPath(
                                                        alert.id
                                                    )}
                                                >
                                                    {alert.name}
                                                </Link>
                                            </TableCell>
                                            <TableCell>
                                                <Box
                                                    className={
                                                        styles.statusCellContainer
                                                    }
                                                >
                                                    {alert.active ? (
                                                        <CheckCircle
                                                            className={
                                                                styles.healthyIcon
                                                            }
                                                        />
                                                    ) : (
                                                        <WarningOutlined
                                                            className={
                                                                styles.unhealthyIcon
                                                            }
                                                        />
                                                    )}
                                                    {t(
                                                        alert.active
                                                            ? "label.healthy"
                                                            : "label.unhealthy"
                                                    )}
                                                </Box>
                                            </TableCell>
                                            <TableCell>
                                                {formatDateAndTimeV1(
                                                    alert.created
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    ))
                            ) : (
                                <TableRow className={styles.tableRow}>
                                    <TableCell>
                                        {t("message.no-active-alerts")}
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </TitleCardTable>
                </LoadingErrorStateSwitch>
            </>
        );
    };
