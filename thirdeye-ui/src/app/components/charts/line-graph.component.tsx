/* eslint-disable @typescript-eslint/no-explicit-any */
import * as d3 from "d3";
import React from "react";
import { GraphData, LineGraphProps } from "./line-graph.interfaces";
import "./line-graph.styles.scss";

const LineChart: React.FunctionComponent<LineGraphProps> = ({
    data,
}: LineGraphProps) => {
    const svgRef = React.useRef<SVGSVGElement>(null);

    const renderChart = React.useCallback(() => {
        // Get SVG Element Ref
        const svg = d3.select(svgRef.current);

        // Remove previosly created graph
        svg.selectAll("*").remove();

        // Get Tooltip element Ref
        const tooltip = d3.select(".tooltip");

        const margin = { top: 10, right: 10, bottom: 10, left: 20 };
        const gWidth =
            +(svgRef.current?.clientWidth || 0) - margin.left - margin.right;
        const gHeight =
            +(svgRef.current?.clientHeight || 0) - margin.top - margin.bottom;

        const g = svg
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // Set the scales
        const minDate: Date =
            d3.min(data, function (d: GraphData) {
                return d.timestamp as Date;
            }) || new Date();
        minDate?.setDate(minDate.getDate() - 1);

        const maxDate: Date =
            d3.max(data, function (d: GraphData) {
                return d.timestamp as Date;
            }) || new Date();

        const dateFormat = d3.timeFormat("%b %d");

        const initialYmin =
            d3.min(data, function (d: GraphData) {
                return d.current;
            }) || 0;
        const initialYMax =
            d3.max(data, function (d: GraphData) {
                return d.current;
            }) || 0;

        const xScale = d3
            .scaleTime()
            .domain([minDate, maxDate])
            .range([margin.left, gWidth - margin.right]);
        const linearYScale = d3
            .scaleLinear()
            .rangeRound([margin.top, gHeight - margin.bottom])
            .domain([initialYMax, initialYmin]);

        const xAxis = d3
            .axisBottom(xScale)
            .ticks(d3.timeWeek.every(1))
            .tickFormat(dateFormat as any);
        const yAxis = d3.axisLeft(linearYScale).tickFormat(d3.format("~s"));

        // create x Axis
        g.append("g")
            .attr("transform", "translate(0," + (gHeight - margin.bottom) + ")")
            .call(xAxis);
        // create y Axis
        g.append("g")
            .attr("transform", "translate( " + margin.left + "  ,0)")
            .call(yAxis);

        const currentLineGenerator = d3
            .line<GraphData>()
            .x((d: GraphData) => {
                return xScale(d.timestamp) as number;
            })
            .y((d: GraphData) => {
                return linearYScale(d["current"]) as number;
            });

        const expactedLineGenerator = d3
            .line<GraphData>()
            .x((d: GraphData) => {
                return xScale(d.timestamp) as number;
            })
            .y((d: GraphData) => {
                return linearYScale(d.expacted || 0) as number;
            });

        // 9. Append the path, bind the data, and call the line generator
        g.append("path")
            .datum(data) // 10. Binds data to the line
            .attr("class", "line") // Assign a class for styling
            .attr("fill", "none")
            .attr("stroke", "#1B1B1E")
            .attr("stroke-width", "2")
            .attr("d", currentLineGenerator);

        g.append("path")
            .datum(data) // 10. Binds data to the line
            .attr("class", "line") // Assign a class for styling
            .attr("fill", "none")
            .attr("stroke", "#FF9505")
            .attr("stroke-dasharray", "5, 5")
            .attr("stroke-width", "2")
            .attr("d", expactedLineGenerator);

        // Area graph
        g.append("path")
            .datum(data)
            .attr("fill", "#1CAFED")
            .attr(
                "d",
                d3
                    .area<GraphData>()
                    .defined((d: GraphData) => !isNaN(d.lowerBound))
                    .x((d: GraphData) => xScale(d.timestamp) as number)
                    .y0((d: GraphData) => linearYScale(d.lowerBound) as number)
                    .y1((d: GraphData) => linearYScale(d.current) as number)
            );

        // For tootlip and focus
        const focus = g
            .append("g")
            .attr("class", "focus")
            .style("display", "none");
        focus
            .append("circle")
            .attr("fill", "stealblue")
            .attr("r", 5)
            .attr("class", "circle");

        g.append("rect")
            .attr("class", "overlay")
            .attr("width", gWidth)
            .attr("height", gHeight)
            .style("opacity", 0)
            .on("mouseover", () => {
                focus.style("display", null);
            })
            .on("mouseout", () => {
                tooltip.transition().duration(300).style("opacity", 0);
            })
            .on("mousemove", mousemove);

        // Update tooltip on mousemove event
        function mousemove(event: any): void {
            const bisect = d3.bisector((d: GraphData) => d.timestamp).left;
            const xPos = (d3 as any).pointer(event)[0];
            const x0 = bisect(data, xScale.invert(xPos));
            const d0 = data[x0];
            focus.attr(
                "transform",
                `translate(${xScale(d0.timestamp)},${linearYScale(d0.current)})`
            );
            tooltip.transition().duration(300).style("opacity", 0.9);
            tooltip
                .html(
                    `<strong>${d3.timeFormat("%b %d, %H:%M %p")(
                        d0.timestamp
                    )}</strong> <br /> <strong>Current:</strong> ${
                        d0.current
                    } <br /> <strong>Baseline: </strong>${
                        d0.expacted
                    } <br /> <strong>Upperbound: </strong>${
                        d0.current
                    } <br /> <strong>Lowerbound: </strong>${parseInt(
                        d0.lowerBound + ""
                    )}`
                )
                .style("transform", `translate(${xScale(d0.timestamp)}px,0px)`);
        }
    }, [data]);

    const redrawChart = React.useCallback(() => {
        renderChart();
    }, [renderChart]);

    // Update chart on window resize
    React.useEffect(() => {
        window.addEventListener("resize", redrawChart);
        redrawChart();

        return (): void => window.removeEventListener("resize", redrawChart);
    }, [redrawChart]);

    return (
        <div id="container" style={{ position: "relative" }}>
            <svg height="200" ref={svgRef} width="100%" />
            <div className="tooltip" />
        </div>
    );
};

export default LineChart;
