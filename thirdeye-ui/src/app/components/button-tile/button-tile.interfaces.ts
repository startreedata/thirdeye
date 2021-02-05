import { SvgIconComponent } from "@material-ui/icons";

export interface ButtonTileProps {
    icon?: SvgIconComponent | SvgComponent;
    iconColor?: string;
    text: string;
    disabled?: boolean;
    onClick: () => void;
}
