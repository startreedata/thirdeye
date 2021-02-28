import { SvgIconComponent } from "@material-ui/icons";

export interface ButtonTileProps {
    icon?: SvgIconComponent | SvgComponent; // Material-UI or custom SVG
    iconColor?: string;
    text?: string;
    disabled?: boolean;
    onClick?: () => void;
}
