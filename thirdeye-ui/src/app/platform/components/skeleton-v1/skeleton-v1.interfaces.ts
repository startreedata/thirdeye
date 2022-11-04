import { SkeletonProps } from "@material-ui/lab";

export interface SkeletonV1Props extends SkeletonProps {
    delayInMS?: number;
    preventDelay?: boolean;
}
