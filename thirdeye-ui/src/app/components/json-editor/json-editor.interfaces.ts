export interface JSONEditorProps {
    value?: string;
    lineNumbers?: boolean;
    readOnly?: boolean;
    onChange?: (value: string) => void;
}
