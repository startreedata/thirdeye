import { ValidationResult } from "../../utils/validation/validation.util";

export interface EditableListProps {
    list?: string[];
    inputLabel?: string;
    addButtonLabel?: string;
    loading?: boolean;
    validateFn?: (listItem: string) => ValidationResult;
    onChange?: (list: string[]) => void;
}
