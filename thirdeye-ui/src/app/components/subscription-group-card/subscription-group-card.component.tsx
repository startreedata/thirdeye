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
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
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

    const onExpandToggle = (): void => {
        setExpand((expand) => !expand);
    };

    const onViewAlertDetails = (id: number): void => {
        history.push(getAlertsDetailPath(id));
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
                        <Grid container item md={12}>
                            {/* Subscribed alerts */}
                            <Grid item md={6}>
                                <div
                                    className={
                                        subscriptionGroupCardClasses.label
                                    }
                                >
                                    <Typography variant="subtitle2">
                                        {t("label.subscribed-alerts")}
                                    </Typography>
                                </div>

                                {/* Expand/collapse button */}
                                {props.subscriptionGroupCardData.alerts &&
                                    props.subscriptionGroupCardData.alerts
                                        .length > 1 && (
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

                                <div
                                    className={
                                        subscriptionGroupCardClasses.value
                                    }
                                >
                                    {/* No data available */}
                                    {isEmpty(
                                        props.subscriptionGroupCardData.alerts
                                    ) && (
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
                                    {!isEmpty(
                                        props.subscriptionGroupCardData.alerts
                                    ) &&
                                        expand && (
                                            <>
                                                {props.subscriptionGroupCardData.alerts.map(
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
                                                                text={
                                                                    alert.name
                                                                }
                                                            />
                                                        </Link>
                                                    )
                                                )}
                                            </>
                                        )}

                                    {/* First subscribed alert */}
                                    {!isEmpty(
                                        props.subscriptionGroupCardData.alerts
                                    ) &&
                                        !expand && (
                                            <Link
                                                component="button"
                                                display="block"
                                                variant="body2"
                                                onClick={(): void => {
                                                    onViewAlertDetails(
                                                        props
                                                            .subscriptionGroupCardData
                                                            .alerts[0].id
                                                    );
                                                }}
                                            >
                                                <TextHighlighter
                                                    searchWords={
                                                        props.searchWords
                                                    }
                                                    text={
                                                        props
                                                            .subscriptionGroupCardData
                                                            .alerts[0].name
                                                    }
                                                />
                                            </Link>
                                        )}
                                </div>
                            </Grid>

                            {/* Subscribed emails */}
                            <Grid item md={6}>
                                <div
                                    className={
                                        subscriptionGroupCardClasses.label
                                    }
                                >
                                    <Typography variant="subtitle2">
                                        {t("label.subscribed-emails")}
                                    </Typography>
                                </div>

                                {/* Expand/collapse button */}
                                {props.subscriptionGroupCardData.emails &&
                                    props.subscriptionGroupCardData.emails
                                        .length > 1 && (
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

                                <div
                                    className={
                                        subscriptionGroupCardClasses.value
                                    }
                                >
                                    {/* No data available */}
                                    {isEmpty(
                                        props.subscriptionGroupCardData.emails
                                    ) && (
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
                                    {!isEmpty(
                                        props.subscriptionGroupCardData.emails
                                    ) &&
                                        expand && (
                                            <>
                                                {props.subscriptionGroupCardData.emails.map(
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
                                    {!isEmpty(
                                        props.subscriptionGroupCardData.emails
                                    ) &&
                                        !expand && (
                                            <Typography variant="body2">
                                                <TextHighlighter
                                                    searchWords={
                                                        props.searchWords
                                                    }
                                                    text={
                                                        props
                                                            .subscriptionGroupCardData
                                                            .emails[0]
                                                    }
                                                />
                                            </Typography>
                                        )}
                                </div>
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
