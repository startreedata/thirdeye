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
import { Box } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Investigation } from "../../rest/dto/rca.interfaces";
import { Modal } from "../modal/modal.component";
import { InvestigationDetailsForm } from "../rca/investigation-details-form/investigation-details-form.component";
import { InvestigationUpdateModalProps } from "./investigation-update-modal.interfaces";

export const InvestigationUpdateModal: FunctionComponent<InvestigationUpdateModalProps> =
    ({ onClose, investigation, onUpdateClick, setInvestigation }) => {
        const { t } = useTranslation();
        const setInvestigationName = (value: string): void => {
            setInvestigation({
                ...investigation,
                name: value,
            } as Investigation);
        };
        const setInvestigationText = (value: string): void => {
            setInvestigation({
                ...investigation,
                text: value,
            } as Investigation);
        };

        return (
            <Modal
                initiallyOpen
                disableSubmitButton={!investigation?.name}
                submitButtonLabel={t("label.save")}
                title={t("label.add-investigation-details", {
                    entity: t("label.investigation"),
                })}
                trigger={() => <></>}
                onCancel={onClose}
                onSubmit={onUpdateClick}
            >
                <Box pl={3}>
                    <InvestigationDetailsForm
                        investigation={investigation}
                        onCommentChange={setInvestigationText}
                        onNameChange={setInvestigationName}
                    />
                </Box>
            </Modal>
        );
    };
