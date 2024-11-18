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
// external
import React from "react";
import { Grid } from "@material-ui/core";
import { useTranslation } from "react-i18next";
import { isEmpty } from "lodash";

// app components
import { RadioSection } from "../../../../../../components/form-basics/radio-section-v2/radio-section.component";

// types
import { SelectDimensionsOptions } from "../../../../../../rest/dto/metric.interfaces";
import { RadioSectionOptions } from "../../../../../../components/form-basics/radio-section-v2/radio-section.interfaces";

// state
import {
    MultipleDimensionEnumeratorOptions,
    useCreateAlertStore,
} from "../../../../hooks/state";

// sections
import { SqlQueryView } from "./sql-query-view";
import { DimensionRecommendorView } from "./dimension-recommendaor-view";

export const MultipleDimensionView = (): JSX.Element => {
    const { t } = useTranslation();
    const {
        multipleDimensionEnumeratorType,
        setMultipleDimensionEnumeratorType,
        selectedEnumerationItems,
        setEnumeratorQuery,
    } = useCreateAlertStore();

    const handleMultipleDimensionEnumeratorSelect = (
        enumeratorType: MultipleDimensionEnumeratorOptions
    ): void => {
        if (enumeratorType !== multipleDimensionEnumeratorType) {
            setMultipleDimensionEnumeratorType(enumeratorType);
            if (
                enumeratorType === SelectDimensionsOptions.DIMENSION_RECOMMENDER
            ) {
                setEnumeratorQuery("");
            }
        }
    };

    const getSelectDimensionsOptions = (
        values: Array<MultipleDimensionEnumeratorOptions>
    ): RadioSectionOptions[] => {
        const options: RadioSectionOptions[] = [];
        values.map((item) =>
            options.push({
                value: item,
                label: item,
                onClick: () => handleMultipleDimensionEnumeratorSelect(item),
                tooltipText: item,
            })
        );

        return options;
    };

    return (
        <>
            <Grid item xs={12}>
                <RadioSection
                    label={t("message.select-dimensions")}
                    options={getSelectDimensionsOptions([
                        SelectDimensionsOptions.ENUMERATORS,
                        SelectDimensionsOptions.DIMENSION_RECOMMENDER,
                    ])}
                    value={multipleDimensionEnumeratorType}
                />
            </Grid>
            {multipleDimensionEnumeratorType ===
                SelectDimensionsOptions.ENUMERATORS && <SqlQueryView />}
            {multipleDimensionEnumeratorType ===
                SelectDimensionsOptions.DIMENSION_RECOMMENDER &&
                isEmpty(selectedEnumerationItems) && (
                    <DimensionRecommendorView />
                )}
        </>
    );
};
