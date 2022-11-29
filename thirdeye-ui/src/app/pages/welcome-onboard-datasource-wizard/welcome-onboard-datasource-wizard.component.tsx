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

import { Box, Button, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useLocation } from "react-router-dom";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    StepperV1,
} from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const STEPS = [
    {
        subPath: AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE,
        translationLabel: "onboard-datasource-select-datasource",
    },
    {
        subPath: AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS,
        translationLabel: "onboard-datasource-onboard-datasets",
    },
] as const;

export const WelcomeOnboardDatasourceWizard: FunctionComponent = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();

    const activeStep = useMemo(() => {
        // Tries to extract the last part of the url for
        // Uses the whole url if nothing comes up
        // This is required since the first url substring ("dataset")
        // is a part of the greater url for this module
        const urlPath: string =
            pathname.split("/").filter(Boolean).pop() || pathname;

        const activeStepDefinition = STEPS.find((candidate) =>
            candidate.subPath.includes(urlPath)
        );

        // Fallback
        if (!activeStepDefinition) {
            return STEPS[0].subPath;
        }

        return activeStepDefinition.subPath;
    }, [pathname]);

    const getStepLabel = (step: string): string => {
        const stepDefinition = STEPS.find(
            (candidate) => candidate.subPath === step
        );

        return t(`message.${stepDefinition?.translationLabel}`);
    };

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                customActions={<Button>{t("label.help")}</Button>}
                subtitle={t(
                    "message.connect-to-startree-cloud-data-or-add-your-own-pinot-datasource"
                )}
                title={t("message.lets-start-setting-up-your-data")}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Box pb={0} pt={2} px={2}>
                            <Typography variant="h5">
                                {t("message.complete-the-following-steps")}
                            </Typography>
                            <StepperV1
                                activeStep={activeStep}
                                stepLabelFn={getStepLabel}
                                steps={STEPS.map((item) => item.subPath)}
                            />
                        </Box>
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>
            <Outlet />
        </PageV1>
    );
};
