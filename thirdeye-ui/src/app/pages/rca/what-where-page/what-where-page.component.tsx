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
import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useOutletContext } from "react-router-dom";
import { InvestigationPreview } from "../../../components/rca/investigation-preview/investigation-preview.component";
import { WhatWhereNavigation } from "../../../components/rca/what-where-navigation/what-where-navigation.component";
import { InvestigationContext } from "../investigation-state-tracker-container-page/investigation-state-tracker.interfaces";

export const WhatWherePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const context = useOutletContext<InvestigationContext>();

    return (
        <>
            <Grid item xs={12}>
                <Typography variant="h4">
                    {t("message.what-went-wrong-and-where")}
                </Typography>
                <WhatWhereNavigation />
            </Grid>

            <Outlet context={context} />

            <Grid item xs={12}>
                <InvestigationPreview
                    alertInsight={context.alertInsight}
                    anomaly={context.anomaly}
                    investigation={context.investigation}
                />
            </Grid>
        </>
    );
};
