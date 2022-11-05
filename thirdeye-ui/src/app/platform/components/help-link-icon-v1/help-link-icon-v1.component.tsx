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
