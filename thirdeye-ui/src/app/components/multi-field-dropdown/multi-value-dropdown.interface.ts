export interface MultiFieldDropdownProps {
    label: string;
    name: string;
    options: Array<MultiFieldValueOption>;
}

export interface MultiFieldValueOption {
    value: string;
    label: string;
    fields?: Array<Field>;
}

export interface Field {
    name: string;
    label: string;
    type: FieldType;
    options?: Array<SelectOption | string>;
}

export type FieldType = "text" | "number" | "select";

export interface SelectOption {
    label: string;
    value: string;
}
