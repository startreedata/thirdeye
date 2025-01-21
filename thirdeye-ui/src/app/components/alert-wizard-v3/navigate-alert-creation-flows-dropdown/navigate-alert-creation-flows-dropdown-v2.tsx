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
import { Box, Grid, TextField } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS } from "./navigate-alert-creation-flows-dropdown.interface";
import { useNavigateAlertCreationFlowsDropdownStyles } from "./navigate-alert-creation-flows-dropdown.styles";

export const NavigateAlertCreationFlowsDropdown: FunctionComponent = () => {
    const { t } = useTranslation();
    const classes = useNavigateAlertCreationFlowsDropdownStyles();
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    /**
     * `/alerts/73/update/simple`
     * `/alerts/create/new/advanced`
     * `/alerts/create/new/new-user/select-type`
     * `/alerts/create/copy/69689/json-editor`
     * `/alerts/create/copy/69689/new-user/select-type`
     * `/welcome/create-alert/simple`
     * `/welcome/create-alert/new-user/select-type`
     */
    const routePathPrefix = useMemo(() => {
        let endIdx = 4;

        if (location.pathname.includes(AppRouteRelative.WELCOME)) {
            endIdx = 3;
        }

        if (
            location.pathname.includes(
                AppRouteRelative.ALERTS_CREATE_COPY.replace("/:id", "")
            )
        ) {
            endIdx = 5;
        }

        return location.pathname.split("/").slice(0, endIdx).join("/");
    }, [location]);

    const shortcutCreateMenuItems = [
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.ALERTS_CREATE_ADVANCED_V2),
            navLink: AppRouteRelative.ALERTS_CREATE_ADVANCED_V2,
            text: t("label.advanced-setup"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.ALERTS_CREATE_JSON_EDITOR_V2),
            navLink: AppRouteRelative.ALERTS_CREATE_JSON_EDITOR_V2,
            text: t("label.json-setup"),
        },
    ];

    const currentPage = useMemo(() => {
        return shortcutCreateMenuItems.find((candidate) => {
            return candidate.matcher(location.pathname);
        });
    }, [location]);

    return (
        <Grid container alignContent="center">
            <Grid item>
                <Box paddingTop={1}>
                    {t("label.alert-editor")}
                    {": "}
                </Box>
            </Grid>

            <Grid item>
                <Autocomplete
                    disableClearable
                    className={classes.autocomplete}
                    data-testid={
                        ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS.DROPDOWN_CONTAINER
                    }
                    getOptionLabel={(option) => option.text}
                    options={shortcutCreateMenuItems}
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            InputProps={{
                                ...params.InputProps,
                                /**
                                 * Override class name so
                                 * the size of input is smaller
                                 */
                                className: classes.autoCompleteInput,
                            }}
                            variant="outlined"
                        />
                    )}
                    size="small"
                    value={currentPage}
                    onChange={(_, selectedValue) => {
                        navigate(
                            `${routePathPrefix}/${
                                selectedValue.navLink
                            }?${searchParams.toString()}`
                        );
                    }}
                />
            </Grid>
        </Grid>
    );
};
