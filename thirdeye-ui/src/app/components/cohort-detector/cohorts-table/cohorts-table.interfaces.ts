import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    CohortDetectionResponse,
    CohortResult,
} from "../../../rest/dto/rca.interfaces";

export interface CohortsTableProps {
    getCohortsRequestStatus: ActionStatus;
    cohortsData: CohortDetectionResponse | null;
}

export interface CohortTableRowData extends CohortResult {
    name: string;
}
