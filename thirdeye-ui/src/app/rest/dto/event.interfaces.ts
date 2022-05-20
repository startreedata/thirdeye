export interface Event extends EditableEvent {
    id: number;
}

export interface EditableEvent {
    name: string;
    type?: string;
    startTime: number;
    endTime: number;
}

export interface InvestigationEvent {
    id: number;
    name: string;
}
