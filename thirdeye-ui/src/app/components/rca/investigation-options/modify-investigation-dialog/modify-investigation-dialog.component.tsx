// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    TextField,
} from "@material-ui/core";
import Alert from "@material-ui/lab/Alert";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { ModifyInvestigationDialogProps } from "./modify-investigation-dialog.interfaces";

export const ModifyInvestigationDialog: FunctionComponent<ModifyInvestigationDialogProps> =
    ({
        investigation,
        onClose,
        onSuccessfulSave,
        serverRequestRestFunction,
        actionLabelIdentifier,
        errorGenericMsgIdentifier,
    }) => {
        const { t } = useTranslation();
        const [saveErrors, setSaveErrors] = useState<string[]>([]);
        const [isSaving, setIsSaving] = useState(false);
        const [investigationName, setInvestigationName] = useState(
            investigation.name
        );
        const [investigationText, setInvestigationText] = useState(
            investigation.text
        );

        const handleSaveClick = (): void => {
            investigation.name = investigationName;
            investigation.text = investigationText;
            setSaveErrors([]);
            setIsSaving(true);

            serverRequestRestFunction(investigation)
                .then((investigationFromServer) => {
                    onSuccessfulSave(investigationFromServer);
                })
                .catch((response) => {
                    // Default to a generic error message
                    let errorMessages: string[] = [
                        t(errorGenericMsgIdentifier, {
                            entity: t("label.investigation"),
                        }),
                    ];

                    // If messages exist from server, use those instead
                    if (
                        response &&
                        response.response &&
                        response.response.data
                    ) {
                        if (!isEmpty(response.response.data.list)) {
                            errorMessages = response.response.data.list.map(
                                (error: { msg: string }) => error.msg
                            );
                        }
                    }
                    setSaveErrors(errorMessages);
                })
                .finally(() => {
                    setIsSaving(false);
                });
        };

        return (
            <Dialog
                fullWidth
                open
                maxWidth="sm"
                scroll="body"
                onClose={onClose}
            >
                {/* Header */}
                <DialogTitle className="dialog-provider-v1-header">
                    {t(actionLabelIdentifier, {
                        entity: t("label.investigation"),
                    })}
                </DialogTitle>

                <DialogContent>
                    <Grid container>
                        {saveErrors.length > 0 && (
                            <Grid item xs={12}>
                                {saveErrors.map((msg, idx) => (
                                    <Alert key={idx} severity="error">
                                        {msg}
                                    </Alert>
                                ))}
                            </Grid>
                        )}
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                name="name"
                                placeholder="Name your investigation"
                                value={investigationName}
                                onChange={(e) =>
                                    setInvestigationName(e.target.value)
                                }
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                multiline
                                name="text"
                                placeholder="Add comments or conclusion"
                                rows={5}
                                value={investigationText}
                                onChange={(e) =>
                                    setInvestigationText(e.target.value)
                                }
                            />
                        </Grid>
                    </Grid>
                </DialogContent>

                {/* Controls */}
                <DialogActions>
                    {/* Cancel button */}
                    <Button onClick={onClose}>{t("label.cancel")}</Button>

                    {/* Ok button */}
                    <Button
                        color="primary"
                        disabled={isSaving}
                        onClick={handleSaveClick}
                    >
                        {isSaving && t("label.saving")}
                        {!isSaving && t("label.save")}
                    </Button>
                </DialogActions>
            </Dialog>
        );
    };
