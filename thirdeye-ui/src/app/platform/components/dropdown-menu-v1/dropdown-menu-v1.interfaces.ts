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
export interface DropdownMenuV1Props {
    dropdownMenuItems: DropdownMenuItemV1[];
    anchorEl?: null | Element | ((element: Element) => Element);
    open: boolean;
    className?: string;
    onClose?: () => void;
    onClick?: (menuItemId: number | string, text: string) => void;
    dataTestId?: string;
}

export interface DropdownMenuItemV1 {
    id: number | string;
    text: string;
}
