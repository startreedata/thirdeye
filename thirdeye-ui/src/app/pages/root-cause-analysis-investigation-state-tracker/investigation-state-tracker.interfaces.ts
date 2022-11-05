import { ActionStatus } from "../../rest/actions.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";
import { Investigation } from "../../rest/dto/rca.interfaces";

export type InvestigationContext = {
    investigation: Investigation;
    investigationHasChanged: (modified: Investigation) => void;
    getEnumerationItemRequest: ActionStatus;
    enumerationItem: EnumerationItem | null;
    anomaly: Anomaly;
    getAnomalyRequestStatus: ActionStatus;
    anomalyRequestErrors: string[];
};
