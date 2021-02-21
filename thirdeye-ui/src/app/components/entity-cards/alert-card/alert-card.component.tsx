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
    getAlertsDetailPath,
    getAlertsUpdatePath,
    getSubscriptionGroupsDetailPath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import {
    AlertCardProps,
    AlertDatasetAndMetric,
    AlertSubscriptionGroup,
} from "./alert-card.interfaces";
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
        if (!props.alertCardData) {
            return;
        }

        history.push(getAlertsDetailPath(props.alertCardData.id));
        handleAlertOptionsClose();
    };

    const handleAlertStateToggle = (): void => {
        if (!props.alertCardData || !props.alertCardData.alert) {
            return;
        }

        props.alertCardData.alert.active = !props.alertCardData.alert.active;
        props.onChange && props.onChange(props.alertCardData);
        handleAlertOptionsClose();
    };

    const handleAlertEdit = (): void => {
        if (!props.alertCardData) {
            return;
        }

        history.push(getAlertsUpdatePath(props.alertCardData.id));
        handleAlertOptionsClose();
    };

    const handleAlertDelete = (): void => {
        props.onDelete && props.onDelete(props.alertCardData);
        handleAlertOptionsClose();
    };

    const handleSubscriptionGroupViewDetails = (
        subscriptionGroup: AlertSubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        history.push(getSubscriptionGroupsDetailPath(subscriptionGroup.id));
    };

    const getAlertDataSetAndMetric = (
        alertDatasetAndMetric: AlertDatasetAndMetric
    ): string => {
        if (!alertDatasetAndMetric) {
            return "";
        }

        return `${alertDatasetAndMetric.datasetName}${t(
            "label.pair-separator"
        )}${alertDatasetAndMetric.metricName}`;
    };

    const getAlertSubscriptionGroupName = (
        alertSubscriptionGroup: AlertSubscriptionGroup
    ): string => {
        if (!alertSubscriptionGroup) {
            return "";
        }

        return alertSubscriptionGroup.name;
    };

    return (
        <Card variant="outlined">
            {props.alertCardData && (
                <CardHeader
                    action={
                        <Grid container alignItems="center" spacing={0}>
                            {/* Active/inactive */}
                            <Grid item>
                                <Typography
                                    className={
                                        props.alertCardData.active
                                            ? alertCardClasses.active
                                            : alertCardClasses.inactive
                                    }
                                    variant="h6"
                                >
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.alertCardData.activeText}
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
                                        {props.alertCardData.active
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
                                        text={props.alertCardData.name}
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
                {props.alertCardData && (
                    <Grid container>
                        {/* Created by */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.created-by")}
                                searchWords={props.searchWords}
                                values={[props.alertCardData.createdBy]}
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
                                values={props.alertCardData.detectionTypes}
                            />
                        </Grid>

                        {/* Dataset / Metric */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<AlertDatasetAndMetric>
                                showCount
                                name={`${t("label.dataset")}${t(
                                    "label.pair-separator"
                                )}${t("label.metric")}`}
                                searchWords={props.searchWords}
                                valueRenderer={getAlertDataSetAndMetric}
                                values={props.alertCardData.datasetAndMetrics}
                            />
                        </Grid>

                        {/* Filtered by */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<string>
                                showCount
                                name={t("label.filtered-by")}
                                searchWords={props.searchWords}
                                values={props.alertCardData.filteredBy}
                            />
                        </Grid>

                        {/* Subscription groups */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<AlertSubscriptionGroup>
                                link
                                showCount
                                name={t("label.subscription-groups")}
                                searchWords={props.searchWords}
                                valueRenderer={getAlertSubscriptionGroupName}
                                values={props.alertCardData.subscriptionGroups}
                                onClick={handleSubscriptionGroupViewDetails}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!props.alertCardData && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
