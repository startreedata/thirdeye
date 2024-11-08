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
import axios from "axios";
import { Workspace, WorkspaceConfiguration } from "../dto/workspace.interfaces";

const BASE_URL_WORKSPACES = "/api/workspaces";

export const getWorkspaces = async (): Promise<Workspace[]> => {
    const response = await axios.get(`${BASE_URL_WORKSPACES}/`);

    return response.data;
};

export const getWorkspaceConfiguration =
    async (): Promise<WorkspaceConfiguration> => {
        const response = await axios.get("/api/workspace-configuration");

        return response.data;
    };

export const resetWorkspaceConfiguration =
    async (): Promise<WorkspaceConfiguration> => {
        const response = await axios.post("/api/workspace-configuration/reset");

        return response.data;
    };

export const updateWorkspaceConfiguration = async (
    config: WorkspaceConfiguration
): Promise<WorkspaceConfiguration> => {
    const response = await axios.put("/api/workspace-configuration", config);

    return response.data;
};
