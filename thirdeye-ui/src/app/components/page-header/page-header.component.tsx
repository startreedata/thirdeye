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
import { useMediaQuery, useTheme } from "@material-ui/core";
import {
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageHeaderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent } from "react";
import { useSearchParams } from "react-router-dom";
import { getTimeRangeDuration } from "../../utils/time-range/time-range.util";
import { CreateMenuButton } from "../create-menu-button.component/create-menu-button.component";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import {
    TimeRangeDuration,
    TimeRangeQueryStringKey,
} from "../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelector } from "../time-range/time-range-selector/time-range-selector/time-range-selector.component";
import { PageHeaderProps } from "./page-header.interfaces";

export const PageHeader: FunctionComponent<PageHeaderProps> = (
    props: PageHeaderProps
) => {
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
    } = useTimeRange();
    const theme = useTheme();
    const [searchParams, setSearchParams] = useSearchParams();

    const screenWidthSmUp = useMediaQuery(theme.breakpoints.up("sm"));

    const onHandleTimeRangeChange = (
        timeRangeDuration: TimeRangeDuration
    ): void => {
        setTimeRangeDuration(timeRangeDuration);
        searchParams.set(
            TimeRangeQueryStringKey.TIME_RANGE,
            timeRangeDuration.timeRange
        );
        searchParams.set(
            TimeRangeQueryStringKey.START_TIME,
            timeRangeDuration.startTime.toString()
        );
        searchParams.set(
            TimeRangeQueryStringKey.END_TIME,
            timeRangeDuration.endTime.toString()
        );
        setSearchParams(searchParams);
    };

    const onHandleRefresh = (): void => {
        onHandleTimeRangeChange(
            getTimeRangeDuration(timeRangeDuration.timeRange)
        );
    };

    return (
        <PageHeaderV1>
            <PageHeaderTextV1>
                {props.title}
                {props.children}
            </PageHeaderTextV1>

            <PageHeaderActionsV1>
                {/* Time range selector */}
                {props.showTimeRange && (
                    <TimeRangeSelector
                        hideTimeRange={!screenWidthSmUp}
                        recentCustomTimeRangeDurations={
                            recentCustomTimeRangeDurations
                        }
                        timeRangeDuration={timeRangeDuration}
                        onChange={onHandleTimeRangeChange}
                        onRefresh={onHandleRefresh}
                    />
                )}

                {/* Create options button */}
                {props.showCreateButton && <CreateMenuButton />}
            </PageHeaderActionsV1>
        </PageHeaderV1>
    );
};
