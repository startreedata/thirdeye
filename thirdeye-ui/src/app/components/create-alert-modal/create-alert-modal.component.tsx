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
import { Grid, TextField } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AlertCreatedGuidedPageOutletContext } from "../../pages/alerts-create-guided-page/alerts-create-guided-page.interfaces";
import { SETUP_DETAILS_TEST_IDS } from "../../pages/alerts-create-guided-page/setup-details/setup-details-page.interface";
import { useDialogProviderV1 } from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { generateGenericNameForAlert } from "../../utils/alerts/alerts.util";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { GranularityValue } from "../alert-wizard-v3/select-metric/select-metric.utils";
import { CronEditor } from "../cron-editor-v1/cron-editor-v2.component";
import { InputSectionV2 } from "../form-basics/input-section-v2/input-section-v2.component";
import { Modal } from "../modal/modal.component";
import { CreateAlertModalModalProps } from "./create-alert-modal.interfaces";
import { useAppBarConfigProvider } from "../app-bar/app-bar-config-provider/app-bar-config-provider.component";

export const CreateAlertModal: FunctionComponent<CreateAlertModalModalProps> =
    ({ onCancel, defaultCron }) => {
        const { t } = useTranslation();

        const navigate = useNavigate();
        const { remainingQuota } = useAppBarConfigProvider();

        const {
            alert,
            onAlertPropertyChange,
            selectedAlgorithmOption,
            handleCreateAlertClick,
        } = useOutletContext<AlertCreatedGuidedPageOutletContext>();

        const { showDialog } = useDialogProviderV1();

        useEffect(() => {
            // On initial render, ensure a metric, dataset, and dataSource are set
            if (
                !alert.templateProperties.aggregationColumn ||
                !alert.templateProperties.dataset ||
                !alert.templateProperties.dataSource
            ) {
                navigate(
                    `../${AppRouteRelative.WELCOME_CREATE_ALERT_TUNE_ALERT}`
                );
            }
        }, []);

        const handleCronChange = (cron: string): void => {
            onAlertPropertyChange({
                cron,
            });
        };

        useEffect(() => {
            if (defaultCron) {
                handleCronChange(defaultCron);
            }
        }, [defaultCron]);

        const checkQuota = (): void => {
            if (
                remainingQuota?.notification &&
                remainingQuota?.notification <= 0
            ) {
                showDialog({
                    type: DialogType.ALERT,
                    contents: (
                        <>
                            <div>{t("message.quota-expired-detection")}</div>
                            <div>
                                {t("message.contact-support", {
                                    message:
                                        "For questions or to request a higher quota,",
                                })}
                            </div>
                        </>
                    ),
                    okButtonText: t("label.confirm"),
                    cancelButtonText: t("label.cancel"),
                    onOk: () => handleCreateAlertClick(alert),
                });
            } else {
                handleCreateAlertClick(alert);
            }
        };

        const onCreateAlertClick: () => boolean = () => {
            if (
                alert.templateProperties.monitoringGranularity &&
                (alert.templateProperties.monitoringGranularity ===
                    GranularityValue.ONE_MINUTE ||
                    alert.templateProperties.monitoringGranularity ===
                        GranularityValue.FIVE_MINUTES)
            ) {
                showDialog({
                    type: DialogType.ALERT,
                    contents: t(
                        "message.monitoring-granularity-below-15-minutes-warning"
                    ),
                    okButtonText: t("label.confirm"),
                    cancelButtonText: t("label.cancel"),
                    onOk: () => checkQuota(),
                    keepDialogOnOk: true,
                });
            } else {
                checkQuota();
            }

            return false;
        };

        return (
            <Modal
                initiallyOpen
                submitButtonLabel={t(
                    alert.id ? "label.update-alert" : "label.create-alert"
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
                                    alert.name ||
                                    generateGenericNameForAlert(
                                        alert.templateProperties
                                            .aggregationColumn as string,
                                        alert.templateProperties
                                            .aggregationFunction as string,
                                        selectedAlgorithmOption.algorithmOption
                                            .title as string,
                                        selectedAlgorithmOption?.algorithmOption
                                            .alertTemplateForMultidimension ===
                                            alert?.template?.name
                                    )
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
                                defaultValue={alert.description}
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
                        cron={alert.cron}
                        defaultCron={defaultCron}
                        handleUpdateCron={handleCronChange}
                    />
                </Grid>
            </Modal>
        );
    };
