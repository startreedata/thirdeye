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
import { IconButton, InputAdornment, TextField } from "@material-ui/core";
import VisibilityIcon from "@material-ui/icons/Visibility";
import VisibilityOffIcon from "@material-ui/icons/VisibilityOff";
import classNames from "classnames";
import React, { FunctionComponent, useState } from "react";
import { PasswordInputV1Props } from "./password-input-v1.interfaces";

export const PasswordInputV1: FunctionComponent<PasswordInputV1Props> = ({
    allowShowPassword,
    className,
    ...otherProps
}) => {
    const [type, setType] = useState("password");

    const handleShowPasswordClick = (): void => {
        setType("text");
    };

    const handleHidePasswordClick = (): void => {
        setType("password");
    };

    return (
        <TextField
            {...otherProps}
            InputProps={{
                endAdornment: (
                    <InputAdornment position="end">
                        {allowShowPassword && type === "password" && (
                            // Show password button
                            <IconButton
                                className="password-input-v1-show-password"
                                size="small"
                                onClick={handleShowPasswordClick}
                            >
                                <VisibilityOffIcon
                                    color="action"
                                    fontSize="small"
                                />
                            </IconButton>
                        )}

                        {allowShowPassword && type === "text" && (
                            // Hide password button
                            <IconButton
                                className="password-input-v1-show-password"
                                size="small"
                                onClick={handleHidePasswordClick}
                            >
                                <VisibilityIcon
                                    color="action"
                                    fontSize="small"
                                />
                            </IconButton>
                        )}
                    </InputAdornment>
                ),
            }}
            className={classNames(className, "password-input-v1")}
            type={type}
        />
    );
};
