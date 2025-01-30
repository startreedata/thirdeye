/*
 * Copyright 2023 StarTree Inc
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

// external
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { Grid, TextField } from "@material-ui/core";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { cloneDeep, isEmpty } from "lodash";
import { AxiosError } from "axios";

// state
import { useCreateAlertStore } from "../../hooks/state";

// utils
import { GranularityValue } from "../../../../components/alert-wizard-v3/select-metric/select-metric.utils";
import { generateGenericNameForAlert } from "../../../../utils/alerts/alerts.util";
import { getErrorMessages } from "../../../../utils/rest/rest.util";
import { validateSubscriptionGroup } from "../../../../components/subscription-group-wizard/subscription-group-wizard.utils";
import {
    QUERY_PARAM_KEY_ALERT_TYPE,
    QUERY_PARAM_KEY_ANOMALIES_RETRY,
} from "../../../alerts-view-page/alerts-view-page.utils";
import {
    getAlertsAlertPath,
    getHomePath,
} from "../../../../utils/routes/routes.util";

// app components
import { DialogType } from "../../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { Modal } from "../../../../components/modal/modal.component";
import { InputSectionV2 } from "../../../../components/form-basics/input-section-v2/input-section-v2.component";
import { CronEditor } from "../../../../components/cron-editor-v1/cron-editor-v2.component";
import { useDialogProviderV1 } from "../../../../platform/components/dialog-provider-v1";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../../platform/components";

// types
import { SETUP_DETAILS_TEST_IDS } from "../../../alerts-create-guided-page/setup-details/setup-details-page.interface";
import { EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../../../rest/dto/subscription-group.interfaces";

// apis
import { createAlert } from "../../../../rest/alerts/alerts.rest";
import {
    createSubscriptionGroup,
    updateSubscriptionGroups,
} from "../../../../rest/subscription-groups/subscription-groups.rest";
import { useGetAlertsCount } from "../../../../rest/alerts/alerts.actions";
import { QUERY_PARAM_KEYS } from "../../../../utils/constants/constants.util";
import { useAppBarConfigProvider } from "../../../../components/app-bar/app-bar-config-provider/app-bar-config-provider.component";

type CreateAlertModalModalProps = {
    onCancel: () => void;
    defaultCron?: string;
};

export const ConfirmationModal: FunctionComponent<CreateAlertModalModalProps> =
    ({ onCancel, defaultCron }) => {
        const { t } = useTranslation();
        const navigate = useNavigate();
        const { notify } = useNotificationProviderV1();
        const { setShowAppNavBar } = useAppBarConfigProvider();
        const { alertsCount, getAlertsCount } = useGetAlertsCount();
        useEffect(() => {
            getAlertsCount();
        }, []);
        const {
            workingAlert,
            setWorkingAlert,
            selectedDetectionAlgorithm,
            newSubscriptionGroup,
            selectedExistingSubscriptionGroups,
            resetCreateAlertState,
        } = useCreateAlertStore();

        const { showDialog } = useDialogProviderV1();

        const genericAlertName = useMemo(() => {
            return generateGenericNameForAlert(
                workingAlert.templateProperties?.aggregationColumn as string,
                workingAlert.templateProperties?.aggregationFunction as string,
                selectedDetectionAlgorithm?.algorithmOption.title as string,
                selectedDetectionAlgorithm?.algorithmOption
                    .alertTemplateForMultidimension ===
                    workingAlert?.template?.name
            );
        }, [workingAlert, selectedDetectionAlgorithm]);

        useEffect(() => {
            if (genericAlertName) {
                let clonedworkingAlert = cloneDeep(workingAlert);
                clonedworkingAlert = {
                    ...clonedworkingAlert,
                    name: genericAlertName,
                    cron: defaultCron,
                };
                setWorkingAlert(clonedworkingAlert);
            }
        }, [genericAlertName]);

        const handleCronChange = (cron: string): void => {
            onAlertPropertyChange({ cron });
        };

        const onAlertPropertyChange = (alertProp: {
            [key in "name" | "description" | "cron"]?: string;
        }): void => {
            let clonedworkingAlert = cloneDeep(workingAlert);
            clonedworkingAlert = {
                ...clonedworkingAlert,
                ...alertProp,
            };
            setWorkingAlert(clonedworkingAlert);
        };

        const alertcreation = async (): Promise<EditableAlert | null> => {
            let newAlert: EditableAlert | null = null;
            try {
                newAlert = await createAlert(workingAlert as EditableAlert);
            } catch (error: unknown) {
                const errMessages = getErrorMessages(error as AxiosError);
                errMessages.map((err) =>
                    notify(NotificationTypeV1.Error, err.message, err.details)
                );

                return null;
            }

            return newAlert;
        };

        const subscriptionGroupCreation =
            async (): Promise<SubscriptionGroup | null> => {
                let newlyCreatedSubGroup: SubscriptionGroup | null = null;
                if (newSubscriptionGroup) {
                    if (
                        validateSubscriptionGroup(newSubscriptionGroup) &&
                        newSubscriptionGroup.specs?.length > 0
                    ) {
                        try {
                            newlyCreatedSubGroup =
                                await createSubscriptionGroup(
                                    newSubscriptionGroup
                                );
                        } catch (error: unknown) {
                            const errMessages = getErrorMessages(
                                error as AxiosError
                            );
                            errMessages.map((err) =>
                                notify(
                                    NotificationTypeV1.Error,
                                    err.message,
                                    err.details
                                )
                            );

                            return null;
                        }
                    }
                }

                return newlyCreatedSubGroup;
            };

        const handleAlertSubscriptionAssociation = async (
            alert: EditableAlert
        ): Promise<"success" | "error"> => {
            const allSubGroups: SubscriptionGroup[] = [];
            if (newSubscriptionGroup) {
                try {
                    const newlyCreatedSubGroup =
                        await subscriptionGroupCreation();
                    if (newlyCreatedSubGroup) {
                        allSubGroups.push(newlyCreatedSubGroup);
                    }
                } catch (error) {
                    const errMessages = getErrorMessages(error as AxiosError);
                    errMessages.map((err) =>
                        notify(
                            NotificationTypeV1.Error,
                            err.message,
                            err.details
                        )
                    );

                    return "error";
                }
            }

            if (selectedExistingSubscriptionGroups) {
                allSubGroups.push(...selectedExistingSubscriptionGroups);
            }

            for (const subscriptionGroup of allSubGroups) {
                const association = {
                    alert: { id: alert.id! },
                };
                if (subscriptionGroup.alertAssociations) {
                    subscriptionGroup.alertAssociations.push(association);
                } else {
                    subscriptionGroup.alertAssociations = [association];
                }
            }

            if (!isEmpty(allSubGroups)) {
                try {
                    await updateSubscriptionGroups(allSubGroups);
                } catch (error) {
                    const errMessages = getErrorMessages(error as AxiosError);
                    errMessages.map((err) =>
                        notify(
                            NotificationTypeV1.Error,
                            err.message,
                            err.details
                        )
                    );

                    return "error";
                }
            }

            return "success";
        };

        const handleCreateAlertClick = async (): Promise<void> => {
            if (workingAlert) {
                const newAlert = await alertcreation();
                if (newAlert) {
                    if (
                        newSubscriptionGroup ||
                        !isEmpty(selectedExistingSubscriptionGroups)
                    ) {
                        await handleAlertSubscriptionAssociation(newAlert);
                    }
                    resetCreateAlertState();
                    if (alertsCount) {
                        const searchParamsToSet = new URLSearchParams([
                            [QUERY_PARAM_KEY_ANOMALIES_RETRY, "true"],
                            [QUERY_PARAM_KEY_ALERT_TYPE, "create"],
                        ]);
                        navigate(
                            getAlertsAlertPath(newAlert.id!, searchParamsToSet)
                        );
                    } else {
                        const queryParams = new URLSearchParams([
                            [QUERY_PARAM_KEYS.SHOW_FIRST_ALERT_SUCCESS, "true"],
                        ]);
                        setShowAppNavBar(true);
                        navigate(`${getHomePath()}?${queryParams.toString()}`);
                    }
                }
            }
        };

        const onCreateAlertClick: () => void = async () => {
            if (
                workingAlert.templateProperties?.monitoringGranularity &&
                (workingAlert.templateProperties?.monitoringGranularity ===
                    GranularityValue.ONE_MINUTE ||
                    workingAlert.templateProperties?.monitoringGranularity ===
                        GranularityValue.FIVE_MINUTES)
            ) {
                showDialog({
                    type: DialogType.ALERT,
                    contents: t(
                        "message.monitoring-granularity-below-15-minutes-warning"
                    ),
                    okButtonText: t("label.confirm"),
                    cancelButtonText: t("label.cancel"),
                    onOk: () => handleCreateAlertClick(),
                });
            } else {
                handleCreateAlertClick();
            }
            onCancel();
        };

        return (
            <Modal
                initiallyOpen
                submitButtonLabel={t(
                    workingAlert.id
                        ? "label.update-alert"
                        : "label.create-alert"
                )}
                title={t("label.alert-details")}
                onCancel={onCancel}
                onSubmit={() => onCreateAlertClick()}
            >
                <Grid container>
                    <InputSectionV2
                        inputComponent={
                            <TextField
                                fullWidth
                                data-testid={SETUP_DETAILS_TEST_IDS.NAME_INPUT}
                                defaultValue={
                                    workingAlert.name || genericAlertName
                                }
                                onChange={(e) =>
                                    onAlertPropertyChange({
                                        name: e.currentTarget.value,
                                    })
                                }
                            />
                        }
                        label={t("label.name")}
                    />
                    <InputSectionV2
                        isOptional
                        inputComponent={
                            <TextField
                                fullWidth
                                multiline
                                data-testid={
                                    SETUP_DETAILS_TEST_IDS.DESCRIPTION_INPUT
                                }
                                defaultValue={workingAlert.description}
                                minRows={3}
                                placeholder={t(
                                    "message.provide-an-optional-description-for-this-alert"
                                )}
                                onChange={(e) =>
                                    onAlertPropertyChange({
                                        description: e.currentTarget.value,
                                    })
                                }
                            />
                        }
                        label={t("label.description")}
                    />
                </Grid>
                <Grid container>
                    <CronEditor
                        fullWidth
                        cron={workingAlert.cron!}
                        defaultCron={defaultCron}
                        handleUpdateCron={handleCronChange}
                    />
                </Grid>
            </Modal>
        );
    };
