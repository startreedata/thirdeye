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
							<th></th>
							{{#each timeRangeLabels as |label index|}}
							<th>{{label}}</th>
							{{/each}}
						</tr>
					</thead>
					<tbody>
					   {{#each metricToAnomalySummaryListMap as |anomalySummaryList metric|}}
						<tr class="bg-white">
							<td><a href="#"><span class="dashboard-metric-label">{{getMetricNameFromAlias metric}}</span></a></td>
							{{#each anomalySummaryList as |info index|}}
  							{{#if_eq info.numAnomaliesUnresolved '0'}}
  							<td style="background-color:rgba(124, 184, 47, 0.1)">
  							{{else}}
  							<td>
  							{{/if_eq}}
  							{{#if_eq info.numAnomalies '0'}}
  							   No Anomalies
  							{{else}}
  							<div class="td-box-left">
  									<span class="glyphicon glyphicon-ok" style="color: #7CB82F" aria-hidden="true"></span>
  									<span aria-hidden="true">{{info.numAnomaliesUnresolved}}</span>
  								</div>
  								<div class="td-box-right">
  									<span class="glyphicon glyphicon-remove" style="color: #DD2E1F" aria-hidden="true"></span>
  									<span aria-hidden="true">{{info.numAnomaliesResolved}}</span>
  								</div>
  							{{/if_eq}}
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


