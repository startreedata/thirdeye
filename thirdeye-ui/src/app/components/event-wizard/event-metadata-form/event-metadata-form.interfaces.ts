
export interface EventMetadataFormProps {
    initialPropertiesData: PropertyData[];
    onChange: (newPropertiesData: PropertyData[]) => void;
}

export interface PropertyData {
    originalKey: string | null;
    propertyName: string;
    propertyValue: string[];
}
