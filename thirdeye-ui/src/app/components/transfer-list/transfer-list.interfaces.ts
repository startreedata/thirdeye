// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
export interface TransferListProps<T> {
    fromList: T[];
    toList?: T[];
    fromLabel?: string;
    toLabel?: string;
    link?: boolean;
    loading?: boolean;
    listItemTextFn?: (listItem: T) => string; // Function that returns text to be displayed for list item
    listItemKeyFn?: (listItem: T) => string | number; // Function that returns unique identifier for list item
    onClick?: (listItem: T) => void;
    onChange?: (toList: T[]) => void;
}
