<#--

    Copyright 2022 StarTree Inc

    Licensed under the StarTree Community License (the "License"); you may not use
    this file except in compliance with the License. You may obtain a copy of the
    License at http://www.startree.ai/legal/startree-community-license

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
    either express or implied.
    See the License for the specific language governing permissions and limitations under
    the License.

-->
<div id="chart-area" class="uk-clearfix grid">
    <div id="display-chart-area"  class="" style="position: relative;">
        <div id="error">
        </div>
        <div id="chart-area-loader" class="loader hidden">
            <i class="uk-icon-spinner uk-icon-spin uk-icon-large"></i>
        </div>
        <section id="display-chart-section">
        </section>

        <section id="display-all-charts-section" class="uk-grid" style="display:none">

            <div class="uk-width-1-2 uk-row-first">
                <div id="metric-timeseries-line-loader" class="loader hidden">
                    <i class="uk-icon-spinner uk-icon-spin uk-icon-large"></i>
                </div>
                <div id="all-charts-metric-timeseries-line" class="uk-panel uk-panel-box">Metric timeseries line chart</div>
            </div>
            <div class="uk-width-1-2 uk-row-first">
                <div id="dimension-timeseries-line-loader" class="loader hidden">
                    <i class="uk-icon-spinner uk-icon-spin uk-icon-large" style="position: absolute; top: 1%; left: 50%;"></i>
                </div>
                <div id="all-charts-dimension-timeseries-line" class="uk-panel uk-panel-box">Dimension timeseries line chart</div>
            </div>
            <div class="uk-width-1-2">
                <div id="metric-timeseries-bar-loader" class="loader hidden">
                    <i class="uk-icon-spinner uk-icon-spin uk-icon-large"></i>
                </div>
                <div id="all-charts-metric-timeseries-bar" class="uk-panel uk-panel-box">Metric timeseries bar chart</div>
            </div>
            <div class="uk-width-1-2">
                <div id="dimension-timeseries-bar-loader" class="loader hidden">
                    <i class="uk-icon-spinner uk-icon-spin uk-icon-large" style="position: absolute; top: 1%; left: 50%;"></i>
                </div>
                <div id="all-charts-dimension-timeseries-bar" class="uk-panel uk-panel-box">Dimension timeseries bar chart</div>
            </div>

            <div class="uk-width-1-2">
                <div id="tabular-view-table-loader" class="loader hidden">
                    <i class="uk-icon-spinner uk-icon-spin uk-icon-large" style="position: absolute; top: 1%; left: 50%;"></i>
                </div>
                <div id="all-charts-tabular-view-table" class="uk-panel uk-panel-box">Tabular view table</div>
            </div>

            <div class="uk-width-1-2">
                <div id="contributors-loader" class="loader hidden">
                    <i class="uk-icon-spinner uk-icon-spin uk-icon-large" style="position: absolute; top: 1%; left: 50%;"></i>
                </div>
                <div id="all-charts-contributors" class="uk-panel uk-panel-box">Contributors view table</div>
            </div>

            <div class="uk-width-large-1-1">
                <div id="heatmap-loader" class="loader hidden">
                    <i class="uk-icon-spinner uk-icon-spin uk-icon-large" style="position: absolute; top: 1%; left: 50%;"></i>
                </div>
                <div id="all-charts-heatmap" class="uk-panel uk-panel-box">Treemaps</div>
            </div>
        </section>
    </div>
</div>