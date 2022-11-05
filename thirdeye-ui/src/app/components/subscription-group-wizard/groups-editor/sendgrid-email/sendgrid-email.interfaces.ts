import { SendgridEmailSpec } from "../../../../rest/dto/subscription-group.interfaces";

export interface SendgridEmailProps {
    configuration: SendgridEmailSpec;
    onSpecChange: (updatedSpec: SendgridEmailSpec) => void;
    onDeleteClick: () => void;
}

export interface SendgridEmailFormEntries {
    apiKey: string;
    from: string;
}
