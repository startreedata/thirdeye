import { Button, Typography, useTheme } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { ButtonTileProps } from "./button-tile.interfaces";
import { useButtonTileStyles } from "./button-tile.styles";

const HEIGHT_ICON = 55;

export const ButtonTile: FunctionComponent<ButtonTileProps> = (
    props: ButtonTileProps
) => {
    const buttonTileClasses = useButtonTileStyles();
    const [iconProps, setIconProps] = useState<Record<string, unknown>>();
    const theme = useTheme();

    useEffect(() => {
        // Icon color changed or buton enabled/disabled, initialize icon properties
        initIconProps();
    }, [props.iconColor, props.disabled]);

    const initIconProps = (): void => {
        const properties: Record<string, unknown> = {};
        properties.height = HEIGHT_ICON;
        // To retain original custom SVG colors, SVG fill to be assigned only if icon color provided
        // or button disabled
        if (props.iconColor) {
            properties.fill = props.iconColor;
        }
        if (props.disabled) {
            properties.fill = theme.palette.action.disabled;
            properties.opacity = 0.5;
        }
        setIconProps(properties);
    };

    return (
        // Using ButtonBase doesn't provide hover and active effects out of the box
        // Using Button instead helps avoid duplicating those effects
        <Button
            classes={{
                root: buttonTileClasses.buttonRoot,
                label: buttonTileClasses.buttonLabel,
            }}
            disabled={props.disabled}
            variant="contained"
            onClick={props.onClick}
        >
            {/* Icon */}
            {props.icon && (
                <div className={buttonTileClasses.iconContainer}>
                    <props.icon {...iconProps} />
                </div>
            )}

            {/* Text */}
            {props.text && (
                <div className={buttonTileClasses.textContainer}>
                    <Typography
                        className={buttonTileClasses.text}
                        variant="subtitle1"
                    >
                        <TextHighlighter
                            searchWords={props.searchWords}
                            text={props.text}
                        />
                    </Typography>
                </div>
            )}
        </Button>
    );
};
