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
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { ActionStatus } from "@startree-ui/platform-ui/assets/rest/actions.interfaces";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { EventListV1 } from "../../components/event-list-v1/event-list-v1.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { Event } from "../../rest/dto/event.interfaces";
import { useGetEvents } from "../../rest/event/event.actions";
import { deleteEvent } from "../../rest/event/events.rest";
import { getErrorMessages } from "../../utils/rest/rest.util";

export const EventsAllPage: FunctionComponent = () => {
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const { getEvents, status, errorMessages, events } = useGetEvents();

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
        getEvents({ startTime, endTime });
    }, [startTime, endTime]);

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
