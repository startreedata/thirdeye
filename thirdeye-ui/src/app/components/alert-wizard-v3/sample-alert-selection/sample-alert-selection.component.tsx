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
import { Button, CardActions, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../../platform/components";
import { AlgorithmOptionCard } from "../algorithm-selection/algorithm-option-card/algorithm-option-card.component";
import { SampleAlertSelectionProps } from "./sample-alert-selection.interfaces";
import { generateOptions } from "./sample-alert.utils";

export const SampleAlertSelection: FunctionComponent<SampleAlertSelectionProps> =
    ({ onSampleAlertSelect }) => {
        const { t } = useTranslation();

        const options = useMemo(() => {
            return generateOptions(t);
        }, []);

        return (
            <PageContentsCardV1>
                <Grid container alignItems="stretch">
                    <Grid item xs={12}>
                        <Typography variant="h5">
                            {t("message.you-can-create-sample-alerts")}
                        </Typography>
                        <Typography variant="body2">
                            {t("message.create-sample-alert-description")}
                        </Typography>
                    </Grid>

                    <Grid container item xs={12}>
                        {options.map((option) => {
                            return (
                                <Grid item key={option.title} sm={3} xs={12}>
                                    <AlgorithmOptionCard option={option}>
                                        <CardActions>
                                            <Button
                                                size="small"
                                                onClick={() =>
                                                    onSampleAlertSelect(option)
                                                }
                                            >
                                                {t("label.create-sample-alert")}
                                            </Button>
                                        </CardActions>
                                    </AlgorithmOptionCard>
                                </Grid>
                            );
                        })}
                    </Grid>
                </Grid>
            </PageContentsCardV1>
        );
    };
