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
    Button,
    Card,
    CardContent,
    Grid,
    Link,
    Typography,
} from "@material-ui/core";
import AddCircleOutlineIcon from "@material-ui/icons/AddCircleOutline";
import BarChartIcon from "@material-ui/icons/BarChart";
import OpenInNewIcon from "@material-ui/icons/OpenInNew";
import React, { FunctionComponent, ReactElement } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import {
    DataGridScrollV1,
    DataGridV1,
    PageContentsCardV1,
} from "../../platform/components";
import { formatDateAndTimeV1 } from "../../platform/utils";
import { ActionStatus } from "../../rest/actions.interfaces";
import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";
import { INVESTIGATION_ID_QUERY_PARAM } from "../../utils/investigation/investigation.util";
import { getRootCauseAnalysisForAnomalyInvestigatePath } from "../../utils/routes/routes.util";
import { EmptyStateSwitch } from "../page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { useInvestigationListStyles } from "./investigation-list.styles";
import { InvestigationsListProps } from "./investigations-list.interfaces";

export const InvestigationsList: FunctionComponent<InvestigationsListProps> = ({
    investigations,
    getInvestigationsRequestStatus,
    anomalyId,
}) => {
    const classes = useInvestigationListStyles();
    const { t } = useTranslation();

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: Investigation
    ): ReactElement => {
        if (data.anomaly) {
            const searchParams = new URLSearchParams(
                data.uiMetadata[SavedStateKeys.QUERY_SEARCH_STRING]
            );
            searchParams.set(INVESTIGATION_ID_QUERY_PARAM, data.id.toString());
            const url = `${getRootCauseAnalysisForAnomalyInvestigatePath(
                data.anomaly.id as number
            )}?${searchParams.toString()}`;

            return (
                <Link href={url} target="_blank">
                    <Grid container alignItems="center">
                        <Grid item>{cellValue}</Grid>
                        <Grid item>
                            <OpenInNewIcon
                                className={classes.iconAlignVertical}
                                fontSize="small"
                            />
                        </Grid>
                    </Grid>
                </Link>
            );
        }

        /**
         * In the rare event that anomaly data is missing in the Investigation
         * object, just render the name
         */
        return <span>{cellValue}</span>;
    };

    const investigationsColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 0,
            flex: 1.5,
            sortable: true,
            customCellRenderer: renderLink,
            // eslint-disable-next-line react/display-name
            customCellTooltipRenderer: () => (
                <span>{t("label.view-investigation")}</span>
            ),
        },
        {
            key: "createdBy.principal",
            dataKey: "createdBy.principal",
            header: t("label.created-by"),
            cellTooltip: false,
            minWidth: 0,
            flex: 1,
            sortable: true,
        },
        {
            key: "created",
            dataKey: "created",
            header: t("label.created"),
            cellTooltip: false,
            minWidth: 0,
            flex: 1,
            sortable: true,
            customCellRenderer: (value: Record<string, unknown>) =>
                formatDateAndTimeV1(Number(value)),
        },
        {
            key: "updated",
            dataKey: "updated",
            header: t("label.last-updated"),
            cellTooltip: false,
            minWidth: 0,
            flex: 1,
            sortable: true,
            customCellRenderer: (value: Record<string, unknown>) =>
                formatDateAndTimeV1(Number(value)),
        },
    ];

    return (
        <LoadingErrorStateSwitch
            wrapInCard
            isError={getInvestigationsRequestStatus === ActionStatus.Error}
            isLoading={
                getInvestigationsRequestStatus === ActionStatus.Working ||
                getInvestigationsRequestStatus === ActionStatus.Initial
            }
        >
            <PageContentsCardV1 disablePadding fullHeight>
                <EmptyStateSwitch
                    emptyState={
                        <CardContent>
                            <Box marginBottom={2}>
                                <Typography variant="h6">
                                    {t("label.saved-investigations-anomaly")}
                                </Typography>
                            </Box>
                            <Box>
                                <Card variant="outlined">
                                    <Box pb={3} pt={3} textAlign="center">
                                        <Box>
                                            <BarChartIcon
                                                color="primary"
                                                fontSize="large"
                                            />
                                        </Box>
                                        <Typography variant="h6">
                                            {t(
                                                "message.no-saved-investigations"
                                            )}
                                        </Typography>
                                        <Box pt={1}>
                                            <Button
                                                color="primary"
                                                component={RouterLink}
                                                startIcon={
                                                    <AddCircleOutlineIcon />
                                                }
                                                to={`${getRootCauseAnalysisForAnomalyInvestigatePath(
                                                    anomalyId
                                                )}`}
                                                variant="contained"
                                            >
                                                {t("label.investigate-entity", {
                                                    entity: t("label.anomaly"),
                                                })}
                                            </Button>
                                        </Box>
                                    </Box>
                                </Card>
                            </Box>
                        </CardContent>
                    }
                    isEmpty={!!investigations && investigations.length === 0}
                >
                    <DataGridV1<Investigation>
                        disableSearch
                        disableSelection
                        hideBorder
                        columns={investigationsColumns}
                        data={investigations as Investigation[]}
                        rowKey="id"
                        scroll={DataGridScrollV1.Body}
                        toolbarComponent={
                            <Grid
                                container
                                alignItems="center"
                                justifyContent="space-between"
                            >
                                <Grid item>
                                    <Typography variant="h6">
                                        {t(
                                            "label.saved-investigations-anomaly"
                                        )}
                                    </Typography>
                                </Grid>
                                <Grid item>
                                    <Button
                                        color="primary"
                                        component={RouterLink}
                                        to={`${getRootCauseAnalysisForAnomalyInvestigatePath(
                                            anomalyId
                                        )}`}
                                        variant="contained"
                                    >
                                        {t("label.investigate-entity", {
                                            entity: t("label.anomaly"),
                                        })}
                                    </Button>
                                </Grid>
                            </Grid>
                        }
                    />
                </EmptyStateSwitch>
            </PageContentsCardV1>
        </LoadingErrorStateSwitch>
    );
};
