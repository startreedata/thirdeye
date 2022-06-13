import { Investigation } from "../../../rest/dto/rca.interfaces";

export interface InvestigationOptionsProps {
    investigationId: number | null;
    localInvestigation: Investigation;
    serverInvestigation: Investigation | null;
    onSuccessfulUpdate: (investigation: Investigation) => void;
    onRemoveInvestigationAssociation: () => void;
}
