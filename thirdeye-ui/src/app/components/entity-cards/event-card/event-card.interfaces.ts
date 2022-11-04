import { Event } from "../../../rest/dto/event.interfaces";

export interface EventCardProps {
    event: Event | null;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (event: Event) => void;
    onEdit?: (id: number) => void;
}
