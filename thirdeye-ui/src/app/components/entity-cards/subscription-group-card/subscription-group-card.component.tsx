import {
    Card,
    CardContent,
    CardHeader,
    Grid,
    IconButton,
    Link,
    Menu,
    MenuItem,
    Typography,
} from "@material-ui/core";
import { MoreVert } from "@material-ui/icons";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getSubscriptionGroupsDetailPath,
    getSubscriptionGroupsUpdatePath,
} from "../../../utils/routes-util/routes-util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { ExpandableDetails } from "../expandable-details/expandable-details.components";
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
    const [expand, setExpand] = useState(false);
    const history = useHistory();
    const { t } = useTranslation();

    const onSubscriptionGroupOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setSubscriptionGroupOptionsAnchorElement(event.currentTarget);
    };

    const onCloseSubscriptionGroupOptions = (): void => {
        setSubscriptionGroupOptionsAnchorElement(null);
    };

    const onViewSubscriptionGroupDetails = (): void => {
        history.push(
            getSubscriptionGroupsDetailPath(props.subscriptionGroupCardData.id)
        );

        onCloseSubscriptionGroupOptions();
    };

    const onEditSubscriptionGroup = (): void => {
        history.push(
            getSubscriptionGroupsUpdatePath(props.subscriptionGroupCardData.id)
        );

        onCloseSubscriptionGroupOptions();
    };

    const onDeleteSubscriptionGroup = (): void => {
        props.onDelete && props.onDelete(props.subscriptionGroupCardData);

        onCloseSubscriptionGroupOptions();
    };

    const onViewAlertDetails = (alert: SubscriptionGroupAlert): void => {
        if (!alert) {
            return;
        }

        history.push(getAlertsDetailPath(alert.id));
    };

    const getSubscribedAlertValue = (value: SubscriptionGroupAlert): string => {
        if (!value) {
            return "";
        }

        return value.name;
    };

    return (
        <Card variant="outlined">
            {props.subscriptionGroupCardData && (
                <>
                    <CardHeader
                        disableTypography
                        action={
                            // Subscription group options button
                            <IconButton
                                onClick={onSubscriptionGroupOptionsClick}
                            >
                                <MoreVert />
                            </IconButton>
                        }
                        title={
                            <>
                                {/* Summary */}
                                {props.hideViewDetailsLinks && (
                                    <Typography variant="h6">
                                        {t("label.summary")}
                                    </Typography>
                                )}

                                {/* Subscription group name */}
                                {!props.hideViewDetailsLinks && (
                                    <Link
                                        component="button"
                                        variant="h6"
                                        onClick={onViewSubscriptionGroupDetails}
                                    >
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={
                                                props.subscriptionGroupCardData
                                                    .name
                                            }
                                        />
                                    </Link>
                                )}
                            </>
                        }
                    />
                    <Menu
                        anchorEl={subscriptionGroupOptionsAnchorElement}
                        open={Boolean(subscriptionGroupOptionsAnchorElement)}
                        onClose={onCloseSubscriptionGroupOptions}
                    >
                        {/* View details */}
                        {!props.hideViewDetailsLinks && (
                            <MenuItem onClick={onViewSubscriptionGroupDetails}>
                                {t("label.view-details")}
                            </MenuItem>
                        )}

                        {/* Edit subscription group */}
                        <MenuItem onClick={onEditSubscriptionGroup}>
                            {t("label.edit-subscription-group")}
                        </MenuItem>

                        {/* Delete subscription group */}
                        <MenuItem onClick={onDeleteSubscriptionGroup}>
                            {t("label.delete-subscription-group")}
                        </MenuItem>
                    </Menu>
                </>
            )}

            <CardContent>
                {props.subscriptionGroupCardData && (
                    <Grid container>
                        <Grid container item sm={12}>
                            {/* Subscribed alerts */}
                            <Grid item sm={6}>
                                <ExpandableDetails<SubscriptionGroupAlert>
                                    link
                                    expand={expand}
                                    label={t("label.subscribed-alerts")}
                                    searchWords={props.searchWords}
                                    valueTextFn={getSubscribedAlertValue}
                                    values={
                                        props.subscriptionGroupCardData.alerts
                                    }
                                    onChange={setExpand}
                                    onLinkClick={onViewAlertDetails}
                                />
                            </Grid>

                            {/* Subscribed emails */}
                            <Grid item sm={6}>
                                <ExpandableDetails<string>
                                    expand={expand}
                                    label={t("label.subscribed-emails")}
                                    searchWords={props.searchWords}
                                    valueTextFn={(value: string): string => {
                                        return value || "";
                                    }}
                                    values={
                                        props.subscriptionGroupCardData.emails
                                    }
                                    onChange={setExpand}
                                />
                            </Grid>
                        </Grid>
                    </Grid>
                )}

                {/* No data available message */}
                {!props.subscriptionGroupCardData && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
