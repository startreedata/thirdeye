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
import React from "react";
import { Box, Grid } from "@material-ui/core";

// styles
import { graphOptionsStyles } from "./styles";

// sections
import { DetectionAlgorithms } from "./detection-algorithm";
import { DateRange } from "./date-range";
import { AdvancedOptions } from "./advanced-options";
import { AlertJsonEditorModal } from "../../../../../components/alert-json-editor-modal/alert-json-editor-modal.component";

// state
import { useCreateAlertStore } from "../../../hooks/state";
import { EditableAlert } from "../../../../../rest/dto/alert.interfaces";

export const GraphOptions = (): JSX.Element => {
    const componentStyles = graphOptionsStyles();
    const { workingAlert } = useCreateAlertStore();

    return (
        <>
            <Box className={componentStyles.card}>
                <Grid
                    item
                    className={componentStyles.algorithmContainer}
                    xs={12}
                >
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="space-between"
                    >
                        <Grid container alignItems="flex-start" xs={8}>
                            <Grid item xs={6}>
                                <DetectionAlgorithms />
                            </Grid>
                            <Grid item xs={6}>
                                <DateRange />
                            </Grid>
                        </Grid>
                        <Grid>
                            <Grid container>
                                <Box
                                    display="flex"
                                    gridGap="8px"
                                    justifyContent="space-between"
                                >
                                    <AlertJsonEditorModal
                                        isReadOnly
                                        alert={workingAlert as EditableAlert}
                                        buttonText="View Json"
                                        onSubmitChanges={() => {
                                            // handleAlertUpdate();
                                        }}
                                    />
                                    <AdvancedOptions />
                                </Box>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </Box>
        </>
    );
};
