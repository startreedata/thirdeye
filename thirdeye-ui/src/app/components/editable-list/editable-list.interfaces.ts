import { ValidationResult } from "../../utils/validation/validation.util";

export interface EditableListProps {
    list?: string[];
    inputLabel?: string;
    addButtonLabel?: string;
    validateFn?: (listItem: string) => ValidationResult;
    onChange?: (list: string[]) => void;
}
