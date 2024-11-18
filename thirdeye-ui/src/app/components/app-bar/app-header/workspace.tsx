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
import React, { useMemo, useState } from "react";
import { Autocomplete } from "@material-ui/lab";
import { Box, MenuItem, TextField } from "@material-ui/core";
import { CopyButtonV2 } from "../../copy-button-v2/copy-button-v2.component";
import { useWorkpsaceSwitcherStyles } from "./app-header.styles";
import { useTranslation } from "react-i18next";

type WorkspaceSwitcherProps = {
    workspaces: { id: string }[];
    selectedWorkspace: { id: string };
    onWorkspaceChange: (workspace: { id: string }) => void;
};

type DropdownOption = {
    id: string;
    label: string;
};

export const WorkspaceSwitcher = ({
    workspaces,
    selectedWorkspace,
    onWorkspaceChange,
}: WorkspaceSwitcherProps): JSX.Element => {
    const { t } = useTranslation();
    const classes = useWorkpsaceSwitcherStyles();
    const [selectedOption, setSelectedOption] = useState({
        id: selectedWorkspace.id,
        label: selectedWorkspace.id,
    });
    const workspaceOptions = useMemo(() => {
        return workspaces.map((workspace) => {
            return {
                id: workspace.id,
                label: workspace.id,
            };
        });
    }, [workspaces]);

    const handleWorkspaceChange = (option: DropdownOption): void => {
        setSelectedOption(option);
        onWorkspaceChange({ id: option.id });
    };

    const renderAlertTemplateSelectOption = (
        option: DropdownOption
    ): JSX.Element => {
        return (
            <MenuItem
                key={option.id}
                style={{
                    width: "100%",
                    background:
                        option.id === selectedOption.id ? "#e1edff" : "",
                }}
                value={option.id}
            >
                <Box
                    display="flex"
                    justifyContent="space-between"
                    padding="8px"
                    width="100%"
                >
                    <div>{option.id}</div>
                    <div onClick={(e) => e.stopPropagation()}>
                        <CopyButtonV2
                            afterCopyTooltip="Copied"
                            content={option.id}
                        />
                    </div>
                </Box>
            </MenuItem>
        );
    };

    return (
        <div className={classes.workspaceContainer}>
            <div>{t("label.app-header.workspace")}:</div>
            <Autocomplete
                disableClearable
                fullWidth
                className={classes.autocomplete}
                classes={{
                    listbox: classes.listbox,
                }}
                getOptionLabel={(option: { id: string; label: string }) =>
                    option.label as string
                }
                noOptionsText={t("message.no-search-results")}
                options={workspaceOptions}
                renderInput={(params) => (
                    <TextField
                        {...params}
                        InputLabelProps={{
                            shrink: false,
                        }}
                        InputProps={{
                            ...params.InputProps,
                        }}
                        label={selectedOption ? "" : "Select Workspace"}
                        variant="outlined"
                    />
                )}
                renderOption={renderAlertTemplateSelectOption}
                value={selectedOption}
                onChange={(_, selectedValue) => {
                    handleWorkspaceChange(selectedValue);
                }}
            />
        </div>
    );
};
