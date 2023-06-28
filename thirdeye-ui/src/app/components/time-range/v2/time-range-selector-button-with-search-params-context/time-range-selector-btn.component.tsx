/*
 * Copyright 2023 StarTree Inc
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
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { TimeRangeQueryStringKey } from "../../time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelectorButton } from "../time-range-selector-button/time-range-selector-button.component";
import { TimeRangeSelectorButtonWithSearchParamsContextProps } from "./time-range-selector-btn.interfaces";

export const TimeRangeSelectorButtonWithSearchParamsContext: FunctionComponent<TimeRangeSelectorButtonWithSearchParamsContextProps> =
    (props) => {
        const { t } = useTranslation();
        const [searchParams, setSearchParams] = useSearchParams();

        const [startTime, endTime] = useMemo(() => {
            return [
                Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
                Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
            ];
        }, [searchParams]);

        const onHandleTimeRangeChange = (
            startProp: number,
            endProp: number
        ): void => {
            searchParams.set(
                TimeRangeQueryStringKey.START_TIME,
                startProp.toString()
            );
            searchParams.set(
                TimeRangeQueryStringKey.END_TIME,
                endProp.toString()
            );
            setSearchParams(searchParams);
        };

        return (
            <TimeRangeSelectorButton
                {...props}
                end={endTime}
                placeholder={t("message.click-to-select-date-range")}
                start={startTime}
                onChange={(start, end) => {
                    onHandleTimeRangeChange(start, end);
                }}
            />
        );
    };
