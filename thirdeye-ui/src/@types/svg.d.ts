type SvgComponent = React.FunctionComponent<React.SVGAttributes<SVGElement>>;

// Module declaration to allow importing SVG files
declare module "*.svg" {
    const ReactComponent: SvgComponent;

    export { ReactComponent };
}
