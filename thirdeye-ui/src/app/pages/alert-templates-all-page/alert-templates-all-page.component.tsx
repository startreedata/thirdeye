import { Box, Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertTemplateListV1 } from "../../components/alert-template-list-v1/alert-template-list-v1.component";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlertTemplates } from "../../rest/alert-templates/alert-templates.actions";
import {
    deleteAlertTemplate,
    updateAlertTemplate,
} from "../../rest/alert-templates/alert-templates.rest";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import {
    makeDeleteRequest,
    promptDeleteConfirmation,
} from "../../utils/bulk-delete/bulk-delete.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getAlertTemplatesCreatePath } from "../../utils/routes/routes.util";

export const AlertTemplatesAllPage: FunctionComponent = () => {
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [alertTemplates, setAlertTemplate] = useState<AlertTemplate[]>([]);
    const {
        getAlertTemplates,
        status: alertTemplatesRequestStatus,
        errorMessages,
    } = useGetAlertTemplates();

    useEffect(() => {
        getAlertTemplates().then((data) => {
            data && setAlertTemplate(data);
        });
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

    const handleAlertTemplateDelete = (
        alertTemplatesToDelete: AlertTemplate[]
    ): void => {
        promptDeleteConfirmation(
            alertTemplatesToDelete,
            () => {
                alertTemplates &&
                    makeDeleteRequest(
                        alertTemplatesToDelete,
                        deleteAlertTemplate,
                        t,
                        notify,
                        t("label.alert-template"),
                        t("label.alert-templates")
                    ).then((deleted) => {
                        setAlertTemplate(() => {
                            return [...alertTemplates].filter((candidate) => {
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
            t("label.alert-templates")
        );
    };

    useEffect(() => {
        notifyIfErrors(
            alertTemplatesRequestStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alert-templates"),
            })
        );
    }, [alertTemplatesRequestStatus, errorMessages]);

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={3} />

            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch
                    isError={alertTemplatesRequestStatus == ActionStatus.Error}
                    isLoading={
                        alertTemplatesRequestStatus == ActionStatus.Working
                    }
                >
                    <EmptyStateSwitch
                        emptyState={
                            <Grid item xs={12}>
                                <PageContentsCardV1>
                                    <Box padding={20}>
                                        <NoDataIndicator>
                                            <Box textAlign="center">
                                                {t(
                                                    "message.no-entity-created",
                                                    {
                                                        entity: t(
                                                            "label.alert-templates"
                                                        ),
                                                    }
                                                )}
                                            </Box>
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
                                                <Button
                                                    color="primary"
                                                    href={getAlertTemplatesCreatePath()}
                                                >
                                                    {t("label.create-entity", {
                                                        entity: t(
                                                            "label.alert-template"
                                                        ),
                                                    })}
                                                </Button>
                                            </Box>
                                        </NoDataIndicator>
                                    </Box>
                                </PageContentsCardV1>
                            </Grid>
                        }
                        isEmpty={
                            !!alertTemplates && alertTemplates.length === 0
                        }
                    >
                        <AlertTemplateListV1
                            alertTemplates={alertTemplates}
                            onChange={handleAlertTemplateChange}
                            onDelete={handleAlertTemplateDelete}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
