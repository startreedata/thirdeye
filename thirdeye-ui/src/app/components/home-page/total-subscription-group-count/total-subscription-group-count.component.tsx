import { Box, Grid, Link, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { getSubscriptionGroupsAllPath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TotalSubscriptionGroupCountProps } from "./total-subscription-group-count.interfaces";

export const TotalSubscriptionGroupCount: FunctionComponent<TotalSubscriptionGroupCountProps> =
    ({ subscriptionGroups, getSubscriptionGroupsStatus }) => {
        const { t } = useTranslation();

        return (
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>
                    <Box>{t("label.subscriptions")}</Box>
                    <Box>
                        <Link
                            component={RouterLink}
                            to={getSubscriptionGroupsAllPath()}
                        >
                            {t("label.review-entities", {
                                entity: t("label.subscriptions"),
                            })}
                        </Link>
                    </Box>
                </Grid>
                <Grid item>
                    <LoadingErrorStateSwitch
                        errorState={
                            <NoDataIndicator>
                                {t("message.experienced-issues-fetching-data")}
                            </NoDataIndicator>
                        }
                        isError={
                            getSubscriptionGroupsStatus === ActionStatus.Error
                        }
                        isLoading={
                            getSubscriptionGroupsStatus === ActionStatus.Working
                        }
                        loadingState={
                            <Box width={100}>
                                <Typography variant="h2">
                                    <SkeletonV1 animation="pulse" />
                                </Typography>
                            </Box>
                        }
                    >
                        <Typography variant="h2">
                            {subscriptionGroups && subscriptionGroups.length}
                        </Typography>
                    </LoadingErrorStateSwitch>
                </Grid>
            </Grid>
        );
    };
