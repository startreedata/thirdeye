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
import { Box, Button, Card, CardContent, Grid } from "@material-ui/core";
import { cloneDeep, isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useParams, useSearchParams } from "react-router-dom";
import { MetricRenderer } from "../../../components/anomalies-view/metric-renderer/metric-renderer.component";
import { Crumb } from "../../../components/breadcrumbs/breadcrumbs.interfaces";
import { AnomalyCard } from "../../../components/entity-cards/anomaly-card/anomaly-card.component";
import { anomaliesInvestigateBasicHelpCards } from "../../../components/help-drawer-v1/help-drawer-card-contents.utils";
import { HelpDrawerV1 } from "../../../components/help-drawer-v1/help-drawer-v1.component";
import { PageHeader } from "../../../components/page-header/page-header.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    useGetAlert,
    useGetAlertInsight,
} from "../../../rest/alerts/alerts.actions";
import { useGetAnomaly } from "../../../rest/anomalies/anomaly.actions";
import { EvaluatedTemplateMetadata } from "../../../rest/dto/alert.interfaces";
import {
    Investigation,
    SavedStateKeys,
} from "../../../rest/dto/rca.interfaces";
import { useGetEnumerationItem } from "../../../rest/enumeration-items/enumeration-items.actions";
import { useGetInvestigation } from "../../../rest/rca/rca.actions";
import {
    determineTimezoneFromAlertInEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../../utils/alerts/alerts.util";
import { generateNameForEnumerationItem } from "../../../utils/enumeration-items/enumeration-items.util";
import {
    createNewInvestigation,
    determineInvestigationIDFromSearchParams,
    INVESTIGATION_ID_QUERY_PARAM,
} from "../../../utils/investigation/investigation.util";
import { QUERY_PARAM_KEY_FOR_EXPANDED } from "../../../utils/params/params.util";
import {
    getAlertsAlertPath,
    getAnomaliesAnomalyViewPathV2,
} from "../../../utils/routes/routes.util";
import { RootCauseAnalysisForAnomalyPageParams } from "../../root-cause-analysis-for-anomaly-page/root-cause-analysis-for-anomaly-page.interfaces";

export const InvestigationStateTracker: FunctionComponent = () => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const { id: anomalyId } =
        useParams<RootCauseAnalysisForAnomalyPageParams>();

    const {
        anomaly,
        getAnomaly,
        status: getAnomalyStatus,
        errorMessages,
    } = useGetAnomaly();
    const { alert, getAlert, status: getAlertStatus } = useGetAlert();
    const {
        enumerationItem,
        getEnumerationItem,
        status: getEnumerationItemRequest,
    } = useGetEnumerationItem();
    const {
        getInvestigation,
        status: getInvestigationRequestStatus,
        errorMessages: getInvestigationRequestErrors,
    } = useGetInvestigation();
    const { alertInsight, getAlertInsight } = useGetAlertInsight();

    const [investigationId, setInvestigationId] = useState(
        determineInvestigationIDFromSearchParams(searchParams)
    );
    const [localInvestigation, setLocalInvestigation] =
        useState<Investigation | null>(null);
    const [investigationFromServer, setInvestigationFromServer] =
        useState<Investigation | null>(null);

    const pageTitle = useMemo(() => {
        let titleSoFar = localInvestigation?.name
            ? localInvestigation.name
            : t("label.investigation");

        if (!investigationId) {
            titleSoFar += ` (${t("label.not-saved")})`;
        }

        return titleSoFar;
    }, [investigationId, localInvestigation]);

    const breadcrumbs = useMemo(() => {
        const crumbs: Crumb[] = [];
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
                    ? `${alert?.name} (${enumerationItem.name})`
                    : alert?.name,
            });
        } else {
            crumbs.push({
                label: <SkeletonV1 width={50} />,
            });
        }

        crumbs.push({
            label: anomalyId,
            link: getAnomaliesAnomalyViewPathV2(Number(anomalyId)),
        });

        crumbs.push({
            label: t("label.investigate"),
        });

        return crumbs;
    }, [anomaly, enumerationItem, alert]);

    useEffect(() => {
        getAnomaly(Number(anomalyId));
    }, [anomalyId]);

    useEffect(() => {
        if (anomaly) {
            !!anomaly.enumerationItem &&
                getEnumerationItem(anomaly.enumerationItem.id);
            getAlert(anomaly.alert.id);
            getAlertInsight({ alertId: anomaly.alert.id });
        }
    }, [anomaly]);

    /**
     * If investigation's associated anomaly id does not match current anomalyId
     * then remove it from the search params to effectively disassociate it from
     * the UI
     */
    useEffect(() => {
        if (
            investigationFromServer &&
            investigationFromServer.anomaly &&
            investigationFromServer.anomaly.id
        ) {
            if (Number(anomalyId) !== investigationFromServer.anomaly.id) {
                searchParams.delete(INVESTIGATION_ID_QUERY_PARAM);
                setSearchParams(searchParams);
            }
        }
    }, [anomalyId, investigationFromServer]);

    useEffect(() => {
        setInvestigationId(
            determineInvestigationIDFromSearchParams(searchParams)
        );
        if (localInvestigation) {
            localInvestigation.uiMetadata[SavedStateKeys.QUERY_SEARCH_STRING] =
                searchParams.toString();
            handleInvestigationChange(localInvestigation);
        }
    }, [searchParams]);

    /**
     * Inform users there were issues when retrieving the investigation for the
     * associated investigation id
     */
    useEffect(() => {
        if (getInvestigationRequestStatus === ActionStatus.Error) {
            const genericMsg = t("message.error-while-fetching", {
                entity: t("label.saved-investigation"),
            });
            !isEmpty(getInvestigationRequestErrors)
                ? getInvestigationRequestErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, `${genericMsg}: ${msg}`)
                  )
                : notify(NotificationTypeV1.Error, genericMsg);
        }
    }, [getInvestigationRequestStatus, getInvestigationRequestErrors]);

    /**
     * Inform users there were issues fetching alert so we will have to default
     * to UTC
     */
    useEffect(() => {
        if (getAlertStatus === ActionStatus.Error) {
            notify(
                NotificationTypeV1.Error,
                t(
                    "message.experienced-issue-fetching-alert-information-for-anomaly"
                )
            );
        }
    }, [getAlertStatus]);

    useEffect(() => {
        if (investigationId) {
            getInvestigation(investigationId).then(
                (fetchedInvestigation: Investigation | undefined) => {
                    if (fetchedInvestigation) {
                        setInvestigationFromServer(fetchedInvestigation);
                        setLocalInvestigation(
                            cloneDeep<Investigation>(fetchedInvestigation)
                        );
                    } else {
                        setLocalInvestigation(
                            createNewInvestigation(Number(anomalyId))
                        );
                    }
                }
            );
        } else {
            setLocalInvestigation(createNewInvestigation(Number(anomalyId)));
            setInvestigationFromServer(null);
        }
    }, [investigationId]);

    const handleInvestigationChange = (
        modifiedInvestigation: Investigation
    ): void => {
        setLocalInvestigation(cloneDeep(modifiedInvestigation));
    };

    const handleServerUpdatedInvestigation = (
        updatedInvestigation: Investigation
    ): void => {
        searchParams.set(
            INVESTIGATION_ID_QUERY_PARAM,
            updatedInvestigation.id.toString()
        );
        setSearchParams(searchParams, { replace: true });
        setInvestigationFromServer(updatedInvestigation);
        setLocalInvestigation(cloneDeep(updatedInvestigation));
    };

    return (
        <PageV1>
            <LoadingErrorStateSwitch
                isError={false}
                /**
                 * Ensure localInvestigation is always so the children
                 * views can access a valid object
                 */
                isLoading={
                    localInvestigation === null ||
                    getInvestigationRequestStatus === ActionStatus.Working
                }
                loadingState={
                    <PageContentsGridV1>
                        <Grid item xs={12}>
                            <Card variant="outlined">
                                <CardContent>
                                    <Box padding={20}>
                                        <AppLoadingIndicatorV1 />
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grid>
                    </PageContentsGridV1>
                }
            >
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
                                        size="small"
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
                        </PageHeaderActionsV1>
                    }
                    subtitle={
                        anomaly && (
                            <MetricRenderer
                                alertData={alert}
                                anomaly={anomaly}
                            />
                        )
                    }
                >
                    <PageHeaderTextV1>{pageTitle}</PageHeaderTextV1>
                </PageHeader>

                <PageContentsGridV1>
                    {/* Anomaly Summary */}
                    <Grid item xs={12}>
                        <AnomalyCard
                            anomaly={anomaly}
                            hideTime={shouldHideTimeInDatetimeFormat({
                                metadata:
                                    alert?.templateProperties as EvaluatedTemplateMetadata,
                            })}
                            isLoading={
                                getAnomalyStatus === ActionStatus.Working ||
                                getAnomalyStatus === ActionStatus.Initial
                            }
                            timezone={determineTimezoneFromAlertInEvaluation({
                                metadata:
                                    alert?.templateProperties as EvaluatedTemplateMetadata,
                            })}
                        />
                    </Grid>

                    <Outlet
                        context={{
                            investigation: localInvestigation,
                            onInvestigationChange: handleInvestigationChange,
                            handleServerUpdatedInvestigation,
                            getEnumerationItemRequest,
                            enumerationItem,
                            anomaly,
                            getAnomalyRequestStatus: getAnomalyStatus,
                            anomalyRequestErrors: errorMessages,
                            alert,
                            alertInsight,
                        }}
                    />
                </PageContentsGridV1>
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
