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
import { ExpandLess, ExpandMore, MoreVert } from "@material-ui/icons";
import { isEmpty } from "lodash";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getSubscriptionGroupsDetailPath,
    getSubscriptionGroupsUpdatePath,
} from "../../utils/routes-util/routes-util";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { SubscriptionGroupCardProps } from "./subscription-group-card.interfaces";
import { useSubscriptinoGroupCardStyles } from "./subscription-group-card.styles";

export const SubscriptionGroupCard: FunctionComponent<SubscriptionGroupCardProps> = (
    props: SubscriptionGroupCardProps
) => {
    const subscriptionGroupCardClasses = useSubscriptinoGroupCardStyles();
    const [expand, setExpand] = useState(false);
    const [
        subscriptionGroupOptionsAnchorElement,
        setSubscriptionGroupOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const onSubscriptionGroupOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setSubscriptionGroupOptionsAnchorElement(event.currentTarget);
    };

    const onViewSubscriptionGroupDetails = (): void => {
        history.push(
            getSubscriptionGroupsDetailPath(props.subscriptionGroup.id)
        );
    };

    const onEditSubscriptionGroup = (): void => {
        history.push(
            getSubscriptionGroupsUpdatePath(props.subscriptionGroup.id)
        );
    };

    const onDeleteSubscriptionGroup = (): void => {
        props.onDelete && props.onDelete(props.subscriptionGroup);

        closeSubscriptionGroupOptions();
    };

    const onExpandToggle = (): void => {
        setExpand((expand) => !expand);
    };

    const onViewAlertDetails = (id: number): void => {
        history.push(getAlertsDetailPath(id));
    };

    const closeSubscriptionGroupOptions = (): void => {
        setSubscriptionGroupOptionsAnchorElement(null);
    };

    return (
        <Card variant="outlined">
            {/* Subscription group name */}
            <CardHeader
                disableTypography
                action={
                    // Subscription group options button
                    <IconButton onClick={onSubscriptionGroupOptionsClick}>
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
                                    text={props.subscriptionGroup.name}
                                />
                            </Link>
                        )}
                    </>
                }
            />

            <Menu
                anchorEl={subscriptionGroupOptionsAnchorElement}
                open={Boolean(subscriptionGroupOptionsAnchorElement)}
                onClose={closeSubscriptionGroupOptions}
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

            <CardContent>
                <Grid container>
                    <Grid container item md={12}>
                        {/* Subscribed alerts */}
                        <Grid item md={6}>
                            <div className={subscriptionGroupCardClasses.label}>
                                <Typography variant="subtitle2">
                                    {t("label.subscribed-alerts")}
                                </Typography>
                            </div>

                            {/* Expand/collapse button */}
                            {props.subscriptionGroup.alerts &&
                                props.subscriptionGroup.alerts.length > 1 && (
                                    <div
                                        className={
                                            subscriptionGroupCardClasses.expandCollapseButton
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {/* Collapse */}
                                            {expand && (
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}

                                            {/* Expand */}
                                            {!expand && (
                                                <ExpandMore
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}
                                        </Link>
                                    </div>
                                )}

                            <div className={subscriptionGroupCardClasses.value}>
                                {/* No data available */}
                                {isEmpty(props.subscriptionGroup.alerts) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )}

                                {/* All subscribed alerts */}
                                {!isEmpty(props.subscriptionGroup.alerts) &&
                                    expand && (
                                        <>
                                            {props.subscriptionGroup.alerts.map(
                                                (alert, index) => (
                                                    <Link
                                                        component="button"
                                                        display="block"
                                                        key={index}
                                                        variant="body2"
                                                        onClick={(): void => {
                                                            onViewAlertDetails(
                                                                alert.id
                                                            );
                                                        }}
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            text={alert.name}
                                                        />
                                                    </Link>
                                                )
                                            )}
                                        </>
                                    )}

                                {/* First subscribed alert */}
                                {!isEmpty(props.subscriptionGroup.alerts) &&
                                    !expand && (
                                        <Link
                                            component="button"
                                            display="block"
                                            variant="body2"
                                            onClick={(): void => {
                                                onViewAlertDetails(
                                                    props.subscriptionGroup
                                                        .alerts[0].id
                                                );
                                            }}
                                        >
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                text={
                                                    props.subscriptionGroup
                                                        .alerts[0].name
                                                }
                                            />
                                        </Link>
                                    )}
                            </div>
                        </Grid>

                        {/* Subscribed emails */}
                        <Grid item md={6}>
                            <div className={subscriptionGroupCardClasses.label}>
                                <Typography variant="subtitle2">
                                    {t("label.subscribed-emails")}
                                </Typography>
                            </div>

                            {/* Expand/collapse button */}
                            {props.subscriptionGroup.emails &&
                                props.subscriptionGroup.emails.length > 1 && (
                                    <div
                                        className={
                                            subscriptionGroupCardClasses.expandCollapseButton
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {/* Collapse */}
                                            {expand && (
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}

                                            {/* Expand */}
                                            {!expand && (
                                                <ExpandMore
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}
                                        </Link>
                                    </div>
                                )}

                            <div className={subscriptionGroupCardClasses.value}>
                                {/* No data available */}
                                {isEmpty(props.subscriptionGroup.emails) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )}

                                {/* All subscribed emails */}
                                {!isEmpty(props.subscriptionGroup.emails) &&
                                    expand && (
                                        <>
                                            {props.subscriptionGroup.emails.map(
                                                (email, index) => (
                                                    <Typography
                                                        key={index}
                                                        variant="body2"
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            text={email}
                                                        />
                                                    </Typography>
                                                )
                                            )}
                                        </>
                                    )}

                                {/* First subscribed email */}
                                {!isEmpty(props.subscriptionGroup.emails) &&
                                    !expand && (
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                text={
                                                    props.subscriptionGroup
                                                        .emails[0]
                                                }
                                            />
                                        </Typography>
                                    )}
                            </div>
                        </Grid>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
