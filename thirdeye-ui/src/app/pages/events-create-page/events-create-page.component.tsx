/*
 * Copyright 2022 StarTree Inc
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
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { EventsWizard } from "../../components/event-wizard/event-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { EditableEvent, Event } from "../../rest/dto/event.interfaces";
import { createEvent } from "../../rest/event/events.rest";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getEventsAllPath,
    getEventsViewPath,
} from "../../utils/routes/routes.util";

export const EventsCreatePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const onEventWizardFinish = (event: Event | EditableEvent): void => {
        if (!event) {
            return;
        }

        createEvent(event)
            .then((event: Event): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", {
                        entity: t("label.event"),
                    })
                );
                // Redirect to event detail path
                navigate(getEventsViewPath(event.id));
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.create-error", {
                        entity: t("label.event"),
                    })
                );
            });
    };

    return (
        <PageV1>
            <PageHeader
                title={t("label.create-entity", {
                    entity: t("label.event"),
                })}
            />
            <EventsWizard
                showCancel
                onCancel={() => navigate(getEventsAllPath())}
                onSubmit={onEventWizardFinish}
            />
        </PageV1>
    );
};
