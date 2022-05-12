export interface Event {
    id: number;
    name: string;
    type?: string;
    startTime: number;
    endTime: number;
}

export interface EditableEvent {
    name: string;
    type?: string;
    startTime: number;
    endTime: number;
}
