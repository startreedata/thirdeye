import { ActionStatus } from "../../rest/actions.interfaces";
import { Investigation } from "../../rest/dto/rca.interfaces";

export interface InvestigationsListProps {
    investigations: Investigation[] | null;
    getInvestigationsRequestStatus: ActionStatus;
}
