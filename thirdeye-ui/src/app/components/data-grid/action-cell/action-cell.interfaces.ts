export interface ActionCellProps {
    id: number;
    viewDetails?: boolean;
    edit?: boolean;
    delete?: boolean;
    onViewDetails?: (id: number) => void;
    onEdit?: (id: number) => void;
    onDelete?: (id: number) => void;
}
