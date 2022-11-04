import { WebhookSpec } from "../../../../rest/dto/subscription-group.interfaces";

export interface WebhookProps {
    configuration: WebhookSpec;
    onSpecChange: (updatedSpec: WebhookSpec) => void;
    onDeleteClick: () => void;
}

export interface WebhookFormEntries {
    url: string;
}
