/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
