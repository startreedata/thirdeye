import {
    Card,
    CardContent,
    CardHeader,
    Divider,
    Grid,
    IconButton,
    Link,
    Menu,
    MenuItem,
    Typography,
} from "@material-ui/core";
import SettingsIcon from "@material-ui/icons/Settings";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import Highlighter from "react-highlight-words";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getAlertsUpdatePath,
} from "../../utils/route/routes-util";
import { AlertCardProps } from "./alert-card.interfaces";
import { useAlertCardStyles } from "./alert-card.styles";

export const AlertCard: FunctionComponent<AlertCardProps> = (
    props: AlertCardProps
) => {
    const alertCardClasses = useAlertCardStyles();
    const [
        optionsAnchorElement,
        setOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const onAlertDetails = (): void => {
        history.push(getAlertsDetailPath(props.alert.id));
    };

    const onAlertEdit = (): void => {
        history.push(getAlertsUpdatePath(props.alert.id));
    };

    const onAlertStateToggle = (): void => {
        props.onAlertStateToggle && props.onAlertStateToggle(props.alert);

        closeAlertOptions();
    };

    const onAlertOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        if (!event) {
            return;
        }

        setOptionsAnchorElement(event.currentTarget);
    };

    const closeAlertOptions = (): void => {
        setOptionsAnchorElement(null);
    };

    return (
        <Card className={alertCardClasses.container} variant="outlined">
            {/* Alert name */}
            <CardHeader
                disableTypography
                action={
                    <Grid container alignItems="center">
                        <Grid item>
                            <Typography
                                className={
                                    props.alert.active
                                        ? alertCardClasses.activeText
                                        : alertCardClasses.inactiveText
                                }
                                variant="h6"
                            >
                                {props.alert.active
                                    ? t("label.active")
                                    : t("label.inactive")}
                            </Typography>
                        </Grid>
                        <Grid item>
                            <IconButton onClick={onAlertOptionsClick}>
                                <SettingsIcon color="primary" />
                            </IconButton>
                        </Grid>
                    </Grid>
                }
                className={
                    props.alert.active
                        ? alertCardClasses.activeHeader
                        : alertCardClasses.inactiveHeader
                }
                title={
                    <Link
                        component="button"
                        variant="h6"
                        onClick={onAlertDetails}
                    >
                        <Highlighter
                            highlightClassName={alertCardClasses.highlight}
                            searchWords={props.searchWords as string[]}
                            textToHighlight={props.alert.name}
                        />
                    </Link>
                }
            />

            <Menu
                anchorEl={optionsAnchorElement}
                open={Boolean(optionsAnchorElement)}
                onClose={closeAlertOptions}
            >
                <MenuItem onClick={onAlertDetails}>
                    {t("label.view-details")}
                </MenuItem>
                <MenuItem onClick={onAlertEdit}>
                    {t("label.edit-alert")}
                </MenuItem>
                <MenuItem onClick={onAlertStateToggle}>
                    {props.alert.active
                        ? t("label.deactivate-alert")
                        : t("label.activate-alert")}
                </MenuItem>
            </Menu>

            <CardContent>
                <Grid container>
                    <Grid container item>
                        <Grid item md={3}>
                            {/* Metric */}
                            <Typography variant="body2">
                                <strong>{t("label.metric")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        alertCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={t(
                                        "label.no-data-available"
                                    )}
                                />
                            </Typography>
                        </Grid>

                        {/* Dataset */}
                        <Grid item md={3}>
                            <Typography variant="body2">
                                <strong>{t("label.dataset")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        alertCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={t(
                                        "label.no-data-available"
                                    )}
                                />
                            </Typography>
                        </Grid>

                        {/* Detection type */}
                        <Grid item md={3}>
                            <Typography variant="body2">
                                <strong>{t("label.detection-type")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        alertCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={props.alert.description}
                                />
                            </Typography>
                        </Grid>

                        {/* Subscription group */}
                        <Grid item md={3}>
                            <Typography variant="body2">
                                <strong>{t("label.subscription-group")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        alertCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={t(
                                        "label.no-data-available"
                                    )}
                                />
                            </Typography>
                        </Grid>
                    </Grid>

                    <Divider className={alertCardClasses.divider} />

                    {/* Application */}
                    <Grid container item>
                        <Grid item md={3}>
                            <Typography variant="body2">
                                <strong>{t("label.application")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        alertCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={t(
                                        "label.no-data-available"
                                    )}
                                />
                            </Typography>
                        </Grid>

                        {/* Created by */}
                        <Grid item md={3}>
                            <Typography variant="body2">
                                <strong>{t("label.created-by")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        alertCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={
                                        props.alert.owner.principal
                                    }
                                />
                            </Typography>
                        </Grid>

                        {/* Filtered by */}
                        <Grid item md={3}>
                            <Typography variant="body2">
                                <strong>{t("label.filtered-by")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        alertCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={t(
                                        "label.no-data-available"
                                    )}
                                />
                            </Typography>
                        </Grid>

                        {/* Breakdown by */}
                        <Grid item md={3}>
                            <Typography variant="body2">
                                <strong>{t("label.breakdown-by")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        alertCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={t(
                                        "label.no-data-available"
                                    )}
                                />
                            </Typography>
                        </Grid>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
