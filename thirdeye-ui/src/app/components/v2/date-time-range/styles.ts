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
import { makeStyles } from "@material-ui/core";

export const useDateTimeRangeStyles = makeStyles({
    container: {
        display: "flex",
        border: "1px solid #0000001F",
        // padding: '10px'
        maxHeight: "480px",
    },
    dateTimecontainer: {
        display: "flex",
        gap: "20px",
        flexDirection: "column",
        alignItems: "end",
        padding: "10px",
    },
    dateTimePicker: {
        display: "flex",
        gap: "10px",
    },
    actionButtons: {
        display: "flex",
        gap: "20px",
    },
    separator: {
        width: "1px",
        border: "1px solid #0000001F",
    },
    quickSelect: {
        overflow: "auto",
        width: "200px",
    },
    calendarHeader: {
        marginLeft: "24px",
        "& .label": {
            fontWeight: 500,
        },
        "& .date": {
            color: "#00A3DE;",
        },
    },
});
