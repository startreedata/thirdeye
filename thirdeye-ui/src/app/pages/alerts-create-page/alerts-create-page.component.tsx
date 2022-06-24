import { Box, Button, ButtonGroup } from "@material-ui/core";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NavLink,
    Outlet,
    useNavigate,
    useSearchParams,
} from "react-router-dom";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlertTemplates } from "../../rest/alert-templates/alert-templates.actions";
import { createAlert } from "../../rest/alerts/alerts.rest";
import { AlertTemplate as AlertTemplateType } from "../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { updateSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { AppRoute, getAlertsViewPath } from "../../utils/routes/routes.util";
import { useAlertCreatePageStyles } from "./alert-create-page-component.styles";
import { validateConfiguration } from "./alerts-create-advance-page/alerts-create-advance-page.util";
import { CreateAlertConfigurationSection } from "./alerts-create-page.interfaces";

// 6AM Everyday
const DEFAULT_CRON = "0 0 0/2 1/1 * ? *";

export const AlertsCreatePage: FunctionComponent = () => {
    const classes = useAlertCreatePageStyles();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { notify } = useNotificationProviderV1();
    const [alert, setAlert] = useState<EditableAlert>({
        name: "",
        description: "",
        cron: DEFAULT_CRON,
        templateProperties: {
            dataSource: "pinot",
            dataset: "TrialTaxiData",
            timeColumn: "trip_timestamp",
            timeColumnFormat: "1:MILLISECONDS:EPOCH",
            aggregationFunction: "sum",
            aggregationColumn: "total_amount",
            seasonalityPeriod: "P7D",
            lookback: "P60D",
            monitoringGranularity: "P1D",
            sensitivity: "3",
        },
    });
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const [selectedAlertTemplate, setSelectedAlertTemplate] =
        useState<AlertTemplateType | null>(null);
    const [alertTemplateOptions, setAlertTemplateOptions] = useState<
        AlertTemplateType[]
    >([]);
    const [isAlertValid, setIsAlertValid] = useState(false);
    const [validationEntries, setValidationEntries] = useState<{
        [key: string]: boolean;
    }>({});
    const { t } = useTranslation();

    const {
        getAlertTemplates,
        status: alertTemplatesRequestStatus,
        errorMessages: getAlertTemplatesRequestErrors,
    } = useGetAlertTemplates();

    useEffect(() => {
        if (selectedAlertTemplate) {
            validateConfiguration(
                alert,
                selectedAlertTemplate,
                handleValidationEntryChange
            );
        } else {
            handleValidationEntryChange(
                CreateAlertConfigurationSection.TEMPLATE_PROPERTIES,
                false
            );
        }
    }, [selectedAlertTemplate, alert]);

    useEffect(() => {
        setAlertTemplateOptions([]);
        // Upon successful response, find the matching alert template if it exists
        getAlertTemplates().then((alertTemplates) => {
            if (alertTemplates && alert.template && alert.template.name) {
                const selectedAlertTemplateName = alert.template.name;
                const alertTemplate = alertTemplates.find(
                    (candidate) => candidate.name === selectedAlertTemplateName
                );
                alertTemplate && setSelectedAlertTemplate(alertTemplate);
            }
            if (alertTemplates) {
                setAlertTemplateOptions([
                    ...alertTemplates,
                    { id: -1, name: "link", description: "-1" },
                ]);
            }
        });
    }, []);

    useEffect(() => {
        if (alertTemplatesRequestStatus === ActionStatus.Error) {
            !isEmpty(getAlertTemplatesRequestErrors)
                ? getAlertTemplatesRequestErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.chart-data"),
                      })
                  );
        }
    }, [getAlertTemplatesRequestErrors, alertTemplatesRequestStatus]);

    useEffect(() => {
        setIsAlertValid(
            Object.values(validationEntries).every((isValid) => isValid)
        );
    }, [validationEntries]);

    const handleAlertPropertyChange = (
        contentsToReplace: Partial<EditableAlert>,
        isTotalReplace = false
    ): void => {
        if (isTotalReplace) {
            setAlert(contentsToReplace as EditableAlert);
        } else {
            setAlert({
                ...alert,
                ...contentsToReplace,
            });
        }

        if (
            alertTemplateOptions &&
            contentsToReplace.template &&
            contentsToReplace.template.name
        ) {
            const selectedAlertTemplateName = contentsToReplace.template.name;
            const match = alertTemplateOptions.find(
                (candidate) => candidate.name === selectedAlertTemplateName
            );
            setSelectedAlertTemplate(match === undefined ? null : match);
        }
    };

    const handleSubscriptionGroupChange = (
        updatedGroups: SubscriptionGroup[]
    ): void => {
        setSubscriptionGroups(updatedGroups);
    };

    const handleValidationEntryChange = (
        key: CreateAlertConfigurationSection,
        isValid: boolean
    ): void => {
        setValidationEntries((current) => {
            return {
                ...current,
                [key]: isValid,
            };
        });
    };

    const handleCreateAlertClick = (): void => {
        createAlert(alert)
            .then((alert) => {
                if (isEmpty(subscriptionGroups)) {
                    // Redirect to alerts detail path
                    navigate(getAlertsViewPath(alert.id));

                    return;
                }

                // Update subscription groups with new alert
                for (const subscriptionGroup of subscriptionGroups) {
                    if (subscriptionGroup.alerts) {
                        // Add to existing list
                        subscriptionGroup.alerts.push(alert);
                    } else {
                        // Create and add to list
                        subscriptionGroup.alerts = [alert];
                    }
                }

                updateSubscriptionGroups(subscriptionGroups)
                    .then((): void => {
                        notify(
                            NotificationTypeV1.Success,
                            t("message.update-success", {
                                entity: t("label.subscription-groups"),
                            })
                        );
                    })
                    .finally((): void => {
                        // Redirect to alerts detail path
                        navigate(getAlertsViewPath(alert.id));
                    });
            })
            .catch((error: AxiosError) => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.create-error", {
                              entity: t("label.alert"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    return (
        <PageV1>
            <PageHeaderV1>
                <PageHeaderTextV1>
                    {t("label.create-entity", {
                        entity: t("label.alert"),
                    })}
                </PageHeaderTextV1>

                <PageHeaderActionsV1>
                    <Box padding={1}>View:</Box>
                    <ButtonGroup color="primary" variant="outlined">
                        <Button
                            component={NavLink}
                            to={{
                                pathname: AppRoute.ALERTS_CREATE_SIMPLE,
                                search: searchParams.toString(),
                            }}
                        >
                            Simple
                        </Button>
                        <Button
                            component={NavLink}
                            to={{
                                pathname: AppRoute.ALERTS_CREATE_ADVANCED,
                                search: searchParams.toString(),
                            }}
                        >
                            Advanced
                        </Button>
                    </ButtonGroup>
                </PageHeaderActionsV1>
            </PageHeaderV1>
            <PageContentsGridV1>
                <Outlet
                    context={[
                        alert,
                        handleAlertPropertyChange,
                        subscriptionGroups,
                        handleSubscriptionGroupChange,
                        selectedAlertTemplate,
                        setSelectedAlertTemplate,
                        alertTemplateOptions,
                    ]}
                />
                <Box textAlign="right" width="100%">
                    <PageContentsCardV1>
                        <Button className={classes.footerBtn} color="secondary">
                            Cancel
                        </Button>
                        <Button
                            className={classes.footerBtn}
                            color="primary"
                            disabled={!isAlertValid}
                            onClick={handleCreateAlertClick}
                        >
                            Create Alert
                        </Button>
                    </PageContentsCardV1>
                </Box>
            </PageContentsGridV1>
        </PageV1>
    );
};
