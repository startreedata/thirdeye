/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Card,
    CardContent,
    CardHeader,
    Grid,
    IconButton,
    Link,
    Menu,
    MenuItem,
} from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { UiSubscriptionGroupAlert } from "../../../rest/dto/ui-subscription-group.interfaces";
import {
    getAlertsAlertPath,
    getSubscriptionGroupsUpdatePath,
    getSubscriptionGroupsViewPath,
} from "../../../utils/routes/routes.util";
import { getUiSubscriptionGroupAlertName } from "../../../utils/subscription-groups/subscription-groups.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { SubscriptionGroupCardProps } from "./subscription-group-card.interfaces";

export const SubscriptionGroupCard: FunctionComponent<
    SubscriptionGroupCardProps
> = (props: SubscriptionGroupCardProps) => {
    const [
        subscriptionGroupOptionsAnchorElement,
        setSubscriptionGroupOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleSubscriptionGroupOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setSubscriptionGroupOptionsAnchorElement(event.currentTarget);
    };

    const handleSubscriptionGroupOptionsClose = (): void => {
        setSubscriptionGroupOptionsAnchorElement(null);
    };

    const handleSubscriptionGroupViewDetails = (): void => {
        if (!props.uiSubscriptionGroup) {
            return;
        }

        navigate(getSubscriptionGroupsViewPath(props.uiSubscriptionGroup.id));
        handleSubscriptionGroupOptionsClose();
    };

    const handleSubscriptionGroupEdit = (): void => {
        if (!props.uiSubscriptionGroup) {
            return;
        }

        navigate(getSubscriptionGroupsUpdatePath(props.uiSubscriptionGroup.id));
        handleSubscriptionGroupOptionsClose();
    };

    const handleSubscriptionGroupDelete = (): void => {
        if (!props.uiSubscriptionGroup) {
            return;
        }

        props.onDelete && props.onDelete(props.uiSubscriptionGroup);
        handleSubscriptionGroupOptionsClose();
    };

    const handleAlertViewDetails = (
        uiSubscriptionGroupAlert: UiSubscriptionGroupAlert
    ): void => {
        if (!uiSubscriptionGroupAlert) {
            return;
        }

        navigate(getAlertsAlertPath(uiSubscriptionGroupAlert.id));
    };

    return (
        <Card variant="outlined">
            {props.uiSubscriptionGroup && (
                <CardHeader
                    action={
                        <>
                            {/* Subscription group options button */}
                            <IconButton
                                color="secondary"
                                onClick={handleSubscriptionGroupOptionsClick}
                            >
                                <MoreVertIcon />
                            </IconButton>

                            {/* Subscription group options */}
                            <Menu
                                anchorEl={subscriptionGroupOptionsAnchorElement}
                                open={Boolean(
                                    subscriptionGroupOptionsAnchorElement
                                )}
                                onClose={handleSubscriptionGroupOptionsClose}
                            >
                                {/* View details */}
                                {props.showViewDetails && (
                                    <MenuItem
                                        onClick={
                                            handleSubscriptionGroupViewDetails
                                        }
                                    >
                                        {t("label.view-details")}
                                    </MenuItem>
                                )}

                                {/* Edit subscription group */}
                                <MenuItem onClick={handleSubscriptionGroupEdit}>
                                    {t("label.edit-entity", {
                                        entity: t("label.subscription-group"),
                                    })}
                                </MenuItem>

                                {/* Delete subscription group */}
                                <MenuItem
                                    onClick={handleSubscriptionGroupDelete}
                                >
                                    {t("label.delete-entity", {
                                        entity: t("label.subscription-group"),
                                    })}
                                </MenuItem>
                            </Menu>
                        </>
                    }
                    title={
                        <>
                            {/* Subscription group name */}
                            {props.showViewDetails && (
                                <Link
                                    onClick={handleSubscriptionGroupViewDetails}
                                >
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.uiSubscriptionGroup.name}
                                    />
                                </Link>
                            )}

                            {/* Summary */}
                            {!props.showViewDetails && t("label.summary")}
                        </>
                    }
                    titleTypographyProps={{ variant: "h6" }}
                />
            )}

            <CardContent>
                {props.uiSubscriptionGroup && (
                    <Grid container>
                        {/* Schedule(or Cron) */}
                        <Grid item sm={6} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.schedule")}
                                searchWords={props.searchWords}
                                values={[props.uiSubscriptionGroup.cron]}
                            />
                        </Grid>

                        {/* Subscribed alerts */}
                        <Grid item sm={6} xs={12}>
                            <NameValueDisplayCard<UiSubscriptionGroupAlert>
                                link
                                name={t("label.subscribed-alerts")}
                                searchWords={props.searchWords}
                                valueRenderer={getUiSubscriptionGroupAlertName}
                                values={props.uiSubscriptionGroup.alerts}
                                onClick={handleAlertViewDetails}
                            />
                        </Grid>

                        {/* Subscribed emails */}
                        {props.uiSubscriptionGroup.emails.length > 0 && (
                            <Grid item sm={6} xs={12}>
                                <NameValueDisplayCard<string>
                                    name={t("label.subscribed-emails")}
                                    searchWords={props.searchWords}
                                    values={props.uiSubscriptionGroup.emails}
                                />
                            </Grid>
                        )}
                    </Grid>
                )}

                {/* No data available */}
                {!props.uiSubscriptionGroup && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
