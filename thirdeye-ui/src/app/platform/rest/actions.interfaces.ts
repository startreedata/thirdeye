// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export enum ActionStatus {
    Initial,
    Working,
    Done,
    Error,
}

export interface ActionHook {
    status: ActionStatus;
    errorMessage: string;
}
