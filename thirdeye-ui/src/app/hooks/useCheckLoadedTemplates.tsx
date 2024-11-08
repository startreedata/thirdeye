/*
 * Copyright 2024 StarTree Inc
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
import { useEffect } from "react";
import { useCreateDefaultAlertTemplates } from "../rest/alert-templates/alert-templates.actions";
import { isEmpty } from "lodash";
import { notifyIfErrors } from "../utils/notifications/notifications.util";
import { useNotificationProviderV1 } from "../platform/components";
import { useTranslation } from "react-i18next";
import { ActionStatus } from "../rest/actions.interfaces";
import { AlertTemplate } from "../rest/dto/alert-template.interfaces";

type CheckLoadedTemplatesProps = {
    alertTemplates: AlertTemplate[] | null;
    alertTemplatesRequestStatus: ActionStatus;
};

export const useCheckLoadedTemplates = ({
    alertTemplates,
    alertTemplatesRequestStatus,
}: CheckLoadedTemplatesProps): void => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const {
        createDefaultAlertTemplates,
        status: createDefaultAlertTemplatesStatus,
        errorMessages: createDefaultAlertTemplatesErrors,
    } = useCreateDefaultAlertTemplates();

    useEffect(() => {
        if (
            alertTemplatesRequestStatus === ActionStatus.Done &&
            isEmpty(alertTemplates)
        ) {
            createDefaultAlertTemplates();
        }
    }, [alertTemplates, alertTemplatesRequestStatus]);

    useEffect(() => {
        notifyIfErrors(
            createDefaultAlertTemplatesStatus,
            createDefaultAlertTemplatesErrors,
            notify,
            t("errors.load-alert-templates")
        );
    }, [createDefaultAlertTemplatesStatus, createDefaultAlertTemplatesErrors]);
};
