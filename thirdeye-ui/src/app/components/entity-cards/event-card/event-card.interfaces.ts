import { UiEvent } from "../../../rest/dto/ui-event.interfaces";

export interface EventCardProps {
    event: UiEvent | null;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (event: UiEvent) => void;
    onEdit?: (id: number) => void;
}
