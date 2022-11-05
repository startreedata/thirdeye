import { SlackSpec } from "../../../../rest/dto/subscription-group.interfaces";

export interface SlackProps {
    configuration: SlackSpec;
    onSpecChange: (updatedSpec: SlackSpec) => void;
    onDeleteClick: () => void;
}

export interface SlackFormEntries {
    webhookUrl: string;
}
