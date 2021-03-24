import { GridCellParams } from "@material-ui/data-grid";

export interface LinkCellProps<T> {
    params: GridCellParams;
    searchWords?: string[];
    valueTextFn?: (value: T) => string;
    onClick?: (value: T, rowId: number) => void;
}
