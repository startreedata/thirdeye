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
import {
    Box,
    FormControl,
    FormControlLabel,
    Grid,
    Radio,
    RadioGroup,
    Tooltip,
    Typography,
} from "@material-ui/core";
import { Icon } from "@iconify/react";
import React, { FunctionComponent } from "react";
import { RadioSectionProps } from "./radio-section.interfaces";
import { radioInputStyles } from "./radio-section.styles";

export const RadioSection: FunctionComponent<RadioSectionProps> = ({
    label,
    options,
    subText,
    defaultValue,
}) => {
    const classes = radioInputStyles();

    return (
        <Grid item xs={12}>
            <FormControl>
                <Box marginBottom="10px">
                    <Typography className={classes.header} variant="h6">
                        {label}
                    </Typography>
                    {subText && (
                        <Typography variant="body2">{subText}</Typography>
                    )}
                </Box>
                <RadioGroup
                    row
                    aria-labelledby="demo-row-radio-buttons-group-label"
                    defaultValue={defaultValue}
                    name="row-radio-buttons-group"
                >
                    {options.map((item) => (
                        <Box
                            alignItems="center"
                            border="1px #89bedf solid"
                            borderRadius="8px"
                            display="flex"
                            justifyContent="space-between"
                            key={item.label}
                            margin="5px"
                            marginLeft={0}
                            marginRight="20px"
                            minWidth="150px"
                            padding="2px 12px 2px 5px"
                        >
                            <FormControlLabel
                                control={<Radio />}
                                disabled={item.disabled}
                                label={item.label}
                                value={item.value}
                                onClick={item.onClick}
                            />
                            {!!item.tooltipText && (
                                <Tooltip
                                    arrow
                                    interactive
                                    placement="top"
                                    title={
                                        <Typography variant="caption">
                                            {item.tooltipText}
                                        </Typography>
                                    }
                                >
                                    <Icon
                                        height={20}
                                        icon="mdi:information-outline"
                                    />
                                </Tooltip>
                            )}
                        </Box>
                    ))}
                </RadioGroup>
            </FormControl>
        </Grid>
    );
};
