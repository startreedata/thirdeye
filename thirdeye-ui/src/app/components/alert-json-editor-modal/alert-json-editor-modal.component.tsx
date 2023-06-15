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
import { Button } from "@material-ui/core";
import React, { FunctionComponent, useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { JSONEditorV1, useDialogProviderV1 } from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { AlertJsonEditorModalProps } from "./alert-json-editor-modal.interfaces";

export const AlertJsonEditorModal: FunctionComponent<AlertJsonEditorModalProps> =
    ({ alert, onSubmitChanges }) => {
        const { t } = useTranslation();
        const { showDialog } = useDialogProviderV1();

        const [, setLocalAlert] = useState<EditableAlert>(alert);

        const handleAdvancedEditorBtnClick = useCallback((): void => {
            showDialog({
                type: DialogType.CUSTOM,
                headerText: t("label.detection-configuration"),
                contents: (
                    <JSONEditorV1<EditableAlert>
                        disableValidation
                        value={alert}
                        onChange={(updates) => {
                            try {
                                const parsedString = JSON.parse(updates);
                                setLocalAlert(() => parsedString);
                            } catch {
                                // do nothing if invalid JSON string
                            }
                        }}
                    />
                ),
                width: "md",
                okButtonText: t("label.apply-changes"),
                cancelButtonText: t("label.cancel"),
                onOk: () => {
                    setLocalAlert((current) => {
                        // Wait for previous state updates to finish before
                        // calling onSubmitChanges
                        onSubmitChanges(current, true);

                        return current;
                    });
                },
            });
        }, [showDialog, alert]);

        return (
            <>
                <Button
                    color="primary"
                    variant="outlined"
                    onClick={handleAdvancedEditorBtnClick}
                >
                    {t("label.json-editor")}
                </Button>
            </>
        );
    };
