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
import { Workspace } from "../dto/workspace.interfaces";
import { GetWorkspaces } from "./workspace.interface";
import { getWorkspaces as getWorkspacesREST } from "./workspace.rest";

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
