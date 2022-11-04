import { Investigation } from "../../../../rest/dto/rca.interfaces";
import {
    createInvestigation,
    updateInvestigation,
} from "../../../../rest/rca/rca.rest";

export interface ModifyInvestigationDialogProps {
    investigation: Investigation;
    onSuccessfulSave: (investigation: Investigation) => void;
    onClose: () => void;
    actionLabelIdentifier: string;
    errorGenericMsgIdentifier: string;
    serverRequestRestFunction:
        | typeof createInvestigation
        | typeof updateInvestigation;
}
