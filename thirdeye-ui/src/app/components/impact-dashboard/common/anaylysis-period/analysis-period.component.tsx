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
import React, { ReactElement } from "react";
import { useTranslation } from "react-i18next";

// Interfaces
import { AnalysisPeriodProps } from "./analysis-period.interaces";

// Styles
import { useAnalysisPeriodStyles } from "./analysis-period.styles";

export const AnalysisPeriod = ({
    selectedPeriod,
    analysisPeriods,
    onClick,
}: AnalysisPeriodProps): ReactElement => {
    const componentStyles = useAnalysisPeriodStyles();
    const { t } = useTranslation();

    return (
        <div className={componentStyles.container}>
            <div>{`${t("pages.impact-dashboard.common.date-range")}`}:</div>
            <div className={componentStyles.rangeContainer}>
                {analysisPeriods.map((analysisPeriod) => {
                    return (
                        <div
                            className={
                                analysisPeriod === selectedPeriod
                                    ? componentStyles.selected
                                    : componentStyles.range
                            }
                            key={analysisPeriod}
                            onClick={() => onClick(analysisPeriod)}
                        >
                            {analysisPeriod}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};
