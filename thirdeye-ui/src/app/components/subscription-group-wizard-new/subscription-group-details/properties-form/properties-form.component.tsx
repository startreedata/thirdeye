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
import { CronEditor } from "../../../cron-editor-v1/cron-editor-v1.component";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { PropertiesFormProps } from "./properties-form.interface";

// Refer: SubscriptionGroupPropertiesForm
export const PropertiesForm: FunctionComponent<PropertiesFormProps> = ({
    values: { name, cron },
    onChange,
}) => {
    const { t } = useTranslation();

    const handleUpdateCron = (cron: string): void => {
        onChange((stateProp) => ({ ...stateProp, cron }));
    };

    const handleUpdateName = (name: string): void => {
        onChange((stateProp) => ({ ...stateProp, name }));
    };

    return (
        <Grid item xs={12}>
            <Card variant="outlined">
                <CardHeader title={t("label.group-details")} />
                <CardContent>
                    <InputSection
                        inputComponent={
                            <TextField
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
                                variant="outlined"
                                onChange={(e) => {
                                    handleUpdateName(e.currentTarget.value);
                                }}
                            />
                        }
                        label={t("label.group-name")}
                    />
                    <CronEditor
                        cron={cron}
                        handleUpdateCron={handleUpdateCron}
                    />
                </CardContent>
            </Card>
        </Grid>
    );
};
