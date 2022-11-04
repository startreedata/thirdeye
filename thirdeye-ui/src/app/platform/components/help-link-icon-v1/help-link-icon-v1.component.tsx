// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import HelpOutlineIcon from "@material-ui/icons/HelpOutline";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { LinkV1 } from "../link-v1/link-v1.component";
import { HelpLinkIconV1Props } from "./help-link-icon-v1.interfaces";
import { useHelpLinkIconV1Styles } from "./help-link-icon-v1.styles";

export const HelpLinkIconV1: FunctionComponent<HelpLinkIconV1Props> = ({
    href,
    externalLink,
    displayInline,
    enablePadding,
    className,
    ...otherProps
}) => {
    const helpLinkIconV1Classes = useHelpLinkIconV1Styles();

    return (
        <LinkV1
            {...otherProps}
            className={classNames(className, "help-link-icon-v1")}
            externalLink={externalLink}
            href={href}
            target="_blank"
            variant="body2"
        >
            {/* Help icon */}
            <div
                className={classNames({
                    [helpLinkIconV1Classes.helpIconFlex]: !displayInline,
                    [helpLinkIconV1Classes.helpIconInline]: displayInline,
                    [helpLinkIconV1Classes.helpIconPadding]: enablePadding,
                })}
            >
                <HelpOutlineIcon color="secondary" fontSize="small" />
            </div>
        </LinkV1>
    );
};
