// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Popover } from "@material-ui/core";
import InfoOutlinedIcon from "@material-ui/icons/InfoOutlined";
import classNames from "classnames";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { LinkV1 } from "../link-v1/link-v1.component";
import { InfoIconV1Props } from "./info-icon-v1.interfaces";
import { useInfoIconV1Styles } from "./info-icon-v1.styles";

export const InfoIconV1: FunctionComponent<InfoIconV1Props> = ({
    autoFitToContents,
    displayInline,
    enablePadding,
    className,
    children,
    ...otherProps
}) => {
    const infoIconV1Classes = useInfoIconV1Styles();
    const [infoElement, setInfoElement] = useState<HTMLElement | null>();

    const handleIconClick = (event: MouseEvent<HTMLElement>): void => {
        setInfoElement(event.currentTarget);
    };

    const handleInfoClose = (): void => {
        setInfoElement(null);
    };

    return (
        <>
            <LinkV1
                {...otherProps}
                className={classNames(className, "info-icon-v1")}
                variant="body2"
                onClick={handleIconClick}
            >
                {/* Info icon */}
                <div
                    className={classNames({
                        [infoIconV1Classes.infoIconFlex]: !displayInline,
                        [infoIconV1Classes.infoIconInline]: displayInline,
                        [infoIconV1Classes.infoIconPadding]: enablePadding,
                    })}
                >
                    <InfoOutlinedIcon color="secondary" fontSize="small" />
                </div>
            </LinkV1>

            {children && (
                // Info
                <Popover
                    anchorEl={infoElement}
                    anchorOrigin={{
                        horizontal: "right",
                        vertical: "top",
                    }}
                    classes={{
                        paper: autoFitToContents
                            ? infoIconV1Classes.autoFitToContents
                            : infoIconV1Classes.defaultSizing,
                    }}
                    open={Boolean(infoElement)}
                    onClose={handleInfoClose}
                >
                    <div className={infoIconV1Classes.info}>{children}</div>
                </Popover>
            )}
        </>
    );
};
