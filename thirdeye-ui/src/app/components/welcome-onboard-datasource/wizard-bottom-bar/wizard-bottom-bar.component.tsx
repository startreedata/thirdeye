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

import { Box, Button, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { PageContentsCardV1 } from "../../../platform/components";
import { WizardBottomBarProps } from "./wizard-bottom-bar.interfaces";

export const WizardBottomBar: FunctionComponent<WizardBottomBarProps> = ({
    backBtnLink,
    nextBtnLink,
    handleNextClick,
    handleBackClick,
    nextButtonLabel,
    backButtonLabel,
    nextButtonIsDisabled,
    children,
    doNotWrapInContainer,
}) => {
    const { t } = useTranslation();

    let mainContent = (
        <PageContentsCardV1>
            <Grid container alignItems="center" justifyContent="flex-end">
                {children && <Grid item>{children}</Grid>}

                <Grid item>
                    {handleBackClick && (
                        <Button
                            color="primary"
                            variant="outlined"
                            onClick={handleBackClick}
                        >
                            {backButtonLabel || t("label.back")}
                        </Button>
                    )}
                    {backBtnLink && (
                        <Button
                            color="primary"
                            component={RouterLink}
                            id="back-bottom-bar-btn"
                            to={backBtnLink}
                            variant="outlined"
                        >
                            {backButtonLabel || t("label.back")}
                        </Button>
                    )}
                </Grid>
                <Grid item>
                    {handleNextClick && (
                        <Button
                            color="primary"
                            disabled={nextButtonIsDisabled}
                            id="next-bottom-bar-btn"
                            onClick={handleNextClick}
                        >
                            {nextButtonLabel || t("label.next")}
                        </Button>
                    )}
                    {nextBtnLink && (
                        <Button
                            color="primary"
                            component={RouterLink}
                            disabled={nextButtonIsDisabled}
                            id="next-bottom-bar-btn"
                            to={nextBtnLink}
                        >
                            {nextButtonLabel || t("label.next")}
                        </Button>
                    )}
                </Grid>
            </Grid>
        </PageContentsCardV1>
    );

    if (!doNotWrapInContainer) {
        mainContent = (
            <Box
                bottom={0}
                marginTop="auto"
                position="sticky"
                width="100%"
                zIndex={10}
            >
                {mainContent}
            </Box>
        );
    }

    return mainContent;
};
