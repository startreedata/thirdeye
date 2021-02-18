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
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getSubscriptionGroupsDetailPath,
    getSubscriptionGroupsUpdatePath,
} from "../../../utils/routes/routes.util";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardProps,
} from "./subscription-group-card.interfaces";

export const SubscriptionGroupCard: FunctionComponent<SubscriptionGroupCardProps> = (
    props: SubscriptionGroupCardProps
) => {
    const [
        subscriptionGroupOptionsAnchorElement,
        setSubscriptionGroupOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
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
        history.push(
            getSubscriptionGroupsDetailPath(props.subscriptionGroupCardData.id)
        );
        handleSubscriptionGroupOptionsClose();
    };

    const handleSubscriptionGroupEdit = (): void => {
        history.push(
            getSubscriptionGroupsUpdatePath(props.subscriptionGroupCardData.id)
        );
        handleSubscriptionGroupOptionsClose();
    };

    const handleSubscriptionGroupDelete = (): void => {
        props.onDelete && props.onDelete(props.subscriptionGroupCardData);
        handleSubscriptionGroupOptionsClose();
    };

    const handleAlertViewDetails = (alert: SubscriptionGroupAlert): void => {
        if (!alert) {
            return;
        }

        history.push(getAlertsDetailPath(alert.id));
    };

    const getSubscriptionGroupAlertName = (
        subscriptionGroupAlert: SubscriptionGroupAlert
    ): string => {
        if (!subscriptionGroupAlert) {
            return "";
        }

        return subscriptionGroupAlert.name;
    };

    return (
        <Card variant="outlined">
            {props.subscriptionGroupCardData && (
                <CardHeader
                    action={
                        <>
                            {/* Subscription group options button */}
                            <IconButton
                                onClick={handleSubscriptionGroupOptionsClick}
                            >
                                <MoreVertIcon />
                            </IconButton>

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
                                    {t("label.edit-subscription-group")}
                                </MenuItem>

                                {/* Delete subscription group */}
                                <MenuItem
                                    onClick={handleSubscriptionGroupDelete}
                                >
                                    {t("label.delete-subscription-group")}
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
                                        text={
                                            props.subscriptionGroupCardData.name
                                        }
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
                <Grid container>
                    {/* Subscribed alerts */}
                    <Grid item sm={6} xs={12}>
                        <NameValueDisplayCard<SubscriptionGroupAlert>
                            link
                            name={t("subscribed-alerts")}
                            searchWords={props.searchWords}
                            valueTextFn={getSubscriptionGroupAlertName}
                            values={props.subscriptionGroupCardData.alerts}
                            onClick={handleAlertViewDetails}
                        />
                    </Grid>

                    {/* Subscribed emails */}
                    <Grid item sm={6} xs={12}>
                        <NameValueDisplayCard<string>
                            name={t("subscribed-emails")}
                            searchWords={props.searchWords}
                            values={props.subscriptionGroupCardData.emails}
                        />
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
