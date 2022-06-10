///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


import { useHTTPAction } from "../create-rest-action";
import { AlertTemplate } from "../dto/alert-template.interfaces";
import {
    GetAlertTemplate,
    GetAlertTemplates,
} from "./alert-templates.interfaces";
import {
    getAlertTemplate as getAlertTemplateREST,
    getAlertTemplates as getAlertTemplatesREST,
} from "./alert-templates.rest";

export const useGetAlertTemplate = (): GetAlertTemplate => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<AlertTemplate>(getAlertTemplateREST);

    const getAlertTemplate = (
        alertTemplateId: number
    ): Promise<AlertTemplate | undefined> => {
        return makeRequest(alertTemplateId);
    };

    return { alertTemplate: data, getAlertTemplate, status, errorMessages };
};

export const useGetAlertTemplates = (): GetAlertTemplates => {
    const { data, makeRequest, status, errorMessages } = useHTTPAction<
        AlertTemplate[]
    >(getAlertTemplatesREST);

    const getAlertTemplates = (): Promise<AlertTemplate[] | undefined> => {
        return makeRequest();
    };

    return { alertTemplates: data, getAlertTemplates, status, errorMessages };
};
