import { EditableAlert } from "../../rest/dto/alert.interfaces";

export enum CreateAlertConfigurationSection {
    NAME,
    CRON,
    TEMPLATE_PROPERTIES,
}

export interface AlertsCreatePageProps {
    startingAlertConfiguration: EditableAlert;
}
