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

import {
    Box,
    FormControl,
    FormControlLabel,
    FormLabel,
    Radio,
    RadioGroup,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent, useMemo, useState } from "react";
import { PageContentsCardV1 } from "../../platform/components";
import { getDatasourceGroups } from "./welcome-onboard-datasource-select-datasource.utils";

export const WelcomeSelectDatasource: FunctionComponent = () => {
    const [value, setValue] = useState("female");

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>): void => {
        setValue(event.target.value);
    };

    const datasourceGroups = useMemo(() => getDatasourceGroups(), []);

    return (
        <PageContentsCardV1>
            <Box px={2} py={2}>
                <Typography variant="h5">Select datasource</Typography>
                <Typography variant="body2">
                    You can always add, remove or chance datasources in the
                    configuration section.
                </Typography>
            </Box>
            {datasourceGroups.map((datasourceGroup) => (
                <Box key={datasourceGroup.key} px={2} py={1}>
                    <FormControl component="fieldset">
                        <FormLabel color="secondary" component="legend">
                            {datasourceGroup.title}
                        </FormLabel>
                        <RadioGroup
                            aria-label="Select Datasource"
                            name="select-datasource"
                            value={value}
                            onChange={handleChange}
                        >
                            {datasourceGroup.options.map((datasourceOption) => (
                                <FormControlLabel
                                    control={<Radio />}
                                    key={datasourceOption.value}
                                    label={datasourceOption.label}
                                    value={datasourceOption.value}
                                />
                            ))}
                        </RadioGroup>
                    </FormControl>
                </Box>
            ))}
        </PageContentsCardV1>
    );
};
