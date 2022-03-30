import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { AlertTemplateListV1 } from "../../components/alert-template-list-v1/alert-template-list-v1.component";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../platform/rest/actions.interfaces";
import { useGetAlertTemplates } from "../../rest/alert-templates/alert-templates.actions";
import {
    deleteAlertTemplate,
    updateAlertTemplate,
} from "../../rest/alert-templates/alert-templates.rest";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import { getErrorMessages } from "../../utils/rest/rest.util";

export const AlertTemplatesAllPage: FunctionComponent = () => {
    const { showDialog } = useDialog();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const {
        alertTemplates,
        getAlertTemplates,
        status: alertTemplatesRequestStatus,
        errorMessages,
    } = useGetAlertTemplates();

    useEffect(() => {
        getAlertTemplates();
    }, []);

    const handleAlertTemplateChange = (
        alertTemplateToChange: AlertTemplate
    ): void => {
        if (!alertTemplateToChange) {
            return;
        }

        updateAlertTemplate(alertTemplateToChange).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", {
                    entity: t("label.alert-template"),
                })
            );

            // Refresh list
            getAlertTemplates();
        });
    };

    const handleAlertTemplateDelete = (alertTemplate: AlertTemplate): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: alertTemplate.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleAlertTemplateDeleteOk(alertTemplate),
        });
    };

    const handleAlertTemplateDeleteOk = (
        alertTemplate: AlertTemplate
    ): void => {
        deleteAlertTemplate(alertTemplate.id)
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", {
                        entity: t("label.alert-template"),
                    })
                );

                // Refresh list
                getAlertTemplates();
            })
            .catch((errors) => {
                const errorMessages = getErrorMessages(errors as AxiosError);

                isEmpty(errorMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.delete-error", {
                              entity: t("label.alert-template"),
                          })
                      )
                    : errorMessages.map((msg) =>
                          notify(NotificationTypeV1.Error, msg)
                      );
            });
    };

    useEffect(() => {
        if (alertTemplatesRequestStatus === ActionStatus.Error) {
            isEmpty(errorMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.alert-templates"),
                      })
                  )
                : errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  );
        }
    }, [alertTemplatesRequestStatus, errorMessages]);

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={3} />

            <PageContentsGridV1 fullHeight>
                {/* Alert Template list */}
                <AlertTemplateListV1
                    alertTemplates={alertTemplates}
                    onChange={handleAlertTemplateChange}
                    onDelete={handleAlertTemplateDelete}
                />
            </PageContentsGridV1>
        </PageV1>
    );
};
