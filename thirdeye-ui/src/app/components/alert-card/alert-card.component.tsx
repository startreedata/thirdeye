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
    getConfigurationSubscriptionGroupsDetailPath,
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
        setOptionsAnchorElement(event.currentTarget);
    };

    const onExpandToggle = (): void => {
        setExpand((expand) => !expand);
    };

    const onSubscriptionGroupDetails = (id: number): void => {
        history.push(getConfigurationSubscriptionGroupsDetailPath(id));
    };

    const closeAlertOptions = (): void => {
        setOptionsAnchorElement(null);
    };

    return (
        <Card variant="outlined">
            {/* Alert name */}
            <CardHeader
                disableTypography
                action={
                    <Grid container alignItems="center">
                        <Grid item>
                            {/* Active/inactive indicator */}
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
                                    textToHighlight={props.alert.activeText}
                                />
                            </Typography>
                        </Grid>

                        <Grid item>
                            <IconButton onClick={onAlertOptionsClick}>
                                <MoreVert />
                            </IconButton>
                        </Grid>
                    </Grid>
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
                            // Alert name
                            <Link
                                component="button"
                                variant="h6"
                                onClick={onAlertDetails}
                            >
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    textToHighlight={props.alert.name}
                                />
                            </Link>
                        )}
                    </>
                }
            />

            <Menu
                anchorEl={optionsAnchorElement}
                open={Boolean(optionsAnchorElement)}
                onClose={closeAlertOptions}
            >
                {/* View details */}
                {!props.hideViewDetailsLinks && (
                    <MenuItem onClick={onAlertDetails}>
                        {t("label.view-details")}
                    </MenuItem>
                )}

                {/* Edit alert */}
                <MenuItem onClick={onAlertEdit}>
                    {t("label.edit-alert")}
                </MenuItem>

                {/* Activete/deactivete alert */}
                <MenuItem onClick={onAlertStateToggle}>
                    {props.alert.active
                        ? t("label.deactivate-alert")
                        : t("label.activate-alert")}
                </MenuItem>
            </Menu>

            <CardContent>
                <Grid container>
                    <Grid container item md={12}>
                        {/* Created by */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.created-by")}</strong>
                            </Typography>

                            <TextHighlighter
                                searchWords={props.searchWords}
                                textToHighlight={props.alert.createdBy}
                            />
                        </Grid>
                    </Grid>

                    <Grid item md={12}>
                        <Divider variant="fullWidth" />
                    </Grid>

                    <Grid container item md={12}>
                        {/* Detection type */}
                        <Grid item md={3}>
                            <div className={alertCardClasses.bottomRowLabel}>
                                <Typography variant="body2">
                                    <strong>{t("label.detection-type")}</strong>
                                </Typography>
                            </div>

                            {props.alert.detectionTypes &&
                                props.alert.detectionTypes.length > 1 && (
                                    // Expand/collapse icon
                                    <div
                                        className={
                                            alertCardClasses.bottomRowIcon
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

                            <div className={alertCardClasses.bottomRowValue}>
                                {isEmpty(props.alert.detectionTypes) && (
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

                                {!isEmpty(props.alert.detectionTypes) &&
                                    expand && (
                                        // All detection types
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
                                                            textToHighlight={
                                                                detectionType
                                                            }
                                                        />
                                                    </Typography>
                                                )
                                            )}
                                        </>
                                    )}

                                {!isEmpty(props.alert.detectionTypes) &&
                                    !expand && (
                                        // First detection type
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                textToHighlight={
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
                            <div className={alertCardClasses.bottomRowLabel}>
                                <Typography variant="body2">
                                    <strong>
                                        {t("label.dataset-/-metric")}
                                    </strong>
                                </Typography>
                            </div>

                            {props.alert.datasetAndMetrics &&
                                props.alert.datasetAndMetrics.length > 1 && (
                                    // Expand/collapse icon
                                    <div
                                        className={
                                            alertCardClasses.bottomRowIcon
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {expand && (
                                                // Colapse
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

                            <div className={alertCardClasses.bottomRowValue}>
                                {isEmpty(props.alert.datasetAndMetrics) && (
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

                                {!isEmpty(props.alert.datasetAndMetrics) &&
                                    expand && (
                                        // All dataset / metric
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
                                                            textToHighlight={t(
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

                                {!isEmpty(props.alert.datasetAndMetrics) &&
                                    !expand && (
                                        // First dataset / metric
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                textToHighlight={t(
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
                            <div className={alertCardClasses.bottomRowLabel}>
                                <Typography variant="body2">
                                    <strong>{t("label.filtered-by")}</strong>
                                </Typography>
                            </div>

                            {props.alert.filteredBy &&
                                props.alert.filteredBy.length > 1 && (
                                    // Expand/collapse icon
                                    <div
                                        className={
                                            alertCardClasses.bottomRowIcon
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

                            <div className={alertCardClasses.bottomRowValue}>
                                {isEmpty(props.alert.filteredBy) && (
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

                                {!isEmpty(props.alert.filteredBy) && expand && (
                                    // All filtered by
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
                                                        textToHighlight={
                                                            filteredBy
                                                        }
                                                    />
                                                </Typography>
                                            )
                                        )}
                                    </>
                                )}

                                {!isEmpty(props.alert.filteredBy) && !expand && (
                                    // First filtered by
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            textToHighlight={
                                                props.alert.filteredBy[0]
                                            }
                                        />
                                    </Typography>
                                )}
                            </div>
                        </Grid>

                        {/* Subscription groups */}
                        <Grid item md={3}>
                            <div className={alertCardClasses.bottomRowLabel}>
                                <Typography variant="body2">
                                    <strong>
                                        {t("label.subscription-groups")}
                                    </strong>
                                </Typography>
                            </div>

                            {props.alert.subscriptionGroups &&
                                props.alert.subscriptionGroups.length > 1 && (
                                    // Expand/collapse icon
                                    <div
                                        className={
                                            alertCardClasses.bottomRowIcon
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

                            <div className={alertCardClasses.bottomRowValue}>
                                {isEmpty(props.alert.subscriptionGroups) && (
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

                                {!isEmpty(props.alert.subscriptionGroups) &&
                                    expand && (
                                        // All subscription groups
                                        <>
                                            {props.alert.subscriptionGroups.map(
                                                (subscriptionGroup, index) => (
                                                    <Link
                                                        component="button"
                                                        display="block"
                                                        key={index}
                                                        variant="body2"
                                                        onClick={(): void => {
                                                            onSubscriptionGroupDetails(
                                                                subscriptionGroup.id
                                                            );
                                                        }}
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            textToHighlight={
                                                                subscriptionGroup.name
                                                            }
                                                        />
                                                    </Link>
                                                )
                                            )}
                                        </>
                                    )}

                                {!isEmpty(props.alert.subscriptionGroups) &&
                                    !expand && (
                                        // First subscription group
                                        <Link
                                            component="button"
                                            display="block"
                                            variant="body2"
                                            onClick={(): void => {
                                                onSubscriptionGroupDetails(
                                                    props.alert
                                                        .subscriptionGroups[0]
                                                        .id
                                                );
                                            }}
                                        >
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                textToHighlight={
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
