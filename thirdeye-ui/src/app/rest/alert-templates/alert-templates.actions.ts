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
