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

import { Card, CardContent, CardHeader, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { AlertsDimensionsProps } from "./alerts-dimentions.interface";

export const AlertsDimensions: FunctionComponent<AlertsDimensionsProps> = ({
    uiSubscriptionGroupAlerts,
}) => {
    const { t } = useTranslation();

    return (
        <Grid item xs={12}>
            <Card variant="outlined">
                <CardHeader title={t("label.alerts-and-dimensions")} />
                <CardContent>
                    {JSON.stringify(uiSubscriptionGroupAlerts)}
                </CardContent>
            </Card>
        </Grid>
    );
};
