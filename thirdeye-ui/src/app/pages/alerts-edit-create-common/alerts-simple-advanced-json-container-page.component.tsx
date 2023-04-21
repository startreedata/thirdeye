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
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Outlet } from "react-router-dom";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlertTemplates } from "../../rest/alert-templates/alert-templates.actions";
import { AlertTemplate as AlertTemplateType } from "../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { validateTemplateProperties } from "../../utils/alerts/alerts-configuration-validator.util";
import { handleAlertPropertyChangeGenerator } from "../../utils/anomalies/anomalies.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { AlertsSimpleAdvancedJsonContainerPageProps } from "./alerts-edit-create-common-page.interfaces";

export const AlertsSimpleAdvancedJsonContainerPage: FunctionComponent<AlertsSimpleAdvancedJsonContainerPageProps> =
    ({
        isEditRequestInFlight,
        startingAlertConfiguration,
        onSubmit,
        selectedSubscriptionGroups,
        onSubscriptionGroupChange,
        newSubscriptionGroup,
        onNewSubscriptionGroupChange,
        onPageExit,
    }) => {
        const { notify } = useNotificationProviderV1();
        const { t } = useTranslation();

        const {
            getAlertTemplates,
            status: alertTemplatesRequestStatus,
            errorMessages: getAlertTemplatesRequestErrors,
        } = useGetAlertTemplates();

        const [alert, setAlert] = useState<EditableAlert>(
            startingAlertConfiguration
        );
        const [selectedAlertTemplate, setSelectedAlertTemplate] =
            useState<AlertTemplateType | null>(null);
        const [alertTemplateOptions, setAlertTemplateOptions] = useState<
            AlertTemplateType[]
        >([]);

        useEffect(() => {
            setAlertTemplateOptions([]);
            // Upon successful response, find the matching alert template if it exists
            getAlertTemplates().then((alertTemplates) => {
                if (alertTemplates && alert.template && alert.template.name) {
                    const selectedAlertTemplateName = alert.template.name;
                    const alertTemplate = alertTemplates.find(
                        (candidate) =>
                            candidate.name === selectedAlertTemplateName
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
            notifyIfErrors(
                alertTemplatesRequestStatus,
                getAlertTemplatesRequestErrors,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.chart-data"),
                })
            );
        }, [getAlertTemplatesRequestErrors, alertTemplatesRequestStatus]);

        const handleAlertPropertyChange = useMemo(() => {
            return handleAlertPropertyChangeGenerator(
                setAlert,
                alertTemplateOptions,
                setSelectedAlertTemplate,
                t
            );
        }, [setAlert, alertTemplateOptions, setSelectedAlertTemplate]);

        const handleSubmitAlertClick = (
            alertToSubmit: EditableAlert,
            suggestedName: string
        ): void => {
            let isOk = true;

            if (alertToSubmit.name === "") {
                alertToSubmit.name = suggestedName;
            }

            if (selectedAlertTemplate?.properties) {
                const validationErrors = validateTemplateProperties(
                    selectedAlertTemplate.properties,
                    alertToSubmit.templateProperties,
                    t
                );

                validationErrors.forEach((error) => {
                    notify(NotificationTypeV1.Error, error.msg);
                });

                isOk = validationErrors.length === 0;
            }

            if (isOk) {
                onSubmit && onSubmit(alertToSubmit);
            }
        };

        return (
            <LoadingErrorStateSwitch
                wrapInCard
                wrapInGrid
                wrapInGridContainer
                isError={false}
                isLoading={
                    alertTemplatesRequestStatus === ActionStatus.Working ||
                    alertTemplatesRequestStatus === ActionStatus.Initial
                }
            >
                <Outlet
                    context={{
                        alert,
                        handleAlertPropertyChange,
                        selectedSubscriptionGroups,
                        handleSubscriptionGroupChange:
                            onSubscriptionGroupChange,
                        selectedAlertTemplate,
                        setSelectedAlertTemplate,
                        alertTemplateOptions,
                        handleSubmitAlertClick,
                        newSubscriptionGroup,
                        onNewSubscriptionGroupChange,
                        isEditRequestInFlight,
                        onPageExit,
                    }}
                />
            </LoadingErrorStateSwitch>
        );
    };
