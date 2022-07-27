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
<!-- Chart section -->
<div class="analysis-card analysis-chart bottom-buffer">
    <div class="analysis-chart__legend padding-all" id="analysis-chart-legend"></div>
    <div class="analysis-chart__graph" id="analysis-chart" style=""></div>
</div>

<!-- Percentage change table -->
<h4 class="analysis-title">Change Over Time</h4>
<div class="analysis-change bottom-buffer">
  <span class="analysis-change__description label-small-semibold">Click on a cell to drill down into its contribution breakdown.</span>
  <input class="analysis-change__checkbox" type="checkbox" id="show-details" {{#if this.showDetailsChecked}}checked{{/if}}>
  <label for="show-details" class="metric-label analysis-change__label">See Contribution Details</label>
  <input class="analysis-change__checkbox" type="checkbox" id="show-cumulative" {{#if this.showCumulativeChecked}}checked{{/if}}>
  <label for="show-cumulative" class="metric-label analysis-change__label">Show Cumulative</label>
</div>

<div class="row">
  <div id="contributor-table-placeholder"></div>
</div>
PinotDatasetOnboarderTest
