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
import { Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { Outlet, useOutletContext } from "react-router-dom";
import { WhatWhereNavigation } from "../../../components/rca/what-where-navigation/what-where-navigation.component";
import { InvestigationContext } from "../../root-cause-analysis-investigation-state-tracker/investigation-state-tracker.interfaces";

export const WhatWherePage: FunctionComponent = () => {
    const context = useOutletContext<InvestigationContext>();

    return (
        <>
            <Typography variant="h4">What went wrong and where?</Typography>
            <WhatWhereNavigation />
            <Outlet context={context} />
        </>
    );
};
