/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Button, ButtonGroup, Grid, Typography } from "@material-ui/core";
import { isEmpty, isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NavLink,
    Outlet,
    useLocation,
    useNavigate,
    useSearchParams,
} from "react-router-dom";
import { createNewStartingAlert } from "../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    HelpLinkIconV1,
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageV1,
    TooltipV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlertTemplates } from "../../rest/alert-templates/alert-templates.actions";
import { AlertTemplate as AlertTemplateType } from "../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { handleAlertPropertyChangeGenerator } from "../../utils/anomalies/anomalies.util";
import { THIRDEYE_DOC_LINK } from "../../utils/constants/constants.util";
import {
    AppRouteRelative,
    getAlertsAllPath,
} from "../../utils/routes/routes.util";
import { AlertsEditPageProps } from "./alerts-update-page.interfaces";

export const AlertsEditBasePage: FunctionComponent<AlertsEditPageProps> = ({
    startingAlertConfiguration,
    pageTitle,
    onSubmit,
    submitButtonLabel,
    selectedSubscriptionGroups,
    onSubscriptionGroupChange,
}) => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const location = useLocation();
    const { notify } = useNotificationProviderV1();
    const [alert, setAlert] = useState<EditableAlert>(
        startingAlertConfiguration
    );
    const [selectedAlertTemplate, setSelectedAlertTemplate] =
        useState<AlertTemplateType | null>(null);
    const [alertTemplateOptions, setAlertTemplateOptions] = useState<
        AlertTemplateType[]
    >([]);
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();

    const {
        getAlertTemplates,
        status: alertTemplatesRequestStatus,
        errorMessages: getAlertTemplatesRequestErrors,
    } = useGetAlertTemplates();

    useEffect(() => {
        setAlertTemplateOptions([]);
        // Upon successful response, find the matching alert template if it exists
        getAlertTemplates().then((alertTemplates) => {
            if (alertTemplates && alert.template && alert.template.name) {
                const selectedAlertTemplateName = alert.template.name;
                const alertTemplate = alertTemplates.find(
                    (candidate) => candidate.name === selectedAlertTemplateName
                );
                alertTemplate && setSelectedAlertTemplate(alertTemplate);
            }
            if (alertTemplates) {
                setAlertTemplateOptions([
                    ...alertTemplates,
                    { id: -1, name: "link", description: "-1" },
                ]);
            }
        });
    }, []);

    useEffect(() => {
        if (alertTemplatesRequestStatus === ActionStatus.Error) {
            !isEmpty(getAlertTemplatesRequestErrors)
                ? getAlertTemplatesRequestErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.chart-data"),
                      })
                  );
        }
    }, [getAlertTemplatesRequestErrors, alertTemplatesRequestStatus]);

    const handleAlertPropertyChange = useMemo(() => {
        return handleAlertPropertyChangeGenerator(
            setAlert,
            alertTemplateOptions,
            setSelectedAlertTemplate,
            t
        );
    }, [setAlert, alertTemplateOptions, setSelectedAlertTemplate]);

    const handleSubscriptionGroupChange = (
        updatedGroups: SubscriptionGroup[]
    ): void => {
        onSubscriptionGroupChange(updatedGroups);
    };

    const handleSubmitAlertClick = (): void => {
        onSubmit && onSubmit(alert);
    };

    /**
     * Prompt the user if they are sure they want to leave
     */
    const handlePageExitChecks = (): void => {
        // If user has not input anything navigate to all alerts page
        if (isEqual(alert, createNewStartingAlert())) {
            navigate(getAlertsAllPath());
        } else {
            showDialog({
                type: DialogType.ALERT,
                headerText: t("message.redirected-to-another-page"),
                contents: (
                    <>
                        <Typography variant="body1">
                            <strong>
                                {t("message.do-you-want-to-leave-this-page")}
                            </strong>
                        </Typography>
                        <ul>
                            <li>{t("message.your-changes-wont-save")}</li>
                        </ul>
                    </>
                ),
                okButtonText: t("label.yes-leave-page"),
                cancelButtonText: t("label.no-stay"),
                onOk: () => {
                    navigate(getAlertsAllPath());
                },
            });
        }
    };

    return (
        <PageV1>
            <PageHeader
                customActions={
                    <PageHeaderActionsV1>
                        <Box padding={1}>View:</Box>
                        <ButtonGroup color="primary" variant="outlined">
                            <Button
                                component={NavLink}
                                to={{
                                    pathname:
                                        AppRouteRelative.ALERTS_CREATE_SIMPLE,
                                    search: searchParams.toString(),
                                }}
                                variant={
                                    location.pathname.includes("simple")
                                        ? "contained"
                                        : "outlined"
                                }
                            >
                                Simple
                            </Button>
                            <Button
                                component={NavLink}
                                to={{
                                    pathname:
                                        AppRouteRelative.ALERTS_CREATE_ADVANCED,
                                    search: searchParams.toString(),
                                }}
                                variant={
                                    location.pathname.includes("advanced")
                                        ? "contained"
                                        : "outlined"
                                }
                            >
                                Advanced
                            </Button>
                        </ButtonGroup>
                    </PageHeaderActionsV1>
                }
            >
                <PageHeaderTextV1>
                    {pageTitle}
                    <TooltipV1
                        placement="top"
                        title={t("label.view-configuration-docs") as string}
                    >
                        <span>
                            <HelpLinkIconV1
                                displayInline
                                enablePadding
                                externalLink
                                href={`${THIRDEYE_DOC_LINK}/getting-started/create-your-first-alert`}
                            />
                        </span>
                    </TooltipV1>
                </PageHeaderTextV1>
            </PageHeader>
            <PageContentsGridV1>
                <Outlet
                    context={[
                        alert,
                        handleAlertPropertyChange,
                        selectedSubscriptionGroups,
                        handleSubscriptionGroupChange,
                        selectedAlertTemplate,
                        setSelectedAlertTemplate,
                        alertTemplateOptions,
                    ]}
                />
            </PageContentsGridV1>

            <Box width="100%">
                <PageContentsCardV1>
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Button
                                color="secondary"
                                onClick={handlePageExitChecks}
                            >
                                {t("label.cancel")}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Button
                                color="primary"
                                onClick={handleSubmitAlertClick}
                            >
                                {submitButtonLabel}
                            </Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Box>
        </PageV1>
    );
};
