import { SvgIconComponent } from "@material-ui/icons";

export interface ButtonTileProps {
    icon?: SvgIconComponent | SvgComponent;
    text: string;
    disabled?: boolean;
    onClick: () => void;
}
