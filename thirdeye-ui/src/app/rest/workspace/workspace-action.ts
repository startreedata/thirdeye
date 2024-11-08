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
import { useHTTPAction } from "../create-rest-action";
import { Workspace, WorkspaceConfiguration } from "../dto/workspace.interfaces";
import {
    GetWorkspaceConfiguration,
    GetWorkspaces,
    ResetWorkspaceConfiguration,
    UpdateWorkspaceConfiguration,
} from "./workspace.interface";
import {
    getWorkspaces as getWorkspacesREST,
    getWorkspaceConfiguration as getWorkspaceConfigurationREST,
    updateWorkspaceConfiguration as updateWorkspaceConfigurationREST,
    resetWorkspaceConfiguration as resetWorkspaceConfigurationREST,
} from "./workspace.rest";

export const useGetWorkspaces = (): GetWorkspaces => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<Workspace[]>(getWorkspacesREST);

    const getWorkspaces = (): Promise<Workspace[] | undefined> => {
        return makeRequest();
    };

    return {
        workspaces: data,
        getWorkspaces,
        status,
        errorMessages,
        resetData,
    };
};

export const useGetWorkspaceConfiguration = (): GetWorkspaceConfiguration => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<WorkspaceConfiguration>(getWorkspaceConfigurationREST);

    const getWorkspaceConfiguration = (): Promise<
        WorkspaceConfiguration | undefined
    > => {
        return makeRequest();
    };

    return {
        workspaceConfiguration: data,
        getWorkspaceConfiguration,
        status,
        errorMessages,
        resetData,
    };
};

export const useUpdateWorkspaceConfiguration =
    (): UpdateWorkspaceConfiguration => {
        const { data, makeRequest, status, errorMessages, resetData } =
            useHTTPAction<WorkspaceConfiguration>(
                updateWorkspaceConfigurationREST
            );

        const updateWorkspaceConfiguration = (
            config: WorkspaceConfiguration
        ): Promise<WorkspaceConfiguration | undefined> => {
            return makeRequest(config);
        };

        return {
            workspaceConfiguration: data,
            updateWorkspaceConfiguration,
            status,
            errorMessages,
            resetData,
        };
    };

export const useResetWorkspaceConfiguration =
    (): ResetWorkspaceConfiguration => {
        const { data, makeRequest, status, errorMessages, resetData } =
            useHTTPAction<WorkspaceConfiguration>(
                resetWorkspaceConfigurationREST
            );

        const resetWorkspaceConfiguration = (): Promise<
            WorkspaceConfiguration | undefined
        > => {
            return makeRequest();
        };

        return {
            workspaceConfiguration: data,
            resetWorkspaceConfiguration,
            status,
            errorMessages,
            resetData,
        };
    };
