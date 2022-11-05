import { EditableAlert } from "../../../../rest/dto/alert.interfaces";

export interface PreviewChartProps {
    alert: EditableAlert;
    displayState: MessageDisplayState;
    subtitle: string;
}

export enum MessageDisplayState {
    SELECT_TEMPLATE,
    FILL_TEMPLATE_PROPERTY_VALUES,
    GOOD_TO_PREVIEW,
}
