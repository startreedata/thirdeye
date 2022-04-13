import { IconButton, Tooltip } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { InfoIconV1 } from "../../platform/components";
import { InfoIconWithTooltipProps } from "./info-icon-with-tooltip.interfaces";

export const InfoIconWithTooltip: FunctionComponent<
    InfoIconWithTooltipProps
> = ({ href, target, tooltipTitle }) => {
    if (tooltipTitle) {
        return (
            <a href={href} rel="noreferrer" target={target}>
                <IconButton color="secondary">
                    <Tooltip placement="top" title={tooltipTitle}>
                        <InfoIconV1 />
                    </Tooltip>
                </IconButton>
            </a>
        );
    } else {
        return (
            <a href={href} rel="noreferrer" target={target}>
                <IconButton color="secondary">
                    <InfoIconV1 />
                </IconButton>
            </a>
        );
    }
};
