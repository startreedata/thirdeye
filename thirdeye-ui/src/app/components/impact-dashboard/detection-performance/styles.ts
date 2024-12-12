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

export const useAlertDrodownStyles = makeStyles({
    dropdown: {
        background: "white",
        border: "1px solid #bed3e4",
        borderRadius: "5px",
        paddingLeft: "8px",
        "& .MuiInputBase-root": {
            width: "150px",
            borderRadius: 0,
        },
        "& .MuiSelect-root": {
            padding: 0,
            "&:focus": {
                backgroundColor: "unset",
            },
        },
        "& .MuiSvgIcon-root": {},
        "& .MuiFormLabel-root": {
            transform: "translate(12px, 12px) ",
        },
        "& .MuiOutlinedInput-notchedOutline": {
            border: "none",
        },
    },
    menuItem: {
        "& .copy-icon-button": {
            visibility: "hidden",
        },
        "&:hover .copy-icon-button": {
            visibility: "visible",
        },
    },
    noResults: {
        display: "flex",
        justifyContent: "center",
    },
    selectedLabel: {
        width: "80%",
        textOverflow: "ellipsis",
        overflow: "hidden",
    },
    searchInput: {
        boxShadow:
            "rgba(0, 0, 0, 0.16) 0px 3px 6px, rgba(0, 0, 0, 0.23) 0px 3px 6px",
    },
});
