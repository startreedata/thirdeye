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
    Button,
    FormControl,
    Grid,
    Select,
    TextField,
} from "@material-ui/core";
import ReplayIcon from "@material-ui/icons/Replay";
import React, { FunctionComponent, useMemo, useState } from "react";
import { OFFSET_REGEX_EXTRACT } from "../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import {
    OFFSET_TO_HUMAN_READABLE,
    PastDurationPickerProps,
} from "./past-duration-picker.interfaces";

export const PastDurationPicker: FunctionComponent<PastDurationPickerProps> = ({
    selected,
    onSelectedChange,
    children,
}) => {
    const [offsetUnit, setOffsetUnit] = useState<string>(() => {
        const result = OFFSET_REGEX_EXTRACT.exec(selected);

        if (result) {
            return result[2];
        }

        // Default to week
        return OFFSET_TO_HUMAN_READABLE.W.valueOf();
    });

    const [offsetValue, setOffsetValue] = useState<number>(() => {
        const result = OFFSET_REGEX_EXTRACT.exec(selected);

        if (result) {
            return Number(result[1]);
        }

        return 1;
    });

    const availableOptions = useMemo(() => {
        return Object.entries(OFFSET_TO_HUMAN_READABLE).map(([unit, label]) => {
            return {
                key: unit,
                label,
            };
        });
    }, []);

    const shouldDisableRefreshBtn = useMemo(() => {
        return `P${offsetValue}${offsetUnit}` === selected;
    }, [offsetValue, offsetUnit, selected]);

    const handleSetClick = (): void => {
        if (!offsetUnit || offsetValue === 0) {
            return;
        }

        onSelectedChange(`P${offsetValue}${offsetUnit}`);
    };

    return (
        <Grid
            container
            alignItems="center"
            justifyContent="flex-end"
            spacing={1}
        >
            {!!children && <Grid item>{children}</Grid>}
            <Grid item>
                <TextField
                    size="small"
                    type="number"
                    value={offsetValue}
                    onChange={(e) => setOffsetValue(Number(e.target.value))}
                />
            </Grid>
            <Grid item>
                <FormControl size="small" variant="outlined">
                    <Select
                        native
                        value={offsetUnit}
                        onChange={(e) =>
                            selected && setOffsetUnit(e.target.value as string)
                        }
                    >
                        {availableOptions.map((option) => {
                            return (
                                <option key={option.key} value={option.key}>
                                    {option.label}
                                </option>
                            );
                        })}
                    </Select>
                </FormControl>
            </Grid>
            <Grid item>
                <Button
                    color="primary"
                    disabled={shouldDisableRefreshBtn}
                    size="medium"
                    variant="contained"
                    onClick={handleSetClick}
                >
                    <ReplayIcon />
                </Button>
            </Grid>
        </Grid>
    );
};
