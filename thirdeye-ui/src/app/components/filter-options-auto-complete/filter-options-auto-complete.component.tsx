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

    const [availableOptions, setAvailableOptions] = useState<FilterOption[]>(
        []
    );

    const [locallyStoredSelected, setLocallyStoredSelected] =
        useState<FilterOption | null>(selected);

    useEffect(() => {
        fetchOptions()
            .then((dataFromServer: FetchedDataType[]) => {
                setAvailableOptions(dataFromServer.map(formatOptionFromServer));
            })
            .catch(() => {
                notify(
                    NotificationTypeV1.Warning,
                    t("message.error-while-fetching", { entity: label })
                );
            });
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
                        className: "",
                    }}
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
