import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { EventsWizard } from "../../components/events-wizard/events-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { Event } from "../../rest/dto/event.interfaces";
import { createEvent } from "../../rest/event/events.rest";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getEventsViewPath } from "../../utils/routes/routes.util";

export const EventsCreatePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const onEventWizardFinish = (event: Event): void => {
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

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.create-error", {
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
            <PageHeader
                title={t("label.create-entity", {
                    entity: t("label.event"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <EventsWizard onFinish={onEventWizardFinish} />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
