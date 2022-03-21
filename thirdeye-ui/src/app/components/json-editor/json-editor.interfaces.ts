export interface JSONEditorProps {
    value?: string | Record<string, unknown>;
    helperText?: string;
    error?: boolean;
    readOnly?: boolean;
    className?: string;
    onChange?: (value: string) => void;
}
