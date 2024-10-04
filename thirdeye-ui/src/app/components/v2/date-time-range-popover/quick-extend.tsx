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
import { Button } from "@material-ui/core";
import React from "react";
import { TooltipV1 } from "../../../platform/components";
import { KeyboardArrowLeft, KeyboardArrowRight } from "@material-ui/icons";
import { useTranslation } from "react-i18next";

export type Direction = "backward" | "forward";

type QuickExtendProps = {
    disabled: boolean;
    handleQuickExtend: (direction: Direction) => void;
    direction: Direction;
};

export const QuickExtend = ({
    disabled,
    handleQuickExtend,
    direction,
}: QuickExtendProps): JSX.Element => {
    const { t } = useTranslation();
    const renderIcon = (): JSX.Element => {
        if (direction === "backward") {
            return <KeyboardArrowLeft style={{ fontSize: "0.9375rem" }} />;
        } else {
            return <KeyboardArrowRight style={{ fontSize: "0.9375rem" }} />;
        }
    };

    return (
        <Button
            color="secondary"
            disabled={disabled}
            variant="outlined"
            onClick={() => handleQuickExtend(direction)}
        >
            <TooltipV1
                placement="top"
                title={t("message.extend-entity-by-week", {
                    entity: `${t("label.time-range")} ${t("label.start")}`,
                })}
            >
                {renderIcon()}
            </TooltipV1>
        </Button>
    );
};
