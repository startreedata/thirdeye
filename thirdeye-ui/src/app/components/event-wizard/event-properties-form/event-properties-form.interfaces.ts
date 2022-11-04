import { FieldErrors } from "react-hook-form/dist/types/errors";
import { FieldName } from "react-hook-form/dist/types/fields";
import { Control } from "react-hook-form/dist/types/form";
import { RegisterOptions } from "react-hook-form/dist/types/validator";
import { Event } from "../../../rest/dto/event.interfaces";

export interface EventPropertiesFormProps {
    formRegister: (name: FieldName<Event>, rules?: RegisterOptions) => void;
    formErrors: FieldErrors<Event>;
    formControl: Control<Event>;
    onSubmit?: (event: Event) => void;
    fullWidth?: boolean;
}

export interface DynamicFormType {
    key: string;
    propertyName: string;
    propertyValue: string[];
}
