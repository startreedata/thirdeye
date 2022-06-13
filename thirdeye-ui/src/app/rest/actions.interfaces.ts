export enum ActionStatus {
    Initial,
    Working,
    Done,
    Error,
}

export interface ActionHook {
    status: ActionStatus;
    errorMessages: string[];
}
