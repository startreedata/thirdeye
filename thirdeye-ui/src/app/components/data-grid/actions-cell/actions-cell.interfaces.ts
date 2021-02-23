import { CellParams } from "@material-ui/data-grid";

export interface ActionsCellProps {
    params: CellParams;
    viewDetails?: boolean;
    edit?: boolean;
    delete?: boolean;
    onViewDetails?: (rowId: number) => void;
    onEdit?: (rowId: number) => void;
    onDelete?: (rowId: number) => void;
}
