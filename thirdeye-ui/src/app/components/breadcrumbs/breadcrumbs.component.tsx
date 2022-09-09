/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Breadcrumbs as MUIBreadcrumbs,
    Link,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { Link as ReactDomLink } from "react-router-dom";
import { BreadcrumbsProps } from "./breadcrumbs.interfaces";

export const Breadcrumbs: FunctionComponent<BreadcrumbsProps> = ({
    crumbs,
}) => {
    return (
        <MUIBreadcrumbs>
            {crumbs.map((crumb, idx) => {
                if (crumb.link) {
                    return (
                        <Link
                            component={ReactDomLink}
                            key={idx}
                            to={crumb.link}
                        >
                            {crumb.label}
                        </Link>
                    );
                }

                return (
                    <Typography color="textPrimary" key={idx}>
                        {crumb.label}
                    </Typography>
                );
            })}
        </MUIBreadcrumbs>
    );
};
