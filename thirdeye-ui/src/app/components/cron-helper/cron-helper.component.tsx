/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { FormHelperText, Grid, Link } from "@material-ui/core";
import cronstrue from "cronstrue";
import React from "react";
import { useTranslation } from "react-i18next";
import { CronHelperProps } from "./cron-helper.interface";

const CronHelper: React.FunctionComponent<CronHelperProps> = ({
    isCronValid,
    cron,
}: CronHelperProps) => {
    const { t } = useTranslation();

    return (
        <>
            <Grid item lg={2} md={4} sm={12} xs={12} />
            <Grid item lg={10} md={8} sm={12} xs={12}>
                {isCronValid && (
                    <FormHelperText>
                        {cronstrue.toString(cron, {
                            verbose: true,
                        })}
                    </FormHelperText>
                )}

                {/* If there are errors, render them */}
                {!isCronValid && (
                    <FormHelperText error data-testid="error-message-container">
                        {t("message.invalid-cron-input-1")}
                        <Link
                            href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html"
                            target="_blank"
                        >
                            {t("label.cron-documentation")}
                        </Link>
                        {t("message.invalid-cron-input-2")}
                    </FormHelperText>
                )}
            </Grid>
        </>
    );
};

export default CronHelper;
