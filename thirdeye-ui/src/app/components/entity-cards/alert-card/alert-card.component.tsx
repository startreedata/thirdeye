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
import { MoreVert } from "@material-ui/icons";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getAlertsUpdatePath,
    getSubscriptionGroupsDetailPath,
} from "../../../utils/routes-util/routes-util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { ExpandableDetails } from "../expandable-details/expandable-details.components";
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
    const [expand, setExpand] = useState(false);
    const history = useHistory();
    const { t } = useTranslation();

    const onAlertOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setAlertOptionsAnchorElement(event.currentTarget);
    };

    const onCloseAlertOptions = (): void => {
        setAlertOptionsAnchorElement(null);
    };

    const onViewAlertDetails = (): void => {
        history.push(getAlertsDetailPath(props.alertCardData.id));

        onCloseAlertOptions();
    };

    const onAlertStateToggle = (): void => {
        if (props.alertCardData && props.alertCardData.alert) {
            props.alertCardData.alert.active = !props.alertCardData.alert
                .active;
        }

        props.onChange && props.onChange(props.alertCardData);

        onCloseAlertOptions();
    };

    const onEditAlert = (): void => {
        history.push(getAlertsUpdatePath(props.alertCardData.id));

        onCloseAlertOptions();
    };

    const onDeleteAlert = (): void => {
        props.onDelete && props.onDelete(props.alertCardData);

        onCloseAlertOptions();
    };

    const onViewSubscriptionGroupDetails = (
        subscriptionGroup: AlertSubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        history.push(getSubscriptionGroupsDetailPath(subscriptionGroup.id));
    };

    const getDataSetAndMetricValues = (
        value: AlertDatasetAndMetric
    ): string => {
        if (!value) {
            return "";
        }

        return t("label.dataset-/-metric-values", {
            dataset: value.datasetName,
            metric: value.metricName,
        });
    };

    const getSubscriptionGroupValue = (
        value: AlertSubscriptionGroup
    ): string => {
        if (!value) {
            return "";
        }

        return value.name;
    };

    return (
        <Card variant="outlined">
            {props.alertCardData && (
                <>
                    <CardHeader
                        disableTypography
                        action={
                            <Grid container alignItems="center">
                                {/* Active/inactive */}
                                <Grid item>
                                    <Typography
                                        className={
                                            props.alertCardData.active
                                                ? alertCardClasses.activeText
                                                : alertCardClasses.inactiveText
                                        }
                                        variant="h6"
                                    >
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={
                                                props.alertCardData.activeText
                                            }
                                        />
                                    </Typography>
                                </Grid>

                                {/* Alert options button */}
                                <Grid item>
                                    <IconButton onClick={onAlertOptionsClick}>
                                        <MoreVert />
                                    </IconButton>
                                </Grid>
                            </Grid>
                        }
                        title={
                            <>
                                {/* Summary */}
                                {props.hideViewDetailsLinks && (
                                    <Typography variant="h6">
                                        {t("label.summary")}
                                    </Typography>
                                )}

                                {/* Alert name */}
                                {!props.hideViewDetailsLinks && (
                                    <Link
                                        component="button"
                                        variant="h6"
                                        onClick={onViewAlertDetails}
                                    >
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={props.alertCardData.name}
                                        />
                                    </Link>
                                )}
                            </>
                        }
                    />

                    <Menu
                        anchorEl={alertOptionsAnchorElement}
                        open={Boolean(alertOptionsAnchorElement)}
                        onClose={onCloseAlertOptions}
                    >
                        {/* View details */}
                        {!props.hideViewDetailsLinks && (
                            <MenuItem onClick={onViewAlertDetails}>
                                {t("label.view-details")}
                            </MenuItem>
                        )}

                        {/* Activate/deactivete alert */}
                        <MenuItem onClick={onAlertStateToggle}>
                            {props.alertCardData.active
                                ? t("label.deactivate-alert")
                                : t("label.activate-alert")}
                        </MenuItem>

                        {/* Edit alert */}
                        <MenuItem onClick={onEditAlert}>
                            {t("label.edit-alert")}
                        </MenuItem>

                        {/* Delete alert */}
                        <MenuItem onClick={onDeleteAlert}>
                            {t("label.delete-alert")}
                        </MenuItem>
                    </Menu>
                </>
            )}

            <CardContent>
                {props.alertCardData && (
                    <Grid container>
                        <Grid container item md={12}>
                            {/* Created by */}
                            <Grid item md={4}>
                                <Typography variant="subtitle2">
                                    {t("label.created-by")}
                                </Typography>

                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    text={props.alertCardData.createdBy}
                                />
                            </Grid>
                        </Grid>

                        {/* Separator */}
                        <Grid item md={12}>
                            <Divider variant="fullWidth" />
                        </Grid>

                        <Grid container item md={12}>
                            {/* Detection type */}
                            <Grid item md={3}>
                                <ExpandableDetails<string>
                                    expand={expand}
                                    label={t("label.detection-type")}
                                    searchWords={props.searchWords}
                                    valueTextFn={(value: string): string => {
                                        return value || "";
                                    }}
                                    values={props.alertCardData.detectionTypes}
                                    onChange={setExpand}
                                />
                            </Grid>

                            {/* Dataset / Metric */}
                            <Grid item md={3}>
                                <ExpandableDetails<AlertDatasetAndMetric>
                                    expand={expand}
                                    label={t("label.dataset-/-metric")}
                                    searchWords={props.searchWords}
                                    valueTextFn={getDataSetAndMetricValues}
                                    values={
                                        props.alertCardData.datasetAndMetrics
                                    }
                                    onChange={setExpand}
                                />
                            </Grid>

                            {/* Filtered by */}
                            <Grid item md={3}>
                                <ExpandableDetails<string>
                                    expand={expand}
                                    label={t("label.filtered-by")}
                                    searchWords={props.searchWords}
                                    valueTextFn={(value: string): string => {
                                        return value || "";
                                    }}
                                    values={props.alertCardData.filteredBy}
                                    onChange={setExpand}
                                />
                            </Grid>

                            {/* Subscription groups */}
                            <Grid item md={3}>
                                <ExpandableDetails<AlertSubscriptionGroup>
                                    link
                                    expand={expand}
                                    label={t("label.subscription-groups")}
                                    searchWords={props.searchWords}
                                    valueTextFn={getSubscriptionGroupValue}
                                    values={
                                        props.alertCardData.subscriptionGroups
                                    }
                                    onChange={setExpand}
                                    onLinkClick={onViewSubscriptionGroupDetails}
                                />
                            </Grid>
                        </Grid>
                    </Grid>
                )}

                {/* No data available message */}
                {!props.alertCardData && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
