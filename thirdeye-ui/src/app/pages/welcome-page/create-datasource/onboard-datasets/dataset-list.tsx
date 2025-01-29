/*
 * Copyright 2025 StarTree Inc
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
import { FormGroup, Typography } from "@material-ui/core";
import React from "react";
import { useTranslation } from "react-i18next";
import { SelectDatasetOption } from "../../../../components/welcome-onboard-datasource/select-dataset-option/select-dataset-option.component";
import { isEmpty } from "lodash";
import { Dataset, DemoDataset } from "../../../../rest/dto/dataset.interfaces";

type DatasetListProps = {
    datasetGroup?: string;
    datasets: Dataset[] | DemoDataset[] | null;
    selectedDatasets: string[];
    onSelectDataset: (e: React.ChangeEvent<HTMLInputElement>) => void;
};

export const DatasetList = ({
    datasetGroup,
    datasets,
    selectedDatasets,
    onSelectDataset,
}: DatasetListProps): JSX.Element => {
    const { t } = useTranslation();

    return (
        <FormGroup>
            <Typography align="center" variant="body1">
                {datasetGroup}
            </Typography>

            {datasets?.map((dataset) => (
                <SelectDatasetOption
                    checked={selectedDatasets.includes(dataset.name)}
                    key={dataset.name}
                    labelPrimaryText={dataset.name}
                    labelSecondaryText={
                        dataset.dimensions?.length
                            ? t("label.num-dimensions", {
                                  num: dataset.dimensions?.length,
                              })
                            : ""
                    }
                    name={dataset.name}
                    onChange={onSelectDataset}
                />
            ))}
            {isEmpty(datasets) && (
                <Typography variant="body2">No datasets available.</Typography>
            )}
        </FormGroup>
    );
};
