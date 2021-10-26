import { Grid, Typography } from "@material-ui/core";
import { PageContentsCardV1 } from "@startree-ui/platform-ui";
import { forEach } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { UiAlert } from "../../../rest/dto/ui-alert.interfaces";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { AlertCardProps } from "../alert-card/alert-card.interfaces";
import { useAlertCardStyles } from "./alert-card-v1-styles";

export const AlertCardV1: FunctionComponent<AlertCardProps> = (
    props: AlertCardProps
) => {
    const classes = useAlertCardStyles();
    const { t } = useTranslation();

    const getAllSubscriptionGroupsName = (uiAlert: UiAlert | null): string => {
        const subScriptionGroups: Array<string | number> = [];
        if (uiAlert) {
            forEach(uiAlert.subscriptionGroups, (Obj) => {
                if (Obj.name) {
                    subScriptionGroups.push(Obj.name);
                }
            });
        }

        return subScriptionGroups.join(",");
    };

    const getAllDatasetAndMetrics = (uiAlert: UiAlert | null): string => {
        const datasetMetrics: Array<string | number> = [];
        if (uiAlert) {
            forEach(uiAlert.datasetAndMetrics, (Obj) => {
                if (Obj.datasetName && Obj.metricName) {
                    datasetMetrics.push(`${Obj.datasetName}/${Obj.metricName}`);
                }
            });
        }

        return datasetMetrics.join(",");
    };

    const getDetectionTypes = (uiAlert: UiAlert | null): string => {
        let detectionTypes = "";
        if (uiAlert) {
            detectionTypes = uiAlert.detectionTypes.join(",");
        }

        return detectionTypes;
    };

    const getFilteredBy = (uiAlert: UiAlert | null): string => {
        let filteredData = "";
        if (uiAlert) {
            filteredData = uiAlert.filteredBy.join(",") || "-";
        }

        return filteredData;
    };

    if (!props.uiAlert) {
        return <NoDataIndicator />;
    }

    return (
        <PageContentsCardV1>
            <Grid container spacing={2} xs={12}>
                <Grid container item md={6} xs={12}>
                    {/* Created By */}
                    {props.uiAlert?.name && (
                        <Grid container item spacing={2} xs={12}>
                            <Grid item lg={3} sm={5} xs={6}>
                                <Typography
                                    className={classes.fontMedium}
                                    variant="body2"
                                >
                                    {t("label.created-by")}:
                                </Typography>
                            </Grid>
                            <Grid item lg={9} sm={7} xs={6}>
                                <Typography variant="body2">
                                    {props.uiAlert?.name || "-"}
                                </Typography>
                            </Grid>
                        </Grid>
                    )}

                    {/* Detection Type */}
                    <Grid container item xs={12}>
                        <Grid item lg={3} sm={5} xs={6}>
                            <Typography
                                className={classes.fontMedium}
                                variant="body2"
                            >
                                {t("label.detection-type")}:
                            </Typography>
                        </Grid>

                        <Grid item lg={9} sm={7} xs={6}>
                            <Typography variant="body2">
                                {getDetectionTypes(props.uiAlert) || "-"}
                            </Typography>
                        </Grid>
                    </Grid>

                    {/* Dataset/Metric */}
                    <Grid container item xs={12}>
                        <Grid item lg={3} sm={5} xs={6}>
                            <Typography
                                className={classes.fontMedium}
                                variant="body2"
                            >
                                {t("label.dataset-metric")}:
                            </Typography>
                        </Grid>
                        <Grid item lg={9} sm={7} xs={6}>
                            <Typography variant="body2">
                                {getAllDatasetAndMetrics(props.uiAlert) || "-"}
                            </Typography>
                        </Grid>
                    </Grid>
                </Grid>

                {/* second column */}
                <Grid container item md={6} xs={12}>
                    {/* Filtered By */}

                    <Grid container item xs={12}>
                        <Grid item lg={3} sm={5} xs={6}>
                            <Typography
                                className={classes.fontMedium}
                                variant="body2"
                            >
                                {t("label.filtered-by")}:
                            </Typography>
                        </Grid>

                        <Grid item lg={9} sm={7} xs={6}>
                            <Typography variant="body2">
                                {getFilteredBy(props.uiAlert) || "-"}
                            </Typography>
                        </Grid>
                    </Grid>
                    {/* Subscription Groups */}

                    <Grid container item xs={12}>
                        <Grid item lg={3} sm={5} xs={6}>
                            <Typography
                                className={classes.fontMedium}
                                variant="body2"
                            >
                                {t("label.subscription-groups")}:
                            </Typography>
                        </Grid>

                        <Grid item lg={9} sm={7} xs={6}>
                            <Typography variant="body2">
                                {getAllSubscriptionGroupsName(props.uiAlert) ||
                                    "-"}
                            </Typography>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </PageContentsCardV1>
    );
};
