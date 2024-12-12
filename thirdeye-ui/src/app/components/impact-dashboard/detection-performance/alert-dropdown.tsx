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

// type AlertDropdownProps = {
//   alerts: { id: string }[];
//   selectedAlert: { id: string };
//   onAlertChange: (workspace: { id: string }) => void;
// };

type AlertDropdownProps = {
    alerts?: { id: number; name: string }[];
    selectedAlert: { id: number; name: string };
    onAlertChange: any;
};

import {
    ListItem,
    ListItemIcon,
    ListItemText,
    TextField,
    Tooltip,
} from "@material-ui/core";
import MenuItem from "@material-ui/core/MenuItem";
import Typography from "@material-ui/core/Typography";
import CheckIcon from "@material-ui/icons/Check";
import React, { useEffect, useState } from "react";
import { useAlertDrodownStyles } from "./styles";
import { isEmpty } from "lodash";

export const AlertDropdown = ({
    alerts,
    selectedAlert,
    onAlertChange,
}: AlertDropdownProps): JSX.Element => {
    const classes = useAlertDrodownStyles();
    const [filteredAlerts, setFilteredAlerts] = useState(alerts);
    const [open, setOpen] = useState(false);

    const handleChangeAlert = (value: string | null): void => {
        if (!value) {
            return;
        }
        setFilteredAlerts(alerts);
        onAlertChange(value);
    };

    const handleFilter = (value: string): void => {
        const newFilteredAlerts = alerts?.filter((alert) => {
            const lowercaseWorkspaceId = alert.name.toLowerCase();
            const lowercaseFilterWorkspaceId = value.toLowerCase();

            return lowercaseWorkspaceId.includes(lowercaseFilterWorkspaceId);
        });
        setFilteredAlerts(newFilteredAlerts);
    };

    useEffect(() => {
        if (open) {
            setTimeout(() => {
                const selectedWorkspaceElem = document.getElementById(
                    selectedAlert.name
                );
                selectedWorkspaceElem?.scrollIntoView();
            });
        }
    }, [open]);

    useEffect(() => {
        setFilteredAlerts(alerts);
    }, [alerts]);

    return (
        <div className={classes.dropdown}>
            <TextField
                autoFocus
                fullWidth
                select
                SelectProps={{
                    open,
                    // eslint-disable-next-line react/display-name
                    renderValue: () => {
                        if (selectedAlert.name.length > 10) {
                            return (
                                <Tooltip
                                    arrow
                                    interactive
                                    placement="top"
                                    title={selectedAlert?.name}
                                >
                                    <Typography
                                        className={classes.selectedLabel}
                                        variant="subtitle1"
                                    >
                                        {selectedAlert?.name}
                                    </Typography>
                                </Tooltip>
                            );
                        }

                        return (
                            <Typography
                                className={classes.selectedLabel}
                                variant="subtitle1"
                            >
                                {selectedAlert?.name}
                            </Typography>
                        );
                    },
                }}
                value={selectedAlert?.name || ""}
                onClick={() => setOpen(!open)}
            >
                {alerts && alerts?.length > 1 && (
                    <ListItem className={classes.searchInput}>
                        <TextField
                            fullWidth
                            placeholder="Search alerts"
                            onChange={(e) => {
                                handleFilter(e.target.value);
                            }}
                            // prevent selecting search box
                            onClick={(e) => e.stopPropagation()}
                        />
                    </ListItem>
                )}
                <div style={{ maxHeight: "400px", overflow: "auto" }}>
                    {filteredAlerts?.map((alert, index) => (
                        <MenuItem
                            className={classes.menuItem}
                            id={alert.name}
                            key={index}
                            value={alert.name}
                            onClick={(event: React.MouseEvent) => {
                                const elem = event.target as HTMLElement;
                                handleChangeAlert(elem.textContent);
                            }}
                        >
                            <ListItemIcon style={{ minWidth: "40px" }}>
                                {selectedAlert.id === alert.id && (
                                    <CheckIcon
                                        color="primary"
                                        fontSize="small"
                                    />
                                )}
                            </ListItemIcon>
                            <ListItemText style={{ marginRight: "16px" }}>
                                {alert.name}
                            </ListItemText>
                        </MenuItem>
                    ))}
                    {isEmpty(filteredAlerts) && (
                        <div className={classes.noResults}>
                            No search results.
                        </div>
                    )}
                </div>
            </TextField>
        </div>
    );
};
