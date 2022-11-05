export interface Event extends EditableEvent {
    id: number;
}

export interface EditableEvent {
    name: string;
    type?: string;
    startTime: number;
    endTime: number;
    targetDimensionMap?: TargetDimensionMap;
}

export interface TargetDimensionMap {
    [key: string]: string[];
}
