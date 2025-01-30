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

type WorkspaceSwitcherProps = {
    workspaces: { id: string }[];
    selectedWorkspace: { id: string };
    onWorkspaceChange: (workspace: { id: string }) => void;
};

import {
    InputLabel,
    ListItem,
    ListItemIcon,
    ListItemText,
    TextField,
} from "@material-ui/core";
import MenuItem from "@material-ui/core/MenuItem";
import Typography from "@material-ui/core/Typography";
import CheckIcon from "@material-ui/icons/Check";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CopyButtonV2 } from "../../copy-button-v2/copy-button-v2.component";
import { useWorkpsaceSwitcherStyles } from "./app-header.styles";
import { isEmpty } from "lodash";

export const WorkspaceSwitcher = ({
    workspaces,
    selectedWorkspace,
    onWorkspaceChange,
}: WorkspaceSwitcherProps): JSX.Element => {
    const { t } = useTranslation();
    const classes = useWorkpsaceSwitcherStyles();
    const [filteredWorkspaces, setFilteredWorkspaces] = useState(workspaces);
    const [open, setOpen] = useState(false);

    const handleChangeWorkspace = (value: string | null): void => {
        if (!value) {
            return;
        }
        setFilteredWorkspaces(workspaces);
        onWorkspaceChange({ id: value });
    };

    const handleFilter = (value: string): void => {
        const newFilteredWorkspaces = workspaces.filter((workspace) => {
            const lowercaseWorkspaceId = workspace.id.toLowerCase();
            const lowercaseFilterWorkspaceId = value.toLowerCase();

            return lowercaseWorkspaceId.includes(lowercaseFilterWorkspaceId);
        });
        setFilteredWorkspaces(newFilteredWorkspaces);
    };

    useEffect(() => {
        if (open) {
            setTimeout(() => {
                const selectedWorkspaceElem = document.getElementById(
                    selectedWorkspace.id
                );
                selectedWorkspaceElem?.scrollIntoView();
            });
        }
    }, [open]);

    return (
        <div style={{ display: "flex", gap: "8px" }}>
            <InputLabel>
                <Typography variant="overline">
                    {t("label.app-header.workspace")}:
                </Typography>
            </InputLabel>

            <div className={classes.dropdown}>
                <TextField
                    autoFocus
                    select
                    SelectProps={{
                        open,
                        // eslint-disable-next-line react/display-name
                        renderValue: () => <div>{selectedWorkspace.id}</div>,
                    }}
                    value={selectedWorkspace.id || ""}
                    onClick={() => setOpen(!open)}
                >
                    <ListItem>
                        <TextField
                            fullWidth
                            placeholder="Search workspace"
                            onChange={(e) => {
                                handleFilter(e.target.value);
                            }}
                            // prevent selecting search box
                            onClick={(e) => e.stopPropagation()}
                        />
                    </ListItem>
                    <div style={{ maxHeight: "400px", overflow: "auto" }}>
                        {filteredWorkspaces.map((workspace, index) => (
                            <MenuItem
                                className={classes.menuItem}
                                id={workspace.id}
                                key={index}
                                value={workspace.id}
                                onClick={(event: React.MouseEvent) => {
                                    const elem = event.target as HTMLElement;
                                    handleChangeWorkspace(elem.textContent);
                                }}
                            >
                                <ListItemIcon style={{ minWidth: "40px" }}>
                                    {selectedWorkspace.id === workspace.id && (
                                        <CheckIcon
                                            color="primary"
                                            fontSize="small"
                                        />
                                    )}
                                </ListItemIcon>
                                <ListItemText style={{ marginRight: "16px" }}>
                                    {workspace.id}
                                </ListItemText>
                                <div onClick={(e) => e.stopPropagation()}>
                                    <CopyButtonV2 content={workspace.id} />
                                </div>
                            </MenuItem>
                        ))}
                        {isEmpty(filteredWorkspaces) && (
                            <div className={classes.noResults}>
                                No search results.
                            </div>
                        )}
                    </div>
                </TextField>
            </div>
        </div>
    );
};
