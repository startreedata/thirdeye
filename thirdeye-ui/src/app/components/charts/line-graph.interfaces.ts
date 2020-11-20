export interface LineGraphProps {
    data: GraphData[];
}

export interface GraphData {
    timestamp: Date;
    current: number;
    expacted: number;
    upperBound: number;
    lowerBound: number;
}
