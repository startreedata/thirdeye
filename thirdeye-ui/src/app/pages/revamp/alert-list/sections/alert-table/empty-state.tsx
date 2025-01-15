/*
 * Copyright 2025 StarTree Inc
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
import React from "react";
import { Box, Button, Card, CardContent, Grid, Link } from "@material-ui/core";
import { useTranslation } from "react-i18next";
import { NoDataIndicator } from "../../../../../components/no-data-indicator/no-data-indicator.component";
import { getAlertsCreatePath } from "../../../../../utils/routes/routes.util";

export const EmptyStateView = (): JSX.Element => {
    const { t } = useTranslation();

    return (
        <Grid item xs={12}>
            <Card variant="outlined">
                <CardContent>
                    <Box pb={20} pt={20}>
                        <NoDataIndicator>
                            <Box>
                                {t("message.no-alerts-created")}{" "}
                                <Link
                                    href="https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/getting-started/create-your-first-alert"
                                    target="_blank"
                                >
                                    {t("message.view-documentation")}
                                </Link>{" "}
                                {t("message.on-how-to-create-entity", {
                                    entity: t("label.alert"),
                                })}
                            </Box>
                            <Box marginTop={2} textAlign="center">
                                or
                            </Box>
                            <Box marginTop={2} textAlign="center">
                                <Button
                                    color="primary"
                                    href={getAlertsCreatePath()}
                                >
                                    {t("label.create-an-entity", {
                                        entity: t("label.alert"),
                                    })}
                                </Button>
                            </Box>
                        </NoDataIndicator>
                    </Box>
                </CardContent>
            </Card>
        </Grid>
    );
};
