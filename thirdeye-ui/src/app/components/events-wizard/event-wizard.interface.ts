import { Event } from "../../rest/dto/event.interfaces";

export interface EventWizardProps {
    event?: Event;
    showCancel?: boolean;
    onCancel?: () => void;
    onChange?: (eventWizardStep: EventsWizardStep) => void;
    onFinish?: (event: Event) => void;
}

export enum EventsWizardStep {
    EVENT_PROPERTIES,
    REVIEW_AND_SUBMIT,
}
