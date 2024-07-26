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
import React from "react";
import { PageHeaderProps } from "./page-header.interfaces";
import { usePageHeaderStyles } from "./page-header.styles";
import { Button, Typography } from "@material-ui/core";

const PageHeader = ({
    mainHeading,
    subHeading = "",
    actionButtons = [],
}: PageHeaderProps): JSX.Element => {
    const pageHeaderClasses = usePageHeaderStyles();

    const renderActionButtons = (): JSX.Element => {
        if (Array.isArray(actionButtons)) {
            return (
                <div className={pageHeaderClasses.actionButtons}>
                    {actionButtons.map(({ label, onClick }): JSX.Element => {
                        return (
                            <Button
                                color="primary"
                                key={label}
                                size="small"
                                variant="outlined"
                                onClick={onClick}
                            >
                                {label}
                            </Button>
                        );
                    })}
                </div>
            );
        } else {
            return <div>{actionButtons}</div>;
        }
    };

    return (
        <div className={pageHeaderClasses.container}>
            <div className={pageHeaderClasses.headingWrapper}>
                <Typography variant="h4">{mainHeading}</Typography>
                <div>{subHeading}</div>
            </div>
            {renderActionButtons()}
        </div>
    );
};

export default PageHeader;
