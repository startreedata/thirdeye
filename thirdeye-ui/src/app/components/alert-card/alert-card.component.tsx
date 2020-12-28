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
import { ExpandLess, ExpandMore, MoreVert } from "@material-ui/icons";
import { isEmpty } from "lodash";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getAlertsUpdatePath,
    getSubscriptionGroupsDetailPath,
} from "../../utils/routes-util/routes-util";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { AlertCardProps } from "./alert-card.interfaces";
import { useAlertCardStyles } from "./alert-card.styles";

export const AlertCard: FunctionComponent<AlertCardProps> = (
    props: AlertCardProps
) => {
    const alertCardClasses = useAlertCardStyles();
    const [expand, setExpand] = useState(false);
    const [
        alertOptionsAnchorElement,
        setAlertOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const onAlertOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setAlertOptionsAnchorElement(event.currentTarget);
    };

    const onViewAlertDetails = (): void => {
        history.push(getAlertsDetailPath(props.alert.id));
    };

    const onAlertStateToggle = (): void => {
        props.onStateToggle && props.onStateToggle(props.alert);

        closeAlertOptions();
    };

    const onEditAlert = (): void => {
        history.push(getAlertsUpdatePath(props.alert.id));
    };

    const onDeleteAlert = (): void => {
        props.onDelete && props.onDelete(props.alert);

        closeAlertOptions();
    };

    const onExpandToggle = (): void => {
        setExpand((expand) => !expand);
    };

    const onViewSubscriptionGroupDetails = (id: number): void => {
        history.push(getSubscriptionGroupsDetailPath(id));
    };

    const closeAlertOptions = (): void => {
        setAlertOptionsAnchorElement(null);
    };

    return (
        <Card variant="outlined">
            {/* Alert name */}
            <CardHeader
                disableTypography
                action={
                    <Grid container alignItems="center">
                        {/* Active/inactive */}
                        <Grid item>
                            <Typography
                                className={
                                    props.alert.active
                                        ? alertCardClasses.activeText
                                        : alertCardClasses.inactiveText
                                }
                                variant="h6"
                            >
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    text={props.alert.activeText}
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
                                    text={props.alert.name}
                                />
                            </Link>
                        )}
                    </>
                }
            />

            <Menu
                anchorEl={alertOptionsAnchorElement}
                open={Boolean(alertOptionsAnchorElement)}
                onClose={closeAlertOptions}
            >
                {/* View details */}
                {!props.hideViewDetailsLinks && (
                    <MenuItem onClick={onViewAlertDetails}>
                        {t("label.view-details")}
                    </MenuItem>
                )}

                {/* Activate/deactivete alert */}
                <MenuItem onClick={onAlertStateToggle}>
                    {props.alert.active
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

            <CardContent>
                <Grid container>
                    <Grid container item md={12}>
                        {/* Created by */}
                        <Grid item md={4}>
                            <Typography variant="subtitle2">
                                {t("label.created-by")}
                            </Typography>

                            <TextHighlighter
                                searchWords={props.searchWords}
                                text={props.alert.createdBy}
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
                            <div className={alertCardClasses.label}>
                                <Typography variant="subtitle2">
                                    {t("label.detection-type")}
                                </Typography>
                            </div>

                            {/* Expand/collapse button */}
                            {props.alert.detectionTypes &&
                                props.alert.detectionTypes.length > 1 && (
                                    <div
                                        className={
                                            alertCardClasses.expandCollapseButton
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

                            <div className={alertCardClasses.value}>
                                {/* No data available */}
                                {isEmpty(props.alert.detectionTypes) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )}

                                {/* All detection types */}
                                {!isEmpty(props.alert.detectionTypes) &&
                                    expand && (
                                        <>
                                            {props.alert.detectionTypes.map(
                                                (detectionType, index) => (
                                                    <Typography
                                                        key={index}
                                                        variant="body2"
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            text={detectionType}
                                                        />
                                                    </Typography>
                                                )
                                            )}
                                        </>
                                    )}

                                {/* First detection type */}
                                {!isEmpty(props.alert.detectionTypes) &&
                                    !expand && (
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                text={
                                                    props.alert
                                                        .detectionTypes[0]
                                                }
                                            />
                                        </Typography>
                                    )}
                            </div>
                        </Grid>

                        {/* Dataset / Metric */}
                        <Grid item md={3}>
                            <div className={alertCardClasses.label}>
                                <Typography variant="subtitle2">
                                    {t("label.dataset-/-metric")}
                                </Typography>
                            </div>

                            {/* Expand/collapse button */}
                            {props.alert.datasetAndMetrics &&
                                props.alert.datasetAndMetrics.length > 1 && (
                                    <div
                                        className={
                                            alertCardClasses.expandCollapseButton
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {/* Colapse */}
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

                            <div className={alertCardClasses.value}>
                                {/* No data available */}
                                {isEmpty(props.alert.datasetAndMetrics) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )}

                                {/* All dataset / metric */}
                                {!isEmpty(props.alert.datasetAndMetrics) &&
                                    expand && (
                                        <>
                                            {props.alert.datasetAndMetrics.map(
                                                (datasetAndMetric, index) => (
                                                    <Typography
                                                        key={index}
                                                        variant="body2"
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            text={t(
                                                                "label.dataset-/-metric-values",
                                                                {
                                                                    dataset:
                                                                        datasetAndMetric.datasetName,
                                                                    metric:
                                                                        datasetAndMetric.metricName,
                                                                }
                                                            )}
                                                        />
                                                    </Typography>
                                                )
                                            )}
                                        </>
                                    )}

                                {/* First dataset / metric */}
                                {!isEmpty(props.alert.datasetAndMetrics) &&
                                    !expand && (
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                text={t(
                                                    "label.dataset-/-metric-values",
                                                    {
                                                        dataset:
                                                            props.alert
                                                                .datasetAndMetrics[0]
                                                                .datasetName,
                                                        metric:
                                                            props.alert
                                                                .datasetAndMetrics[0]
                                                                .metricName,
                                                    }
                                                )}
                                            />
                                        </Typography>
                                    )}
                            </div>
                        </Grid>

                        {/* Filtered by */}
                        <Grid item md={3}>
                            <div className={alertCardClasses.label}>
                                <Typography variant="subtitle2">
                                    {t("label.filtered-by")}
                                </Typography>
                            </div>

                            {/* Expand/collapse button */}
                            {props.alert.filteredBy &&
                                props.alert.filteredBy.length > 1 && (
                                    <div
                                        className={
                                            alertCardClasses.expandCollapseButton
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

                            <div className={alertCardClasses.value}>
                                {/* No data available */}
                                {isEmpty(props.alert.filteredBy) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )}

                                {/* All filtered by */}
                                {!isEmpty(props.alert.filteredBy) && expand && (
                                    <>
                                        {props.alert.filteredBy.map(
                                            (filteredBy, index) => (
                                                <Typography
                                                    key={index}
                                                    variant="body2"
                                                >
                                                    <TextHighlighter
                                                        searchWords={
                                                            props.searchWords
                                                        }
                                                        text={filteredBy}
                                                    />
                                                </Typography>
                                            )
                                        )}
                                    </>
                                )}

                                {/* First filtered by */}
                                {!isEmpty(props.alert.filteredBy) && !expand && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={props.alert.filteredBy[0]}
                                        />
                                    </Typography>
                                )}
                            </div>
                        </Grid>

                        {/* Subscription groups */}
                        <Grid item md={3}>
                            <div className={alertCardClasses.label}>
                                <Typography variant="subtitle2">
                                    {t("label.subscription-groups")}
                                </Typography>
                            </div>

                            {/* Expand/collapse button */}
                            {props.alert.subscriptionGroups &&
                                props.alert.subscriptionGroups.length > 1 && (
                                    <div
                                        className={
                                            alertCardClasses.expandCollapseButton
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

                            <div className={alertCardClasses.value}>
                                {/* No data available */}
                                {isEmpty(props.alert.subscriptionGroups) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )}

                                {/* All subscription groups */}
                                {!isEmpty(props.alert.subscriptionGroups) &&
                                    expand && (
                                        <>
                                            {props.alert.subscriptionGroups.map(
                                                (subscriptionGroup, index) => (
                                                    <Link
                                                        component="button"
                                                        display="block"
                                                        key={index}
                                                        variant="body2"
                                                        onClick={(): void => {
                                                            onViewSubscriptionGroupDetails(
                                                                subscriptionGroup.id
                                                            );
                                                        }}
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            text={
                                                                subscriptionGroup.name
                                                            }
                                                        />
                                                    </Link>
                                                )
                                            )}
                                        </>
                                    )}

                                {/* First subscription group */}
                                {!isEmpty(props.alert.subscriptionGroups) &&
                                    !expand && (
                                        <Link
                                            component="button"
                                            display="block"
                                            variant="body2"
                                            onClick={(): void => {
                                                onViewSubscriptionGroupDetails(
                                                    props.alert
                                                        .subscriptionGroups[0]
                                                        .id
                                                );
                                            }}
                                        >
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                text={
                                                    props.alert
                                                        .subscriptionGroups[0]
                                                        .name
                                                }
                                            />
                                        </Link>
                                    )}
                            </div>
                        </Grid>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
