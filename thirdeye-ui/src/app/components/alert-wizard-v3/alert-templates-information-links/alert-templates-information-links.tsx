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
import { Box, Link, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";

export const AlertTemplatesInformationLinks: FunctionComponent = () => {
    const { t } = useTranslation();

    return (
        <>
            <Box>
                <Link
                    href="https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/anomaly-detection-algorithms#detection-algorithms"
                    target="_blank"
                >
                    <Typography variant="caption">
                        {t(
                            "message.view-documentation-on-all-available-algorithms"
                        )}
                    </Typography>
                </Link>
            </Box>
            <Box>
                <Link
                    href="https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/concepts/anomaly-detection-algorithms#choosing-the-right-algorithm"
                    target="_blank"
                >
                    <Typography variant="caption">
                        {t("message.view-guide-on-which-algorithm-to-select")}
                    </Typography>
                </Link>
            </Box>
        </>
    );
};
