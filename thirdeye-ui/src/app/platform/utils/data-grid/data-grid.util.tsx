// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { isNil } from "lodash";
import React, {
    HTMLAttributeAnchorTarget,
    ReactElement,
    ReactNode,
} from "react";
import { LinkV1 } from "../../components/link-v1/link-v1.component";
import { TooltipV1 } from "../../components/tooltip-v1/tooltip-v1.component";

export const linkRendererV1 = (
    contents: ReactNode,
    href?: string,
    disabled?: boolean,
    tooltip?: boolean | ReactNode,
    externalLink?: boolean,
    target?: HTMLAttributeAnchorTarget,
    onClick?: () => void
): ReactElement => {
    // Determine tooltip contents
    const tooltipContents =
        isNil(tooltip) || (typeof tooltip === "boolean" && tooltip)
            ? contents
            : tooltip
            ? tooltip
            : "";

    if (tooltipContents) {
        return (
            <TooltipV1 title={tooltipContents}>
                <div>
                    <LinkV1
                        disabled={disabled}
                        externalLink={externalLink}
                        href={href}
                        target={target}
                        variant="body2"
                        onClick={onClick}
                    >
                        {contents}
                    </LinkV1>
                </div>
            </TooltipV1>
        ) as ReactElement;
    }

    return (
        <LinkV1
            disabled={disabled}
            externalLink={externalLink}
            href={href}
            target={target}
            variant="body2"
            onClick={onClick}
        >
            {contents}
        </LinkV1>
    ) as ReactElement;
};
