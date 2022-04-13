import { IconButton, Tooltip } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { HelpLinkIconV1 } from "../../platform/components";
import { InfoIconWithTooltipProps } from "./help-icon-with-tooltip.interfaces";

export const HelpIconWithTooltip: FunctionComponent<
    InfoIconWithTooltipProps
> = ({ href, tooltipTitle }) => {
    return (
        <Tooltip placement="top" title={tooltipTitle}>
            <IconButton color="secondary">
                <HelpLinkIconV1 displayInline externalLink href={href} />
            </IconButton>
        </Tooltip>
    );
};
