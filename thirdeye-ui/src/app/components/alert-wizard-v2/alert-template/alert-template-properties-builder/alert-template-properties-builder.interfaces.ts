import { TemplatePropertiesObject } from "../../../../rest/dto/alert.interfaces";

export interface AlertTemplatePropertiesBuilderProps {
    alertTemplateId: number;
    requiredFields: string[];
    templateProperties: TemplatePropertiesObject;
    defaultTemplateProperties: TemplatePropertiesObject;
    onPropertyValueChange: (newChanges: TemplatePropertiesObject) => void;
}

export interface PropertyRenderConfig {
    key: string;
    value: string | string[] | boolean;
    defaultValue: string | string[] | boolean;
}
