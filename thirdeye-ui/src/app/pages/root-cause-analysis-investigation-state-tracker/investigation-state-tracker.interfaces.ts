import { Investigation } from "../../rest/dto/rca.interfaces";

export type InvestigationContext = {
    investigation: Investigation;
    investigationHasChanged: (modified: Investigation) => void;
};
