import { IconButton, Tooltip } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { HelpLinkIconV1 } from "../../platform/components";
import { InfoIconWithTooltipProps } from "./info-icon-with-tooltip.interfaces";

export const InfoIconWithTooltip: FunctionComponent<
    InfoIconWithTooltipProps
> = ({ href, tooltipTitle }) => {
    return (
        <IconButton color="secondary">
            <Tooltip placement="top" title={tooltipTitle}>
                <HelpLinkIconV1 displayInline externalLink href={href} />
            </Tooltip>
        </IconButton>
    );
};
