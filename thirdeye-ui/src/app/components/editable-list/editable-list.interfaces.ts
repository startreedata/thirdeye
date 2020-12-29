export interface EditableListProps {
    list: string[];
    inputLabel: string;
    buttonLabel: string;
    onChange: (t: string[]) => void;
    /**
     * To validate entered string is valid or not
     * @param t string value
     * @returns ValidationError which contains message & valid flag
     */
    validate?: (t: string) => ValidationError;
}

export interface ValidationError {
    message: string;
    valid: boolean;
}
