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
import { Box, FormControlLabel, Radio } from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { Modal } from "../modal/modal.component";
import { AlertUpdateResetModalProps } from "./alert-update-reset-modal.interfaces";

export const AlertUpdateResetModal: FunctionComponent<AlertUpdateResetModalProps> =
    ({ onUpdateAlertClick }) => {
        const { t } = useTranslation();

        const [shouldDeleteAnomalies, setShouldDeleteAnomalies] =
            useState(false);

        const submitBtnLabel = shouldDeleteAnomalies
            ? "Update & reset alert"
            : t("label.update-entity", {
                  entity: t("label.alert"),
              });

        return (
            <Modal
                initiallyOpen
                submitButtonLabel={submitBtnLabel}
                title={t("label.update-entity", {
                    entity: t("label.alert"),
                })}
                trigger={() => <></>}
                onSubmit={() => onUpdateAlertClick(shouldDeleteAnomalies)}
            >
                <p>Alert Update Options:</p>
                <Box pl={3}>
                    <FormControlLabel
                        checked={!shouldDeleteAnomalies}
                        control={<Radio color="primary" />}
                        label="Keep past investigations, no new notifications for historical anomalies."
                        onClick={() => setShouldDeleteAnomalies(false)}
                    />
                    <FormControlLabel
                        checked={shouldDeleteAnomalies}
                        control={<Radio color="primary" />}
                        label="Delete past results, notify for all historical anomalies"
                        onClick={() => setShouldDeleteAnomalies(true)}
                    />
                </Box>
            </Modal>
        );
    };
