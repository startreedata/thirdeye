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
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { EventListV1 } from "../../components/event-list-v1/event-list-v1.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { Event } from "../../rest/dto/event.interfaces";
import { useGetEvents } from "../../rest/event/event.actions";
import { deleteEvent } from "../../rest/event/events.rest";
import {
    makeDeleteRequest,
    promptDeleteConfirmation,
} from "../../utils/bulk-delete/bulk-delete.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";

export const EventsAllPage: FunctionComponent = () => {
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const [events, setEvents] = useState<Event[]>([]);
    const { getEvents, status, errorMessages } = useGetEvents();

    const [searchParams] = useSearchParams();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );

    useEffect(() => {
        // Refetch events on update of start / end time
        getEvents({ startTime, endTime }).then((data) => {
            data && setEvents(data);
        });
    }, [startTime, endTime]);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.events"),
            })
        );
    }, [status]);

    const handleEventDelete = (eventsToDelete: Event[]): void => {
        promptDeleteConfirmation(
            eventsToDelete,
            () => {
                events &&
                    makeDeleteRequest(
                        eventsToDelete,
                        deleteEvent,
                        t,
                        notify,
                        t("label.event"),
                        t("label.events")
                    ).then((deleted) => {
                        setEvents(() => {
                            return [...events].filter((candidate) => {
                                return (
                                    deleted.findIndex(
                                        (d) => d.id === candidate.id
                                    ) === -1
                                );
                            });
                        });
                    });
            },
            t,
            showDialog,
            t("label.events")
        );
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
