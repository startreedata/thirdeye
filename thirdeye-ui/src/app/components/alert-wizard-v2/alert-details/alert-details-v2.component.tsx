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
import { FormHelperText, Grid, TextField } from "@material-ui/core";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { NavigateAlertCreationFlowsDropdown } from "../../alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown-v2";
import { InputSectionV2 } from "../../form-basics/input-section-v2/input-section-v2.component";
import { AlertDetailsProps } from "./alert-details.interfaces";
import { AlertFrequency } from "./alert-frequency-v2/alert-frequency.component";

function AlertDetails({
    alert,
    onAlertPropertyChange,
}: AlertDetailsProps): JSX.Element {
    const [name, setName] = useState(alert.name);
    const [description, setDescription] = useState(alert.description);
    const [nameHasError, setNameHasError] = useState(false);
    const { t } = useTranslation();

    const handleNameChange = (
        e: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>
    ): void => {
        const newValue = e.currentTarget.value;

        setName(newValue);

        if (newValue && newValue.length > 0) {
            setNameHasError(false);
            onAlertPropertyChange({
                name: newValue,
            });
        } else {
            setNameHasError(true);
        }
    };

    return (
        <Grid container>
            <Grid item xs={12}>
                <Grid container alignContent="center" justifyContent="flex-end">
                    <Grid item>
                        <NavigateAlertCreationFlowsDropdown />
                    </Grid>
                </Grid>
            </Grid>
            <Grid item xs={4}>
                <InputSectionV2
                    inputComponent={
                        <>
                            <TextField
                                fullWidth
                                data-testid="name-input-container"
                                error={nameHasError}
                                value={name}
                                variant="outlined"
                                onChange={handleNameChange}
                            />
                            {nameHasError && (
                                <FormHelperText error>
                                    {t("message.please-enter-valid-name")}
                                </FormHelperText>
                            )}
                        </>
                    }
                    label={t("label.name-of-your-alert")}
                />
            </Grid>
            <Grid item xs={4}>
                <InputSectionV2
                    helperLabel={`(${t("label.optional")})`}
                    inputComponent={
                        <TextField
                            fullWidth
                            multiline
                            value={description}
                            variant="outlined"
                            onChange={(
                                e: React.ChangeEvent<
                                    HTMLTextAreaElement | HTMLInputElement
                                >
                            ) => {
                                setDescription(e.currentTarget.value);
                                onAlertPropertyChange({
                                    description: e.currentTarget.value,
                                });
                            }}
                        />
                    }
                    label={t("label.description")}
                />
            </Grid>
            <AlertFrequency
                alert={alert}
                onAlertPropertyChange={onAlertPropertyChange}
            />
        </Grid>
    );
}

export { AlertDetails };
