type SvgComponent = React.FunctionComponent<React.SVGAttributes<SVGElement>>;

// Module declaration to allow importing SVGs
declare module "*.svg" {
    const ReactComponent: SvgComponent;

    export { ReactComponent };
}
