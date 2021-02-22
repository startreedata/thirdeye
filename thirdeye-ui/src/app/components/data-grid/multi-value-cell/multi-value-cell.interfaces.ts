export interface MultiValueCellProps<T> {
    id: number;
    values: T[];
    link?: boolean;
    searchWords?: string[];
    valueClassName?: string;
    valueTextFn?: (value: T) => string;
    onClick?: (value: T) => void;
    onMore?: (id: number) => void;
}
