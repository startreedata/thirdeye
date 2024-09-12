/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Icon } from "@iconify/react";
import { Box, Button, Card, CardContent, Grid, Link } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    Link as RouterLink,
    Outlet,
    useNavigate,
    useOutletContext,
} from "react-router-dom";
import { FeedbackCard } from "../../components/anomalies-view/feedback-card/feedback-card.component";
import { FeedbackCollector } from "../../components/anomalies-view/feedback-collector/feedback-collector.component";
import { MetricRenderer } from "../../components/anomalies-view/metric-renderer/metric-renderer.component";
import { Crumb } from "../../components/breadcrumbs/breadcrumbs.interfaces";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { anomaliesInvestigateBasicHelpCards } from "../../components/help-drawer-v1/help-drawer-card-contents.utils";
import { HelpDrawerV1 } from "../../components/help-drawer-v1/help-drawer-v1.component";
import { InvestigationsList } from "../../components/investigations-list-v2/investigations-list.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageV1,
    SkeletonV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { Anomaly, AnomalyFeedback } from "../../rest/dto/anomaly.interfaces";
import {
    determineTimezoneFromAlertInEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../utils/alerts/alerts.util";
import {
    getAnomalyName,
    isAnomalyIgnored,
} from "../../utils/anomalies/anomalies.util";
import { generateNameForEnumerationItem } from "../../utils/enumeration-items/enumeration-items.util";
import { QUERY_PARAM_KEY_FOR_EXPANDED } from "../../utils/params/params.util";
import {
    getAlertsAlertPath,
    getAnomaliesAllPath,
    getAnomaliesAnomalyViewPathV1,
} from "../../utils/routes/routes.util";
import { AnomalyViewContainerPageOutletContext } from "./anomalies-view-page.interfaces";
import { useAnomaliesViewPageStyles } from "./anomalies-view-page.styles";

export const AnomaliesViewInformationPage: FunctionComponent = () => {
    const { showDialog } = useDialogProviderV1();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const style = useAnomaliesViewPageStyles();

    const containerContext =
        useOutletContext<AnomalyViewContainerPageOutletContext>();
    const {
        alertInsight,
        anomaly,
        handleFeedbackUpdateSuccess,
        enumerationItem,
        getInvestigationsRequestStatus,
        investigations,
    } = containerContext;

    const [showV1Link, setShowV1Link] = useState(true);
    const [feedback, setFeedback] = useState<AnomalyFeedback | undefined>(
        anomaly?.feedback
    );

    const handleAnomalyDelete = (): void => {
        if (!anomaly) {
            return;
        }
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: getAnomalyName(anomaly),
            }),
            okButtonText: t("label.confirm"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleAnomalyDeleteOk(anomaly),
        });
    };

    const handleAnomalyDeleteOk = (anomalyToDelete: Anomaly): void => {
        deleteAnomaly(anomalyToDelete.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", {
                    entity: t("label.anomaly"),
                })
            );

            // Redirect to anomalies all path
            navigate(getAnomaliesAllPath());
        });
    };

    const getSubtitle = (): string | undefined => {
        if (anomaly && isAnomalyIgnored(anomaly)) {
            return t(
                "message.this-anomaly-was-ignored-due-to-the-system-sensibility"
            );
        }

        return undefined;
    };

    const breadcrumbs = useMemo(() => {
        const crumbs: Crumb[] = [
            {
                link: getAnomaliesAllPath(),
                label: t("label.anomalies"),
            },
        ];
        const enumerationItemSearchParams = enumerationItem
            ? new URLSearchParams([
                  [
                      QUERY_PARAM_KEY_FOR_EXPANDED,
                      generateNameForEnumerationItem(enumerationItem),
                  ],
              ])
            : undefined;

        if (anomaly) {
            crumbs.push({
                link: getAlertsAlertPath(
                    anomaly.alert.id,
                    enumerationItemSearchParams
                ),
                label: enumerationItem
                    ? `${anomaly.alert.name} (${enumerationItem.name})`
                    : anomaly.alert.name,
            });
        } else {
            crumbs.push({
                label: <SkeletonV1 width={50} />,
            });
        }

        crumbs.push({
            label: anomaly?.id,
        });

        return crumbs;
    }, [anomaly, enumerationItem]);

    const getPageTitle = (): string => {
        let pageTitle = getAnomalyName(anomaly);

        if (isAnomalyIgnored(anomaly)) {
            pageTitle += ` (${t("label.ignored")})`;
        }

        return pageTitle;
    };

    const handleFeedbackChange = (feedback: AnomalyFeedback): void => {
        setFeedback(feedback);
        handleFeedbackUpdateSuccess(feedback);
    };

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                breadcrumbs={breadcrumbs}
                customActions={
                    <PageHeaderActionsV1>
                        <HelpDrawerV1
                            cards={anomaliesInvestigateBasicHelpCards}
                            title={`${t("label.need-help")}?`}
                            trigger={(handleOpen) => (
                                <Button
                                    color="primary"
                                    component="button"
                                    variant="outlined"
                                    onClick={handleOpen}
                                >
                                    <Box component="span" mr={1}>
                                        {t("label.need-help")}
                                    </Box>
                                    <Box component="span" display="flex">
                                        <Icon
                                            fontSize={24}
                                            icon="mdi:question-mark-circle-outline"
                                        />
                                    </Box>
                                </Button>
                            )}
                        />
                        <Button
                            component="button"
                            variant="contained"
                            onClick={handleAnomalyDelete}
                        >
                            {t("label.delete")}
                        </Button>
                    </PageHeaderActionsV1>
                }
                subtitle={getSubtitle()}
            >
                <PageHeaderTextV1>{getPageTitle()}</PageHeaderTextV1>
                <MetricRenderer anomaly={anomaly} />
            </PageHeader>

            <PageContentsGridV1>
                {showV1Link && (
                    <Grid item xs={12}>
                        <Alert
                            severity="info"
                            variant="outlined"
                            onClose={() => setShowV1Link(false)}
                        >
                            {t(
                                "message.go-back-to-the-old-version-by-clicking"
                            )}
                            <Link
                                component={RouterLink}
                                to={getAnomaliesAnomalyViewPathV1(anomaly.id)}
                            >
                                {t("label.here")}
                            </Link>
                        </Alert>
                    </Grid>
                )}
                <Grid
                    item
                    hidden={isEmpty(feedback) && isEmpty(investigations)}
                    xs={12}
                >
                    <Card>
                        <CardContent>
                            <Grid container>
                                <Grid item hidden={isEmpty(feedback)} xs={12}>
                                    <FeedbackCard
                                        anomalyId={anomaly.id}
                                        feedback={feedback}
                                        onFeedbackUpdate={handleFeedbackChange}
                                    />
                                </Grid>
                                <Grid
                                    item
                                    hidden={isEmpty(investigations)}
                                    xs={12}
                                >
                                    <InvestigationsList
                                        anomalyId={anomaly.id}
                                        getInvestigationsRequestStatus={
                                            getInvestigationsRequestStatus
                                        }
                                        investigations={investigations}
                                    />
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12}>
                    <AnomalyCard
                        anomaly={anomaly}
                        className={style.fullHeight}
                        hideTime={shouldHideTimeInDatetimeFormat(
                            alertInsight?.templateWithProperties
                        )}
                        isLoading={false}
                        timezone={determineTimezoneFromAlertInEvaluation(
                            alertInsight?.templateWithProperties
                        )}
                    />
                </Grid>
                {!feedback && (
                    <Grid item xs={12}>
                        <FeedbackCollector
                            anomaly={anomaly as Anomaly}
                            onFeedbackUpdate={handleFeedbackChange}
                        />
                    </Grid>
                )}

                <Outlet context={containerContext} />
            </PageContentsGridV1>
        </PageV1>
    );
};
