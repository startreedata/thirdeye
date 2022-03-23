import { ActionHook } from "../actions.interfaces";
import { AlertTemplate } from "../dto/alert-template.interfaces";

export interface GetAlertTemplate extends ActionHook {
    alertTemplate: AlertTemplate | null;
    getAlertTemplate: (id: number) => Promise<AlertTemplate | undefined>;
}

export interface GetAlertTemplates extends ActionHook {
    alertTemplates: AlertTemplate[] | null;
    getAlertTemplates: () => Promise<AlertTemplate[] | undefined>;
}
