export const OFFSET_REGEX_EXTRACT = /[pP](-?\d+)([DWMY])/;

export type AnomaliesViewPageParams = {
    id: string;
};

export const OFFSET_TO_HUMAN_READABLE: { [key: string]: string } = {
    D: "Day",
    W: "Week",
    M: "Month",
    Y: "Year",
};
