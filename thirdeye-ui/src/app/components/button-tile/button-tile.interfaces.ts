export interface ButtonTileProps {
    icon?: SvgComponent;
    iconColor?: string;
    text?: string;
    searchWords?: string[];
    disabled?: boolean;
    onClick?: () => void;
}
