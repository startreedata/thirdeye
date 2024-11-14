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
import { ActionHook } from "../actions.interfaces";
import { Workspace, WorkspaceConfiguration } from "../dto/workspace.interfaces";

export interface GetWorkspaces extends ActionHook {
    workspaces: Workspace[] | null;
    getWorkspaces: () => Promise<Workspace[] | undefined>;
}

export interface GetWorkspaceConfiguration extends ActionHook {
    workspaceConfiguration: WorkspaceConfiguration | null;
    getWorkspaceConfiguration: () => Promise<
        WorkspaceConfiguration | undefined
    >;
}

export interface UpdateWorkspaceConfiguration extends ActionHook {
    workspaceConfiguration: WorkspaceConfiguration | null;
    updateWorkspaceConfiguration: (
        config: WorkspaceConfiguration
    ) => Promise<WorkspaceConfiguration | undefined>;
}

export interface ResetWorkspaceConfiguration extends ActionHook {
    workspaceConfiguration: WorkspaceConfiguration | null;
    resetWorkspaceConfiguration: () => Promise<
        WorkspaceConfiguration | undefined
    >;
}
