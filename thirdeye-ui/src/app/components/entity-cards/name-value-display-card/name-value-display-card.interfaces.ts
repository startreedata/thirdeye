export interface NameValueDisplayCardProps<T> {
    name: string;
    values: T[];
    showCount?: boolean;
    link?: boolean;
    searchWords?: string[];
    valueClassName?: string;
    valueTextFn?: (value: T) => string; // Function that returns text to be displayed for value
    onClick?: (value: T) => void;
}
