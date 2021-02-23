export interface LinkCellProps<T> {
    rowId: number;
    value: T;
    align?: string;
    searchWords?: string[];
    valueTextFn?: (value: T) => string;
    onClick?: (value: T, rowId: number) => void;
}
