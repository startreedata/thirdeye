/*
 * Copyright 2022 StarTree Inc
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
import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useSafariMuiGridFixStyles } from "./safari-mui-grid-fix.styles";

// Safari has issues calculating width of components with top level Material-UI Grid container and
// always wraps the last grid item in the container
// https://github.com/mui-org/material-ui/issues/17142
// This grid item with strictly 1px footprint can be added to the container to fix the issue
export const SafariMuiGridFix: FunctionComponent = () => {
    const safariMuiGridFixClasses = useSafariMuiGridFixStyles();

    return <Grid item className={safariMuiGridFixClasses.safariMuiGridFix} />;
};
