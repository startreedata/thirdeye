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
<script id="job-info-template" type="text/x-handlebars-template">
<div class="panel-body">
	<form role="form">
		<label for="job-dataset-selector">Dataset </label> <Select id="job-dataset-selector" name="dataset">
			<option value="---" selected>---</option> 
			<option value="MOST-RECENT">MOST-RECENT</option> 
            {{#each datasets}}
			  <option value="{{this}}">{{this}}</option> 
            {{/each}}
		</Select>
	</form>
</div>
<div class="JobInfoContainer" id="JobInfoContainer-MOST-RECENT"></div>
{{#each datasets}}
<div class="JobInfoContainer" id="JobInfoContainer-{{this}}"></div>
{{/each}}
</script>






