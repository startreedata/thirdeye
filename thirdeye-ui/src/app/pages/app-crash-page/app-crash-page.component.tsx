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

import {
    Box,
    Button,
    Card,
    CardContent,
    Grid,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { PageContentsGridV1, PageV1 } from "../../platform/components";

export const AppCrashPage: FunctionComponent = () => {
    const handleOnClick = (): void => {
        window.location.reload();
    };

    return (
        <PageV1>
            <PageContentsGridV1 fullHeight>
                <Grid item xs={12}>
                    <Box component={Card} p={20}>
                        <CardContent>
                            <Typography variant="h1">
                                An error occurred
                            </Typography>
                            <Typography>
                                The application has crashed, please reload the
                                page to restart the application.
                            </Typography>
                            <Box mt={2}>
                                <Button
                                    color="primary"
                                    variant="outlined"
                                    onClick={handleOnClick}
                                >
                                    Reload Application
                                </Button>
                            </Box>
                        </CardContent>
                    </Box>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
