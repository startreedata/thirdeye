/*
 * Copyright 2024 StarTree Inc
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
// external
import React, { useEffect } from "react";
import { Box, Grid } from "@material-ui/core";

// app components
import { PageContentsCardV1 } from "../../platform/components";

// styles
import { easyAlertStyles } from "./styles";

// sections
import {
    CreateAlertHeader,
    SelectDatasetAndMetric,
    AlertProperties,
    ViewAlertGraph,
    SubscribeNotification,
    CreateActionButtons,
} from "./sections";

// apis
import { useGetAlertTemplates } from "../../rest/alert-templates/alert-templates.actions";

// state
import { useCreateAlertStore } from "./hooks/state";

export const CreateAlert = (): JSX.Element => {
    const componentStyles = easyAlertStyles();
    const { setAlertTemplates, selectedMetric } = useCreateAlertStore();
    const { alertTemplates, getAlertTemplates } = useGetAlertTemplates();

    useEffect(() => {
        getAlertTemplates();
    }, []);

    useEffect(() => {
        setAlertTemplates(alertTemplates);
    }, [alertTemplates]);

    return (
        <>
            <Grid item xs={12}>
                <PageContentsCardV1 className={componentStyles.container}>
                    <Grid container>
                        <Grid item xs={12}>
                            <Box marginBottom={2}>
                                <Grid
                                    container
                                    alignContent="center"
                                    justifyContent="space-between"
                                >
                                    <CreateAlertHeader />
                                    <SelectDatasetAndMetric />
                                    <AlertProperties />
                                    <ViewAlertGraph />
                                    <SubscribeNotification />
                                    {selectedMetric && <CreateActionButtons />}
                                </Grid>
                            </Box>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Grid>
        </>
    );
};
