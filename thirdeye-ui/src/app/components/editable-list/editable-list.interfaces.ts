import { ValidationResult } from "../../utils/validation/validation.util";

export interface EditableListProps {
    list?: string[];
    inputLabel?: string;
    addButtonLabel?: string;
    validateFn?: (listItem: string) => ValidationResult; // Function that validates list item
    onChange?: (list: string[]) => void;
}
