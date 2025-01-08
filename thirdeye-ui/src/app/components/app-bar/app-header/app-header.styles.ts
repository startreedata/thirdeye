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
import { AppHeaderStyleProps } from "./app-header.inerfaces";

export const useAppHeaderStyles = (
    props: AppHeaderStyleProps
): ReturnType<typeof makeStyles> =>
    makeStyles({
        header: {
            width: props.isFullScreen
                ? "100%"
                : props.navBarMinimized
                ? "calc(100% - 64px)"
                : "calc(100% - 180px)",
            position: "fixed",
            height: "40px",
            padding: "8px 16px",
            background: "#e1edff",
            display: "flex",
            zIndex: 9999999,
            justifyContent: props.showWorkspaceSwitcher
                ? "space-between"
                : "end",
            gap: "50px",
        },
        selectEmpty: {
            padding: 0,
            borderBottom: 0,
            fontWeight: "bold",
        },
        dropdownItem: {
            fontWeight: "bold",
        },
        button: {
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "0 8px",
            background: "#fff",
            borderRadius: "5px",
            border: "1px solid #bed3e4",
            fontWeight: "bold",
            color: "#093d86",
        },
        icon: {
            height: "16px",
            width: "16px",
        },
    });

export const useWorkpsaceSwitcherStyles = makeStyles({
    dropdown: {
        background: "white",
        border: "1px solid #bed3e4",
        borderRadius: "5px",
        paddingLeft: "8px",
        "& .MuiInputBase-root": {
            minWidth: "200px",
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
        "& .MuiListItemText-primary": {
            fontSize: "14px",
        },
    },
    noResults: {
        display: "flex",
        justifyContent: "center",
    },
});
