import { UiEvent } from "../../rest/dto/ui-event.interfaces";

export interface EventListV1Props {
    events: UiEvent[] | null;
    onDelete?: (uiEvent: UiEvent) => void;
}
