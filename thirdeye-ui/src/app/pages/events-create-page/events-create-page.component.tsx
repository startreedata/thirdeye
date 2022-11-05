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
                const errMessages = getErrorMessages(error);

                notifyIfErrors(
                    ActionStatus.Error,
                    errMessages,
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
