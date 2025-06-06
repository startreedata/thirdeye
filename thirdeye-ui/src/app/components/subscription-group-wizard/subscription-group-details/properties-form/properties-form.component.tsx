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

import {
    Card,
    CardContent,
    CardHeader,
    Grid,
    Switch,
    TextField,
} from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { CronEditor } from "../../../cron-editor-v1/cron-editor-v1.component";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { PropertiesFormProps } from "./properties-form.interfaces";
import { InfoLabel } from "../../../info-label/info-label.component";
import { AnomayDurationInput } from "./anomaly-duration-input";
import { usePropertiesFormStyle } from "./styles";

// Refer: SubscriptionGroupPropertiesForm
export const PropertiesForm: FunctionComponent<PropertiesFormProps> = ({
    values: { name, cron, notifyHistoricalAnomalies, minimumAnomalyLength },
    onChange,
    customHeader,
}) => {
    const { t } = useTranslation();
    const [showAnomalyDurationInput, setShowAnomalyDurationInput] = useState(
        !!minimumAnomalyLength
    );

    const classes = usePropertiesFormStyle();
    const handleUpdateCron = (cron: string): void => {
        onChange((stateProp) => ({ ...stateProp, cron }));
    };

    const handleUpdateName = (name: string): void => {
        onChange((stateProp) => ({ ...stateProp, name }));
    };

    const handleUpdateNotifyHistoricAnomalies = (
        notifyHistoricalAnomalies: boolean
    ): void => {
        onChange((stateProp) => ({ ...stateProp, notifyHistoricalAnomalies }));
    };

    const handleAnonalyDurationToggle = (
        showAnomalyDurationInput: boolean
    ): void => {
        setShowAnomalyDurationInput(showAnomalyDurationInput);
        const value = showAnomalyDurationInput ? minimumAnomalyLength : null;
        onChange((stateProp) => ({
            ...stateProp,
            minimumAnomalyLength: value,
        }));
    };

    const handleAnonalyDurationChange = (value: string): void => {
        onChange((stateProp) => ({
            ...stateProp,
            minimumAnomalyLength: value,
        }));
    };

    return (
        <Grid item xs={12}>
            <Card data-testId="card-container">
                {customHeader || (
                    <CardHeader
                        data-testId="details-card-header"
                        subheader={t(
                            "message.add-details-that-define-the-groups-purpose-and-frequency"
                        )}
                        subheaderTypographyProps={{
                            variant: "subtitle1",
                        }}
                        title={t("label.group-details")}
                    />
                )}
                <CardContent>
                    <InputSection
                        inputComponent={
                            <TextField
                                fullWidth
                                required
                                data-testId="group-name"
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
                    <InputSection
                        inputComponent={
                            <Switch
                                defaultChecked={!!notifyHistoricalAnomalies}
                                onChange={(e) => {
                                    handleUpdateNotifyHistoricAnomalies(
                                        e.target.checked
                                    );
                                }}
                            />
                        }
                        labelComponent={
                            <InfoLabel
                                label={t("label.notifyHistoricalAnomalies")}
                                tooltipText={t(
                                    "info.notifyHistoricalAnomalies"
                                )}
                            />
                        }
                    />
                    <div className={classes.anomalyDurationContainer}>
                        <InputSection
                            inputComponent={
                                <div>
                                    <Switch
                                        defaultChecked={
                                            !!showAnomalyDurationInput
                                        }
                                        onChange={(e) => {
                                            handleAnonalyDurationToggle(
                                                e.target.checked
                                            );
                                        }}
                                    />
                                    {showAnomalyDurationInput && (
                                        <AnomayDurationInput
                                            value={minimumAnomalyLength}
                                            onChange={
                                                handleAnonalyDurationChange
                                            }
                                        />
                                    )}
                                </div>
                            }
                            labelComponent={
                                <InfoLabel
                                    label={t("label.minimumAnomalyLength")}
                                    tooltipText={t("info.minimumAnomalyLength")}
                                />
                            }
                        />
                    </div>
                </CardContent>
            </Card>
        </Grid>
    );
};
