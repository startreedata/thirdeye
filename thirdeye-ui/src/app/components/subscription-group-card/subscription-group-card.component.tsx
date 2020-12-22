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
import { useSubscriptinoGroupStyles } from "./subscription-group-card.styles";
import { SubscriptionGroupCardProps } from "./subscription-group.interfaces";

export const SubscriptionGroupCard: FunctionComponent<SubscriptionGroupCardProps> = (
    props: SubscriptionGroupCardProps
) => {
    const [expanded, setExpanded] = useState(false);
    const [
        optionsAnchorElement,
        setOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const subscriptionGroupClasses = useSubscriptinoGroupStyles();

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

    const closeSubscriptionGroupOptions = (): void => {
        setOptionsAnchorElement(null);
    };

    const onExpandedToggle = (): void => {
        setExpanded(!expanded);
    };

    const onAlertDetails = (alertId: number) => (): void => {
        history.push(getAlertsDetailPath(alertId));
    };

    return (
        <Card variant="outlined">
            {/* Subscription Group name */}
            <CardHeader
                disableTypography
                action={
                    <IconButton onClick={onSubscriptionGroupOptionsClick}>
                        <MoreVert />
                    </IconButton>
                }
                title={
                    (props.hideViewDetailsLinks && (
                        <Typography variant="h6">
                            {t("label.summary")}
                        </Typography>
                    )) || (
                        <Link
                            component="button"
                            variant="h6"
                            onClick={onSubscriptionGroupDetails}
                        >
                            <TextHighlighter
                                searchWords={props.searchWords}
                                textToHighlight={props.subscriptionGroup.name}
                            />
                        </Link>
                    )
                }
            />

            <Menu
                anchorEl={optionsAnchorElement}
                open={Boolean(optionsAnchorElement)}
                onClose={closeSubscriptionGroupOptions}
            >
                {!props.hideViewDetailsLinks && (
                    <MenuItem onClick={onSubscriptionGroupDetails}>
                        {t("label.view-details")}
                    </MenuItem>
                )}
                <MenuItem onClick={onSubscriptionGroupEdit}>
                    {t("label.edit-subscription-group")}
                </MenuItem>
            </Menu>

            <CardContent>
                <Grid container>
                    <Grid container item>
                        {/* Application */}
                        <Grid item md={3}>
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

                        {/* Alerts */}
                        <Grid item md={3}>
                            <div
                                className={
                                    subscriptionGroupClasses.bottomRowLabel
                                }
                            >
                                <Typography variant="body2">
                                    <strong>
                                        {t("label.subscribed-alerts")}
                                    </strong>
                                </Typography>
                            </div>

                            {!isEmpty(props.subscriptionGroup.alerts) &&
                                props.subscriptionGroup.alerts.length > 1 &&
                                !props.showDetailsExpanded && (
                                    <div
                                        className={
                                            subscriptionGroupClasses.bottomRowIcon
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandedToggle}
                                        >
                                            {(expanded && (
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )) || (
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
                                    subscriptionGroupClasses.bottomRowValue
                                }
                            >
                                {(isEmpty(props.subscriptionGroup.alerts) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            textToHighlight={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )) ||
                                    ((expanded ||
                                        props.showDetailsExpanded) && (
                                        <>
                                            {props.subscriptionGroup.alerts.map(
                                                (alert) => (
                                                    <Link
                                                        component="button"
                                                        display="block"
                                                        key={alert.id}
                                                        variant="body2"
                                                        onClick={onAlertDetails(
                                                            alert.id
                                                        )}
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
                                    )) || (
                                        <Link
                                            component="button"
                                            display="block"
                                            variant="body2"
                                            onClick={onAlertDetails(
                                                props.subscriptionGroup
                                                    .alerts[0].id
                                            )}
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

                        {/* Subscribed Emails */}
                        <Grid item md={3}>
                            <div
                                className={
                                    subscriptionGroupClasses.bottomRowLabel
                                }
                            >
                                <Typography variant="body2">
                                    <strong>
                                        {t("label.subscribed-emails")}
                                    </strong>
                                </Typography>
                            </div>

                            {!isEmpty(props.subscriptionGroup.emails) &&
                                props.subscriptionGroup.emails.length > 1 &&
                                !props.showDetailsExpanded && (
                                    <div
                                        className={
                                            subscriptionGroupClasses.bottomRowIcon
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandedToggle}
                                        >
                                            {(expanded && (
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )) || (
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
                                    subscriptionGroupClasses.bottomRowValue
                                }
                            >
                                {(isEmpty(props.subscriptionGroup.emails) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            textToHighlight={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )) ||
                                    ((expanded ||
                                        props.showDetailsExpanded) && (
                                        <>
                                            {props.subscriptionGroup.emails.map(
                                                (email) => (
                                                    <Typography
                                                        key={alert.name}
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
                                    )) || (
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
