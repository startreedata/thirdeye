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
import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { EventCard } from "../../components/entity-cards/event-card/event-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { Event } from "../../rest/dto/event.interfaces";
import { useGetEvent } from "../../rest/event/event.actions";
import { deleteEvent } from "../../rest/event/events.rest";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getEventsAllPath } from "../../utils/routes/routes.util";
import { EventsViewPageParams } from "./events-view-page.interface";

export const EventsViewPage: FunctionComponent = () => {
    const [uiEvent, setUiEvent] = useState<Event | null>(null);
    const { showDialog } = useDialogProviderV1();
    const { id: eventId } = useParams<EventsViewPageParams>();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const navigate = useNavigate();
    const {
        event,
        getEvent,
        status: eventRequestStatus,
        errorMessages: eventRequestErrors,
    } = useGetEvent();

    useEffect(() => {
        eventId && isValidNumberId(eventId) && getEvent(toNumber(eventId));
    }, [eventId]);

    useEffect(() => {
        !!event && setUiEvent(event);
    }, [event]);

    useEffect(() => {
        if (eventRequestStatus === ActionStatus.Error) {
            isEmpty(eventRequestErrors)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.event"),
                      })
                  )
                : eventRequestErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  );
        }
    }, [eventRequestStatus, eventRequestErrors]);

    const handleEventDelete = (event: Event): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: event.name,
            }),
            okButtonText: t("label.confirm"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleEventDeleteOk(event),
        });
    };

    const handleEventDeleteOk = (event: Event): void => {
        deleteEvent(event.id)
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", { entity: t("label.event") })
                );

                // Redirect to events all path
                navigate(getEventsAllPath());
            })
            .catch((error: AxiosError) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.delete-error", {
                        entity: t("label.event"),
                    })
                );
            });
    };

    return (
        <PageV1>
            <PageHeader showCreateButton title={uiEvent ? uiEvent.name : ""} />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {/* Event */}
                    <EventCard event={uiEvent} onDelete={handleEventDelete} />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
