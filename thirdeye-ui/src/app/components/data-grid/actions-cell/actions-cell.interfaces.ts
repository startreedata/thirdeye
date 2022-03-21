import { GridCellParams } from "@material-ui/data-grid";

export interface ActionsCellProps {
    params: GridCellParams;
    viewDetails?: boolean;
    edit?: boolean;
    delete?: boolean;
    onViewDetails?: (rowId: number) => void;
    onEdit?: (rowId: number) => void;
    onDelete?: (rowId: number) => void;
}
