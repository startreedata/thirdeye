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
    getConfigurationSubscriptionGroupsDetailPath,
    getConfigurationSubscriptionGroupsUpdatePath,
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
        optionsAnchorElement,
        setOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const onSubscriptionGroupDetails = (): void => {
        history.push(
            getConfigurationSubscriptionGroupsDetailPath(
                props.subscriptionGroup.id
            )
        );
    };

    const onSubscriptionGroupEdit = (): void => {
        history.push(
            getConfigurationSubscriptionGroupsUpdatePath(
                props.subscriptionGroup.id
            )
        );
    };

    const onSubscriptionGroupOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setOptionsAnchorElement(event.currentTarget);
    };

    const onExpandToggle = (): void => {
        setExpand((expand) => !expand);
    };

    const onAlertDetails = (id: number): void => {
        history.push(getAlertsDetailPath(id));
    };

    const closeSubscriptionGroupOptions = (): void => {
        setOptionsAnchorElement(null);
    };

    return (
        <Card variant="outlined">
            {/* Subscription group name */}
            <CardHeader
                disableTypography
                action={
                    <IconButton onClick={onSubscriptionGroupOptionsClick}>
                        <MoreVert />
                    </IconButton>
                }
                title={
                    <>
                        {props.hideViewDetailsLinks && (
                            // Summary
                            <Typography variant="h6">
                                {t("label.summary")}
                            </Typography>
                        )}

                        {!props.hideViewDetailsLinks && (
                            // Subscription group name
                            <Link
                                component="button"
                                variant="h6"
                                onClick={onSubscriptionGroupDetails}
                            >
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    textToHighlight={
                                        props.subscriptionGroup.name
                                    }
                                />
                            </Link>
                        )}
                    </>
                }
            />

            <Menu
                anchorEl={optionsAnchorElement}
                open={Boolean(optionsAnchorElement)}
                onClose={closeSubscriptionGroupOptions}
            >
                {/* View details */}
                {!props.hideViewDetailsLinks && (
                    <MenuItem onClick={onSubscriptionGroupDetails}>
                        {t("label.view-details")}
                    </MenuItem>
                )}

                {/* Edit subscription group */}
                <MenuItem onClick={onSubscriptionGroupEdit}>
                    {t("label.edit-subscription-group")}
                </MenuItem>
            </Menu>

            <CardContent>
                <Grid container>
                    <Grid container item md={12}>
                        {/* Application */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.application")}</strong>
                            </Typography>

                            <TextHighlighter
                                searchWords={props.searchWords}
                                textToHighlight={
                                    props.subscriptionGroup.application
                                }
                            />
                        </Grid>

                        {/* Subscribed alerts */}
                        <Grid item md={4}>
                            <div
                                className={
                                    subscriptionGroupCardClasses.bottomRowLabel
                                }
                            >
                                <Typography variant="body2">
                                    <strong>
                                        {t("label.subscribed-alerts")}
                                    </strong>
                                </Typography>
                            </div>

                            {props.subscriptionGroup.alerts &&
                                props.subscriptionGroup.alerts.length > 1 && (
                                    // Expand/collapse icon
                                    <div
                                        className={
                                            subscriptionGroupCardClasses.bottomRowIcon
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {expand && (
                                                // Collapse
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}

                                            {!expand && (
                                                // Expand
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
                                    subscriptionGroupCardClasses.bottomRowValue
                                }
                            >
                                {isEmpty(props.subscriptionGroup.alerts) && (
                                    // No data available
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            textToHighlight={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )}

                                {!isEmpty(props.subscriptionGroup.alerts) &&
                                    expand && (
                                        // All subscribed alerts
                                        <>
                                            {props.subscriptionGroup.alerts.map(
                                                (alert, index) => (
                                                    <Link
                                                        component="button"
                                                        display="block"
                                                        key={index}
                                                        variant="body2"
                                                        onClick={(): void => {
                                                            onAlertDetails(
                                                                alert.id
                                                            );
                                                        }}
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            textToHighlight={
                                                                alert.name
                                                            }
                                                        />
                                                    </Link>
                                                )
                                            )}
                                        </>
                                    )}

                                {!isEmpty(props.subscriptionGroup.alerts) &&
                                    !expand && (
                                        // First subscribed email
                                        <Link
                                            component="button"
                                            display="block"
                                            variant="body2"
                                            onClick={(): void => {
                                                onAlertDetails(
                                                    props.subscriptionGroup
                                                        .alerts[0].id
                                                );
                                            }}
                                        >
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                textToHighlight={
                                                    props.subscriptionGroup
                                                        .alerts[0].name
                                                }
                                            />
                                        </Link>
                                    )}
                            </div>
                        </Grid>

                        {/* Subscribed emails */}
                        <Grid item md={3}>
                            <div
                                className={
                                    subscriptionGroupCardClasses.bottomRowLabel
                                }
                            >
                                <Typography variant="body2">
                                    <strong>
                                        {t("label.subscribed-emails")}
                                    </strong>
                                </Typography>
                            </div>

                            {props.subscriptionGroup.emails &&
                                props.subscriptionGroup.emails.length > 1 && (
                                    // Expand/collapse icon
                                    <div
                                        className={
                                            subscriptionGroupCardClasses.bottomRowIcon
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {expand && (
                                                // Collapse
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}

                                            {!expand && (
                                                // Expand
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
                                    subscriptionGroupCardClasses.bottomRowValue
                                }
                            >
                                {isEmpty(props.subscriptionGroup.emails) && (
                                    // No data available
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            textToHighlight={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )}

                                {!isEmpty(props.subscriptionGroup.emails) &&
                                    expand && (
                                        // All subscribed emails
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
                                                            textToHighlight={
                                                                email
                                                            }
                                                        />
                                                    </Typography>
                                                )
                                            )}
                                        </>
                                    )}

                                {!isEmpty(props.subscriptionGroup.emails) &&
                                    !expand && (
                                        // First subscribed email
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                textToHighlight={
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
