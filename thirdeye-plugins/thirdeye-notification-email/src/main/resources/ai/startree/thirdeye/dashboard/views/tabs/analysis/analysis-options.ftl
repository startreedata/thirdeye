<#--

    Copyright 2023 StarTree Inc

    Licensed under the StarTree Community License (the "License"); you may not use
    this file except in compliance with the License. You may obtain a copy of the
    License at http://www.startree.ai/legal/startree-community-license

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
    either express or implied.
    See the License for the specific language governing permissions and limitations under
    the License.

-->
<div class="analysis-card padding-all">
    <h4 class="analysis-title bottom-buffer">Trend Analysis
      <span class="analysis-details">
        (for <label class="label-medium-semibold">Metric</label> {{metricName}} from <label class="label-medium-semibold">Dataset</label> {{dataset}})
      </span>
    </h4>
    <div class="analysis-options">
      <div class="analysis-options__datepicker">
      <div class="datepicker-field">
        <h5 class="label-medium-semibold">Date Range (Current) </h5>
        <div id="current-range" class="datepicker-range">
          <span></span><b class="caret"></b>
        </div>
      </div>

      <div class="datepicker-field">
        <h5 class="label-medium-semibold">Compare To (Baseline)</h5>
        <div id="baseline-range" class="datepicker-range">
          <span></span> <b class="caret"></b>
        </div>
      </div>

      </div>
      <div class="analysis-options__dropdown">
        <div class="analysis-options__granularity">
          <label for="analysis-granularity-input">Granularity </label>
          <select id="analysis-granularity-input" style="width: 100%;" class="label-large-light"></select>
        </div>
        <div class="analysis-options__dimensions">
          <label for="analysis-metric-dimension-input">Dimensions</label>
          <select id="analysis-metric-dimension-input" style="width: 100%;" class="label-large-light filter-select-field"></select>
        </div>
        <div class="analysis-options__filters">
          <label for="analysis-metric-filter-input">Filters </label>
          <select id="analysis-metric-filter-input" style="width: 100%;" class="label-large-light"></select>
        </div>
      </div>

      <div class="analysis-options__apply">
        <a class="btn thirdeye-btn" id="analysis-apply-button" type="button"><span>Apply</span></a>
      </div>
    </div>
    <div class="top-buffer" id="timeseries-contributor-placeholder"></div>
  </div>
  <div class="spinner-wrapper">
    <div id="analysis-graph-spin-area"></div>
  </div>
