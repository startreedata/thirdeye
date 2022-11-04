import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { useTileButtonV1 } from "../tile-button-v1";
import { TileButtonIconV1Props } from "./tile-button-icon-v1.interfaces";
import { useTileButtonIconV1Styles } from "./tile-button-icon-v1.styles";

export const TileButtonIconV1: FunctionComponent<TileButtonIconV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const tileButtonIconV1Classes = useTileButtonIconV1Styles();
    const { disabled } = useTileButtonV1();

    return (
        <div
            {...otherProps}
            className={classNames(
                tileButtonIconV1Classes.tileButtonIcon,
                disabled
                    ? tileButtonIconV1Classes.tileButtonIconDisabled
                    : tileButtonIconV1Classes.tileButtonIconEnabled,
                className,
                "tile-button-icon-v1"
            )}
        >
            {children}
        </div>
    );
};
