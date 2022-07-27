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
<div class="row bottom-line row-bordered">
	<div class="col-md-12">
		<div class="container top-buffer bottom-buffer">
			<div class="table-responsive">
				<table class="table dashboard-table" style="border-collapse: separate; border-spacing: 0em 1em">
					<thead>
						<tr>
							<th class="col-md-4"></th> {{#each timeRangeLabels as |label index|}}
							<th class="col-md-2">{{label}}</th> {{/each}}
						</tr>
					</thead>
					<tbody>
						{{#each wowSummary.metricAliasToMetricSummariesMap as |metricSummariesList metricAlias|}}
						<tr class="bg-white">
							<td class="col-md-4"><a href="#"><span class="dashboard-metric-label">{{getMetricNameFromAlias metricAlias}}</span></a></td>
							{{#each metricSummariesList as |metricSummary index|}}
							<td class="col-md-2" style="background-color:{{computeColor metricSummary.wowPercentageChange}};">
							  <span class="label wow-summary-content" style="color:{{computeTextColor metricSummary.wowPercentageChange}}">{{formatDouble metricSummary.currentValue}}</span>
							  <span class="label wow-summary-content" style="color:{{computeTextColor metricSummary.wowPercentageChange}}">({{formatPercent metricSummary.wowPercentageChange}})</span>
							</td> 
							{{/each}}
						</tr>
						{{/each}}
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>


