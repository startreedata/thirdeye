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
import TextField from "@material-ui/core/TextField";
import { Autocomplete } from "@material-ui/lab";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import {
    FilterOption,
    FilterOptionsAutoCompleteProps,
} from "./filter-options-auto-complete.interfaces";
import { useFilterOptionsAutoCompleteStyles } from "./filter-options-auto-complete.style";

function FilterOptionsAutoComplete<FetchedDataType>({
    label,
    fetchOptions,
    formatOptionFromServer,
    onSelectionChange,
    selected,
    formatSelectedAfterOptionsFetch = (selected) => selected,
}: FilterOptionsAutoCompleteProps<FetchedDataType>): JSX.Element {
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const classes = useFilterOptionsAutoCompleteStyles();

    const [availableOptions, setAvailableOptions] = useState<FilterOption[]>(
        []
    );

    const [locallyStoredSelected, setLocallyStoredSelected] =
        useState<FilterOption | null>(selected);

    useEffect(() => {
        // isMounted ensures that the state updates are not run if the component has been unmounted
        let isMounted = true;

        fetchOptions()
            .then((dataFromServer: FetchedDataType[]) => {
                if (isMounted) {
                    setAvailableOptions(
                        dataFromServer.map(formatOptionFromServer)
                    );
                }
            })
            .catch(() => {
                if (isMounted) {
                    notify(
                        NotificationTypeV1.Warning,
                        t("message.error-while-fetching", { entity: label })
                    );
                }
            });

        return () => {
            isMounted = false;
        };
    }, []);

    useEffect(() => {
        if (selected && availableOptions.length > 0) {
            setLocallyStoredSelected(
                formatSelectedAfterOptionsFetch(selected, availableOptions)
            );
        } else {
            setLocallyStoredSelected(selected);
        }
    }, [selected, availableOptions]);

    return (
        <Autocomplete<FilterOption>
            autoSelect
            fullWidth
            getOptionLabel={(option) => option.label}
            noOptionsText={t("message.no-filter-options-available-entity", {
                entity: label,
            })}
            options={availableOptions}
            renderInput={(params) => (
                <TextField
                    {...params}
                    InputProps={{
                        ...params.InputProps,
                        // Override class name so the size of input is smaller
                        className: classes.input,
                    }}
                    fullWidth={false}
                    placeholder={t("message.filter-by-entity", {
                        entity: label,
                    })}
                    variant="outlined"
                />
            )}
            value={locallyStoredSelected}
            onChange={(_, selectedValue) => {
                onSelectionChange(selectedValue);
            }}
        />
    );
}

export { FilterOptionsAutoComplete };
