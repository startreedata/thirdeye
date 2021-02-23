export interface ActionsCellProps {
    rowId: number;
    viewDetails?: boolean;
    edit?: boolean;
    delete?: boolean;
    onViewDetails?: (rowId: number) => void;
    onEdit?: (rowId: number) => void;
    onDelete?: (rowId: number) => void;
}
