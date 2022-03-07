import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { AlertTemplateWizard } from "../../components/alert-template-wizard/altert-template-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { createAlertTemplate } from "../../rest/alert-templates/alert-templates.rest";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import { getAlertTemplatesViewPath } from "../../utils/routes/routes.util";

export const AlertTemplatesCreatePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const onAlertTemplateWizardFinish = (
        alertTemplate: AlertTemplate
    ): void => {
        if (!alertTemplate) {
            return;
        }

        createAlertTemplate(alertTemplate)
            .then((alertTemplate: AlertTemplate): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", {
                        entity: t("label.alert-template"),
                    })
                );
                // Redirect to alert template view path
                alertTemplate.id &&
                    navigate(getAlertTemplatesViewPath(alertTemplate.id));
            })
            .catch((): void => {
                notify(
                    NotificationTypeV1.Error,
                    t("message.create-error", {
                        entity: t("label.alert-template"),
                    })
                );
            });
    };

    return (
        <PageV1>
            <PageHeader
                title={t("label.create-entity", {
                    entity: t("label.alert-template"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <AlertTemplateWizard
                        onFinish={onAlertTemplateWizardFinish}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
