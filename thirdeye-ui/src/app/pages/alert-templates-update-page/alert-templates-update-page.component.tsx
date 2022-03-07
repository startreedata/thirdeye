import { Grid } from "@material-ui/core";
import { assign } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { AlertTemplateWizard } from "../../components/alert-template-wizard/altert-template-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlertTemplate } from "../../rest/alert-templates/alert-templates.actions";
import { updateAlertTemplate } from "../../rest/alert-templates/alert-templates.rest";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import { getAlertTemplatesViewPath } from "../../utils/routes/routes.util";
import { AlertTemplatesUpdatePageParams } from "./alert-templates-update-page.interfaces";

export const AlertTemplatesUpdatePage: FunctionComponent = () => {
    const {
        alertTemplate,
        getAlertTemplate,
        status: alertTemplateRequestStatus,
    } = useGetAlertTemplate();
    const { id: alertTemplateId } = useParams<AlertTemplatesUpdatePageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        alertTemplateId && getAlertTemplate(Number(alertTemplateId));
    }, [alertTemplateId]);

    const onAlertWizardFinish = (
        modifiedAlertTemplate: AlertTemplate
    ): void => {
        if (!alert) {
            return;
        }

        modifiedAlertTemplate = assign(
            { ...modifiedAlertTemplate },
            { id: modifiedAlertTemplate?.id }
        );

        updateAlertTemplate(modifiedAlertTemplate)
            .then((alertTemplate: AlertTemplate): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.alert-template"),
                    })
                );
                alertTemplate.id &&
                    navigate(getAlertTemplatesViewPath(alertTemplate.id));

                return;
            })
            .catch((): void => {
                notify(
                    NotificationTypeV1.Error,
                    t("message.update-error", {
                        entity: t("label.alert-template"),
                    })
                );
            });
    };

    if (alertTemplateRequestStatus === ActionStatus.Working) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageV1>
            <PageHeader
                showTimeRange
                title={t("label.update-entity", {
                    entity: t("label.alert-template"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {alertTemplate && (
                        <AlertTemplateWizard
                            alertTemplate={alertTemplate}
                            onFinish={onAlertWizardFinish}
                        />
                    )}
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
