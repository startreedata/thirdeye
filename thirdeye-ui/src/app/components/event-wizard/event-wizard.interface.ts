import { EditableEvent, Event } from "../../rest/dto/event.interfaces";

export interface EventWizardProps {
    event?: Event | EditableEvent;
    showCancel?: boolean;
    onCancel?: () => void;
    onChange?: (eventWizardStep: EventsWizardStep) => void;
    onFinish?: (event: EditableEvent) => void;
}

export enum EventsWizardStep {
    EVENT_PROPERTIES,
    REVIEW_AND_SUBMIT,
}
