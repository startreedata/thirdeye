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
import { UiEvent } from "../../rest/dto/ui-event.interfaces";
import { deleteEvent, getEvent } from "../../rest/event/events.rest";
import { getUiEvent } from "../../utils/events/events.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getEventsAllPath } from "../../utils/routes/routes.util";
import { EventsViewPageParams } from "./events-view-page.interface";

export const EventsViewPage: FunctionComponent = () => {
    const [uiEvent, setUiEvent] = useState<UiEvent | null>(null);
    const { showDialog } = useDialogProviderV1();
    const params = useParams<EventsViewPageParams>();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const navigate = useNavigate();

    useEffect(() => {
        fetchEvent();
    }, []);

    const fetchEvent = (): void => {
        setUiEvent(null);
        let fetchedUiEvent = {} as UiEvent;

        if (params.id && !isValidNumberId(params.id)) {
            // Invalid id
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.event"),
                    id: params.id,
                })
            );

            setUiEvent(fetchedUiEvent);

            return;
        }

        getEvent(toNumber(params.id))
            .then((event) => {
                fetchedUiEvent = getUiEvent(event);
            })
            .catch((error: AxiosError) => {
                const errMessages = getErrorMessages(error);
                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.error-while-fetching", {
                              entity: t("label.event"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            })
            .finally(() => setUiEvent(fetchedUiEvent));
    };

    const handleEventDelete = (uiEvent: UiEvent): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiEvent.name,
            }),
            okButtonText: t("label.delete"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleEventDeleteOk(uiEvent),
        });
    };

    const handleEventDeleteOk = (uiEvent: UiEvent): void => {
        deleteEvent(uiEvent.id)
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", { entity: t("label.event") })
                );

                // Redirect to events all path
                navigate(getEventsAllPath());
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
