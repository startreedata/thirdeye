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
import React from "react";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import {
    JSONEditorV1,
    PageContentsGridV1,
    PageV1,
} from "../../platform/components";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";

import { Box, Button, Grid } from "@material-ui/core";
import { WorkspaceConfiguration } from "../../rest/dto/workspace.interfaces";
import { useWorkspaceApiRequests } from "./api";

export const NamespaceConfiguration = (): JSX.Element => {
    const {
        isError,
        isLoading,
        isUpdateDisabled,
        namespaceConfig,
        setNamespaceConfig,
        resetWorkspaceConfiguration,
        updateWorkspaceConfiguration,
    } = useWorkspaceApiRequests();

    const handleNamespaceConfigChange = (value: string): void => {
        setNamespaceConfig(JSON.parse(value));
    };
    const handleReset = (): void => {
        resetWorkspaceConfiguration();
    };
    const handleUpdate = async (): Promise<void> => {
        updateWorkspaceConfiguration(namespaceConfig as WorkspaceConfiguration);
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={6} />
            <PageContentsGridV1 fullHeight>
                <Grid container justifyContent="flex-end">
                    <Grid item xs={12}>
                        <LoadingErrorStateSwitch
                            wrapInCard
                            wrapInGrid
                            isError={isError}
                            isLoading={isLoading}
                        >
                            <JSONEditorV1<any>
                                hideValidationSuccessIcon
                                value={namespaceConfig}
                                onChange={handleNamespaceConfigChange}
                            />
                        </LoadingErrorStateSwitch>
                    </Grid>
                    <Grid item>
                        <Box display="flex" gridGap={12}>
                            <Button
                                color="primary"
                                data-testId="update-settings"
                                disabled={isUpdateDisabled}
                                variant="outlined"
                                onClick={handleUpdate}
                            >
                                Update
                            </Button>
                            <Button
                                color="primary"
                                data-testId="reset-settings"
                                variant="outlined"
                                onClick={handleReset}
                            >
                                Reset
                            </Button>
                        </Box>
                    </Grid>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
