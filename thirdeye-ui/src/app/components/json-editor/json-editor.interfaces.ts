export interface JSONEditorProps {
    value?: string | Record<string, unknown>;
    helperText?: string;
    error?: boolean;
    readOnly?: boolean;
    onChange?: (value: string) => void;
}
