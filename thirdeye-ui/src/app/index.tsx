import React, { StrictMode } from "react";
import ReactDOM from "react-dom";
import { BrowserRouter as Router, Route } from "react-router-dom";
import { App } from "./app";
import "./index.scss";

// Aplication entry point
ReactDOM.render(
    <StrictMode>
        <Router>
            {/* App needs to be rendered by a router to allow navigation using AppBar */}
            <Route component={App} />
        </Router>
    </StrictMode>,
    document.getElementById("root") as HTMLElement
);
