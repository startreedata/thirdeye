// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
}: PasswordInputV1Props) => {
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
