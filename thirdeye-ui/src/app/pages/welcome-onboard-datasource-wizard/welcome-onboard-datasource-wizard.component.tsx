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
];

export const WelcomeOnboardDatasourceWizard: FunctionComponent = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();

    const activeStep = useMemo(() => {
        const activeStepDefinition = STEPS.find((candidate) =>
            pathname.includes(candidate.subPath)
        );

        if (!activeStepDefinition) {
            return "";
        }

        return activeStepDefinition.subPath;
    }, [pathname]);

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                customActions={<Button>{t("label.help")}</Button>}
                subtitle="Connect to StarTree cloud data or add your Pinot datasource"
                title="Let's start setting up your data"
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Box pb={0} pt={2} px={2}>
                            <Typography variant="h5">
                                {/* {t("message.complete-the-following-steps")} */}
                                Complete the following steps
                            </Typography>
                            <StepperV1
                                activeStep={activeStep}
                                stepLabelFn={(step: string): string => {
                                    const stepDefinition = STEPS.find(
                                        (candidate) =>
                                            candidate.subPath === step
                                    );

                                    return t(
                                        `message.${stepDefinition?.translationLabel}`
                                    );
                                }}
                                steps={STEPS.map((item) => item.subPath)}
                            />
                        </Box>
                    </PageContentsCardV1>
                </Grid>
                <Grid item xs={12}>
                    <Outlet context={{}} />
                </Grid>
            </PageContentsGridV1>

            <Box marginTop="auto" width="100%">
                <PageContentsCardV1>
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Button color="secondary">{t("label.back")}</Button>
                        </Grid>
                        <Grid item>
                            <Button color="primary">{t("label.next")}</Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Box>
        </PageV1>
    );
};
