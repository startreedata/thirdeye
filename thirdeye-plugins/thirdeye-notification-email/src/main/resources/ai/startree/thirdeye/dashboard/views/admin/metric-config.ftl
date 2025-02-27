<#--

    Copyright 2024 StarTree Inc

    Licensed under the StarTree Community License (the "License"); you may not use
    this file except in compliance with the License. You may obtain a copy of the
    License at http://www.startree.ai/legal/startree-community-license

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
    either express or implied.
    See the License for the specific language governing permissions and limitations under
    the License.

-->
<script id="metric-config-template" type="text/x-handlebars-template">
<div class="panel-body">
	<form role="form">
		<label for="metric-dataset-selector">Dataset </label> <Select id="metric-dataset-selector" name="dataset">
			<option value="Select Dataset">Select Dataset</option> {{#each datasets}}
			<option value="{{this}}">{{this}}</option> {{/each}}
		</Select>
	</form>
</div>
{{#each datasets}}
<div class="MetricConfigContainer" id="MetricConfigContainer-{{this}}"></div>
{{/each}}
</script>





