/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import Tooltip from "@material-ui/core/Tooltip";
import IconButton from "@material-ui/core/IconButton";
import CheckIcon from "@material-ui/icons/CheckCircleOutlineOutlined";
import CopyIcon from "@material-ui/icons/FileCopyOutlined";
import React, { FunctionComponent, useMemo, useState } from "react";
import { LocalThemeProviderV1 } from "../../platform/components";
import { lightV1 } from "../../platform/utils";
import { useTranslation } from "react-i18next";
import { copyToClipboard } from "../../utils/browser/browser.utils";
import { CopyButtonProps } from "./copy-button.interfaces";

export const CopyButton: FunctionComponent<CopyButtonProps> = ({
    content,
    beforeCopyTooltip,
    afterCopyTooltip,
    successTickDuration = 2000,
    tooltipProps,
    iconButtonProps,
    iconProps,
}) => {
    const { t } = useTranslation();
    const [showCopyTick, setShowCopyTick] = useState(false);

    const handleCopyContent = (content: string): void => {
        copyToClipboard(content);
        setShowCopyTick(true);

        setTimeout(() => {
            setShowCopyTick(false);
        }, successTickDuration);
    };

    const tooltipTitle = useMemo(() => {
        if (showCopyTick) {
            return afterCopyTooltip ?? t("label.copied-to-clipboard");
        }

        return beforeCopyTooltip ?? t("label.copy-to-clipboard");
    }, [showCopyTick, beforeCopyTooltip, afterCopyTooltip]);

    return (
        <Tooltip {...tooltipProps} title={tooltipTitle}>
            <IconButton
                color="secondary"
                size="small"
                {...iconButtonProps}
                onClick={(e) => {
                    handleCopyContent(content);
                    iconButtonProps?.onClick?.(e);
                }}
            >
                {showCopyTick ? (
                    <LocalThemeProviderV1 primary={lightV1.palette.success}>
                        <CheckIcon
                            color="primary"
                            fontSize="small"
                            {...iconProps}
                        />
                    </LocalThemeProviderV1>
                ) : (
                    <CopyIcon fontSize="small" {...iconProps} />
                )}
            </IconButton>
        </Tooltip>
    );
};
