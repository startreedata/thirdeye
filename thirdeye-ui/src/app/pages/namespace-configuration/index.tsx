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
import React, { useState } from "react";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import {
    JSONEditorV1,
    PageContentsGridV1,
    PageV1,
} from "../../platform/components";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";

export const NamespaceConfiguration = (): JSX.Element => {
    const isLoading = false;
    const isError = false;
    const [namespaceConfig, setNamespaceConfig] = useState<any>({
        timezone: "",
        format: "",
    });
    const handleNamespaceConfigChange = (value: string): void => {
        setNamespaceConfig(JSON.parse(value));
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={6} />
            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch
                    wrapInCard
                    wrapInGrid
                    isError={isError}
                    isLoading={isLoading}
                >
                    <JSONEditorV1<any>
                        hideValidationSuccessIcon
                        value={namespaceConfig}
                        onChange={handleNamespaceConfigChange}
                    />
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
