// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export interface SearchInputV1Props {
    value?: string;
    placeholder?: string;
    fullWidth?: boolean;
    className?: string;
    onChangeDelay?: number;
    onChange?: (value: string) => void;
}
