import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { EventListV1 } from "../../components/event-list-v1/event-list-v1.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../platform/rest/actions.interfaces";
import { Event } from "../../rest/dto/event.interfaces";
import { useGetEvents } from "../../rest/event/event.actions";
import { deleteEvent } from "../../rest/event/events.rest";
import { getErrorMessages } from "../../utils/rest/rest.util";

export const EventsAllPage: FunctionComponent = () => {
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const { getEvents, status, errorMessages, events } = useGetEvents();

    useEffect(() => {
        getEvents();
    }, []);

    useEffect(() => {
        if (status === ActionStatus.Error) {
            !isEmpty(errorMessages)
                ? errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.events"),
                      })
                  );
        }
    }, [status]);

    useEffect(() => {
        if (status === ActionStatus.Done && events && events.length === 0) {
            notify(
                NotificationTypeV1.Info,
                t("message.no-data-for-entity", {
                    entity: t("label.events"),
                })
            );
        }
    }, [status, events]);

    const handleEventDelete = (event: Event): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: event.name,
            }),
            okButtonText: t("label.delete"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleEventDeleteOk(event),
        });
    };

    const handleEventDeleteOk = (event: Event): void => {
        deleteEvent(event.id)
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", {
                        entity: t("label.event"),
                    })
                );

                // Refresh list
                getEvents();
            })
            .catch((error: AxiosError) => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.delete-error", {
                              entity: t("label.event"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={5} />
            <PageContentsGridV1 fullHeight>
                <EventListV1 events={events} onDelete={handleEventDelete} />
            </PageContentsGridV1>
        </PageV1>
    );
};
