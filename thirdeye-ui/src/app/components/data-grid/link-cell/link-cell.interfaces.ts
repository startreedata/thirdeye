import { CellParams } from "@material-ui/data-grid";

export interface LinkCellProps<T> {
    params: CellParams;
    searchWords?: string[];
    valueTextFn?: (value: T) => string;
    onClick?: (value: T, rowId: number) => void;
}
