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

import { TemplatePropertiesObject } from "../../rest/dto/alert.interfaces";
import { PropertyRenderConfig } from "../alert-wizard-v2/alert-template/alert-template-properties-builder/alert-template-properties-builder.interfaces";

export interface AdditonalFiltersDrawerProps {
    defaultValues: TemplatePropertiesObject;
    isOpen: boolean;
    onApply: (fieldData: TemplatePropertiesObject) => void;
    emptyMessage?: React.ReactNode;
    onClose: () => void;
    availableConfigurations: Record<
        string,
        Record<string, PropertyRenderConfig[]>
    >;
}
