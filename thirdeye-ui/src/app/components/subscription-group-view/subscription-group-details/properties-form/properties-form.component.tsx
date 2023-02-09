/*
 * Copyright 2022 StarTree Inc
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

import {
    Card,
    CardContent,
    CardHeader,
    Grid,
    TextField,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { AlertFrequency } from "../../../alert-wizard-v2/alert-details/alert-frequency/alert-frequency.component";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { PropertiesFormProps } from "./properties-form.interface";

// Refer: SubscriptionGroupPropertiesForm
export const PropertiesForm: FunctionComponent<PropertiesFormProps> = ({
    name,
    cron,
}) => {
    const { t } = useTranslation();

    const handleCronUpdate = (props?: { cron?: string }): void => {
        // console.log("Updated cron: ", props);
    };

    return (
        <Grid item xs={12}>
            <Card variant="outlined">
                <CardHeader title={t("label.group-details")} />
                <CardContent>
                    <InputSection
                        inputComponent={
                            <TextField
                                disabled
                                fullWidth
                                required
                                // error={Boolean(
                                //     errors && errors.name
                                // )}
                                // helperText={
                                //     errors?.name?.message
                                // }
                                name="name"
                                type="string"
                                value={name}
                                // onChange={(e) => {
                                // }}
                                variant="outlined"
                            />
                        }
                        label={t("label.group-name")}
                    />

                    {/* TODO: Move to dedicated component */}
                    <AlertFrequency<EditableAlert>
                        alert={{ cron } as EditableAlert}
                        onAlertPropertyChange={handleCronUpdate}
                    />
                </CardContent>
            </Card>
        </Grid>
    );
};
