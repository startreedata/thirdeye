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
import { AxiosError } from "axios";
import { assign, isEmpty } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { AlertTemplateWizard } from "../../components/alert-template-wizard/altert-template-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    NotificationTypeV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlertTemplate } from "../../rest/alert-templates/alert-templates.actions";
import { updateAlertTemplate } from "../../rest/alert-templates/alert-templates.rest";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getAlertTemplatesViewPath } from "../../utils/routes/routes.util";
import { AlertTemplatesUpdatePageParams } from "./alert-templates-update-page.interfaces";

export const AlertTemplatesUpdatePage: FunctionComponent = () => {
    const {
        alertTemplate,
        getAlertTemplate,
        status: alertTemplateRequestStatus,
        errorMessages: alertTemplateErrors,
    } = useGetAlertTemplate();
    const { id: alertTemplateId } = useParams<AlertTemplatesUpdatePageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        alertTemplateId && getAlertTemplate(Number(alertTemplateId));
    }, [alertTemplateId]);

    const onAlertWizardFinish = (
        modifiedAlertTemplate: AlertTemplate
    ): void => {
        if (!alert) {
            return;
        }

        modifiedAlertTemplate = assign(
            { ...modifiedAlertTemplate },
            { id: modifiedAlertTemplate.id }
        );

        updateAlertTemplate(modifiedAlertTemplate)
            .then((alertTemplate: AlertTemplate): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.alert-template"),
                    })
                );
                alertTemplate.id &&
                    navigate(getAlertTemplatesViewPath(alertTemplate.id));

                return;
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.update-error", {
                        entity: t("label.alert-template"),
                    })
                );
            });
    };

    useEffect(() => {
        if (alertTemplateRequestStatus === ActionStatus.Error) {
            isEmpty(alertTemplateErrors)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.alert-template"),
                      })
                  )
                : alertTemplateErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg.message, msg.details)
                  );
        }
    }, [alertTemplateRequestStatus, alertTemplateErrors]);

    return (
        <PageV1>
            <PageHeader
                showTimeRange
                title={t("label.update-entity", {
                    entity: t("label.alert-template"),
                })}
            />
            <LoadingErrorStateSwitch
                wrapInCard
                wrapInGrid
                wrapInGridContainer
                isError={alertTemplateRequestStatus === ActionStatus.Error}
                isLoading={
                    alertTemplateRequestStatus === ActionStatus.Working ||
                    alertTemplateRequestStatus === ActionStatus.Initial
                }
            >
                {alertTemplate && (
                    <AlertTemplateWizard<AlertTemplate>
                        startingAlertTemplate={alertTemplate}
                        onFinish={onAlertWizardFinish}
                    />
                )}
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
