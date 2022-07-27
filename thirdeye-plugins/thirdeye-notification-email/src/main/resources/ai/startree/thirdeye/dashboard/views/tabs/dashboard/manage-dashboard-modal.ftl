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

<!-- Modal -->
<div class="modal fade" id="create-dashboard-modal" role="dialog">
	<div class="modal-dialog">
		<!-- Modal content-->
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h4 class="modal-title">Create/Edit Dashboard</h4>
			</div>
			<div class="modal-body">
				<div class="row row-bordered">
					<div class="col-md-12 form-group form-inline">
						<label for="dashboard-name" class="label-medium-semibold">Dashboard:</label> <input type="text" name="dashboardName" id="dashboard-name" class="form-control" placeholder="Enter Dashboard Name" />
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<label for="metric-search-box" class="label-medium-semibold">Add Metrics</label> <input type="text" name="dashboardName" id="metric-search-box" class="form-control"
							placeholder="Search and add Metric to dashboard" />
					</div>
				</div>
				<div class="row">
					<div class="col-md-12 box-add-metric"></div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-inverse" data-dismiss="modal">Cancel</button>
				<button type="button" class="btn btn-primary" data-dismiss="modal">Save</button>
			</div>
		</div>
	</div>
</div>

