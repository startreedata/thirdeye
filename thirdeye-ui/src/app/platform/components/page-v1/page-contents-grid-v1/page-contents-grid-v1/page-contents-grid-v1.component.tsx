// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Grid } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { DimensionV1 } from "../../../../utils/material-ui/dimension.util";
import { PageContentsGridV1Props } from "./page-contents-grid-v1.interfaces";
import { usePageContentsGridV1Styles } from "./page-contents-grid-v1.styles";

export const PageContentsGridV1: FunctionComponent<PageContentsGridV1Props> = ({
    fullHeight,
    className,
    children,
    ...otherProps
}) => {
    const pageContentsGridV1Classes = usePageContentsGridV1Styles();

    return (
        <div
            {...otherProps}
            className={classNames(
                pageContentsGridV1Classes.pageContentsGrid,
                {
                    [pageContentsGridV1Classes.pageContentsGridFullHeight]:
                        fullHeight,
                },
                className,
                "page-contents-grid-v1"
            )}
        >
            <Grid
                container
                className={classNames(
                    {
                        [pageContentsGridV1Classes.pageContentsGridFullHeight]:
                            fullHeight,
                    },
                    "page-contents-grid-v1-grid"
                )}
                spacing={DimensionV1.PageGridSpacing}
            >
                {children}
            </Grid>
        </div>
    );
};
