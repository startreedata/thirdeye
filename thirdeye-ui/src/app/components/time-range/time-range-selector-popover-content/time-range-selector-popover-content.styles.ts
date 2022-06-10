///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


import { makeStyles } from "@material-ui/core";
import { Border } from "../../../utils/material-ui/border.util";

const HEIGHT_TIME_RANGE_SELECTOR_MD_UP = 427;

export const useTimeRangeSelectorPopoverStyles = makeStyles((theme) => ({
    timeRangeSelectorHeader: {
        borderBottom: Border.BORDER_DEFAULT,
    },
    timeRangeSelectorContents: {
        [theme.breakpoints.only("xs")]: {
            width: 342,
            paddingBottom: theme.spacing(1),
        },
        [theme.breakpoints.only("sm")]: {
            width: 465,
        },
        [theme.breakpoints.up("sm")]: {
            padding: 0,
        },
        [theme.breakpoints.up("md")]: {
            height: HEIGHT_TIME_RANGE_SELECTOR_MD_UP,
            width: 828,
        },
    },
    timeRangeList: {
        borderRight: Border.BORDER_DEFAULT,
        [theme.breakpoints.only("sm")]: {
            height: "100%",
        },
        [theme.breakpoints.up("md")]: {
            height: HEIGHT_TIME_RANGE_SELECTOR_MD_UP,
            overflowY: "auto",
        },
    },
    startTimeCalendarContainer: {
        [theme.breakpoints.up("md")]: {
            paddingRight: "0px !important",
        },
    },
    endTimeCalendarContainer: {
        [theme.breakpoints.up("md")]: {
            paddingLeft: "0px !important",
        },
    },
    startTimeCalendarLabel: {
        [theme.breakpoints.up("sm")]: {
            marginTop: 12,
            marginLeft: 12,
        },
    },
    endTimeCalendarLabel: {
        [theme.breakpoints.up("sm")]: {
            marginLeft: 12,
        },
        [theme.breakpoints.up("md")]: {
            marginTop: 12,
        },
    },
    calendar: {
        width: 310,
    },
}));
