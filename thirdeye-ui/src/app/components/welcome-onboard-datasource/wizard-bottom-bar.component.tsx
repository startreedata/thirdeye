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

import { Box, Button, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../platform/components";
import { WizardBottomBarProps } from "./wizard-bottom-bar.interfaces";

export const WizardBottomBar: FunctionComponent<WizardBottomBarProps> = ({
    handleNextClick,
    handleBackClick,
    nextButtonLabel,
    backButtonLabel,
}) => {
    const { t } = useTranslation();

    return (
        <Box marginTop="auto" width="100%">
            <PageContentsCardV1>
                <Grid container justifyContent="flex-end">
                    <Grid item>
                        <Button color="secondary" onClick={handleBackClick}>
                            {backButtonLabel || t("label.back")}
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button color="primary" onClick={handleNextClick}>
                            {nextButtonLabel || t("label.next")}
                        </Button>
                    </Grid>
                </Grid>
            </PageContentsCardV1>
        </Box>
    );
};
