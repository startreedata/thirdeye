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
import MoreVertIcon from "@material-ui/icons/MoreVert";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    UiAlertDatasetAndMetric,
    UiAlertSubscriptionGroup,
} from "../../../rest/dto/ui-alert.interfaces";
import {
    getAlertsUpdatePath,
    getAlertsViewPath,
    getSubscriptionGroupsViewPath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { AlertCardProps } from "./alert-card.interfaces";
import { useAlertCardStyles } from "./alert-card.styles";

export const AlertCard: FunctionComponent<AlertCardProps> = (
    props: AlertCardProps
) => {
    const alertCardClasses = useAlertCardStyles();
    const [
        alertOptionsAnchorElement,
        setAlertOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const handleAlertOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setAlertOptionsAnchorElement(event.currentTarget);
    };

    const handleAlertOptionsClose = (): void => {
        setAlertOptionsAnchorElement(null);
    };

    const handleAlertViewDetails = (): void => {
        if (!props.uiAlert) {
            return;
        }

        history.push(getAlertsViewPath(props.uiAlert.id));
        handleAlertOptionsClose();
    };

    const handleAlertStateToggle = (): void => {
        if (!props.uiAlert || !props.uiAlert.alert) {
            return;
        }

        props.uiAlert.alert.active = !props.uiAlert.alert.active;
        props.onChange && props.onChange(props.uiAlert);
        handleAlertOptionsClose();
    };

    const handleAlertEdit = (): void => {
        if (!props.uiAlert) {
            return;
        }

        history.push(getAlertsUpdatePath(props.uiAlert.id));
        handleAlertOptionsClose();
    };

    const handleAlertDelete = (): void => {
        if (!props.uiAlert) {
            return;
        }

        props.onDelete && props.onDelete(props.uiAlert);
        handleAlertOptionsClose();
    };

    const handleSubscriptionGroupViewDetails = (
        subscriptionGroup: UiAlertSubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        history.push(getSubscriptionGroupsViewPath(subscriptionGroup.id));
    };

    const getAlertDataSetAndMetric = (
        uiAlertDatasetAndMetric: UiAlertDatasetAndMetric
    ): string => {
        if (!uiAlertDatasetAndMetric) {
            return "";
        }

        return `${uiAlertDatasetAndMetric.datasetName}${t(
            "label.pair-separator"
        )}${uiAlertDatasetAndMetric.metricName}`;
    };

    const getUiAlertSubscriptionGroupName = (
        uiAlertSubscriptionGroup: UiAlertSubscriptionGroup
    ): string => {
        if (!uiAlertSubscriptionGroup) {
            return "";
        }

        return uiAlertSubscriptionGroup.name;
    };

    return (
        <Card variant="outlined">
            {props.uiAlert && (
                <CardHeader
                    action={
                        <Grid container alignItems="center" spacing={0}>
                            {/* Active/inactive */}
                            <Grid item>
                                <Typography
                                    className={
                                        props.uiAlert.active
                                            ? alertCardClasses.active
                                            : alertCardClasses.inactive
                                    }
                                    variant="h6"
                                >
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.uiAlert.activeText}
                                    />
                                </Typography>
                            </Grid>

                            <Grid item>
                                {/* Alert options button */}
                                <IconButton onClick={handleAlertOptionsClick}>
                                    <MoreVertIcon />
                                </IconButton>

                                {/* Alert options */}
                                <Menu
                                    anchorEl={alertOptionsAnchorElement}
                                    open={Boolean(alertOptionsAnchorElement)}
                                    onClose={handleAlertOptionsClose}
                                >
                                    {/* View details */}
                                    {props.showViewDetails && (
                                        <MenuItem
                                            onClick={handleAlertViewDetails}
                                        >
                                            {t("label.view-details")}
                                        </MenuItem>
                                    )}

                                    {/* Activate/deactivete alert */}
                                    <MenuItem onClick={handleAlertStateToggle}>
                                        {props.uiAlert.active
                                            ? t("label.deactivate-entity", {
                                                  entity: t("label.alert"),
                                              })
                                            : t("label.activate-entity", {
                                                  entity: t("label.alert"),
                                              })}
                                    </MenuItem>

                                    {/* Edit alert */}
                                    <MenuItem onClick={handleAlertEdit}>
                                        {t("label.edit-entity", {
                                            entity: t("label.alert"),
                                        })}
                                    </MenuItem>

                                    {/* Delete alert */}
                                    <MenuItem onClick={handleAlertDelete}>
                                        {t("label.delete-entity", {
                                            entity: t("label.alert"),
                                        })}
                                    </MenuItem>
                                </Menu>
                            </Grid>
                        </Grid>
                    }
                    title={
                        <>
                            {/* Alert name */}
                            {props.showViewDetails && (
                                <Link onClick={handleAlertViewDetails}>
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.uiAlert.name}
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
                {props.uiAlert && (
                    <Grid container>
                        {/* Created by */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.created-by")}
                                searchWords={props.searchWords}
                                values={[props.uiAlert.createdBy]}
                            />
                        </Grid>

                        {/* Separator */}
                        <Grid item xs={12}>
                            <Divider variant="fullWidth" />
                        </Grid>

                        {/* Detection type */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<string>
                                showCount
                                name={t("label.detection-type")}
                                searchWords={props.searchWords}
                                values={props.uiAlert.detectionTypes}
                            />
                        </Grid>

                        {/* Dataset/Metric */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<UiAlertDatasetAndMetric>
                                showCount
                                name={`${t("label.dataset")}${t(
                                    "label.pair-separator"
                                )}${t("label.metric")}`}
                                searchWords={props.searchWords}
                                valueRenderer={getAlertDataSetAndMetric}
                                values={props.uiAlert.datasetAndMetrics}
                            />
                        </Grid>

                        {/* Filtered by */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<string>
                                showCount
                                name={t("label.filtered-by")}
                                searchWords={props.searchWords}
                                values={props.uiAlert.filteredBy}
                            />
                        </Grid>

                        {/* Subscription groups */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<UiAlertSubscriptionGroup>
                                link
                                showCount
                                name={t("label.subscription-groups")}
                                searchWords={props.searchWords}
                                valueRenderer={getUiAlertSubscriptionGroupName}
                                values={props.uiAlert.subscriptionGroups}
                                onClick={handleSubscriptionGroupViewDetails}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!props.uiAlert && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
