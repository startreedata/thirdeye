import { CellParams } from "@material-ui/data-grid";

export interface MultiValueCellProps<T> {
    params: CellParams;
    link?: boolean;
    searchWords?: string[];
    valueTextFn?: (value: T) => string;
    onClick?: (value: T, rowId: number) => void;
    onMore?: (rowId: number) => void;
}
