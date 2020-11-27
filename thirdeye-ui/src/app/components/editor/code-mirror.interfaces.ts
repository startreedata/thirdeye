import { EditorConfiguration } from "codemirror";

export interface CommonCodeMirrorProps {
    options: EditorConfiguration;
    value?: string;
    defaultValue?: string;
    register?: (ref: Element | null) => void;
    name?: string;
    onChange?: (value: string) => void;
}
