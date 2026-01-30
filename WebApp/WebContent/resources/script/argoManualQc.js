const PROFILE_TABLE_LOADING = 1 << 11;
const PROFILE_INFO_LOADING = 1 << 12;

window['SELECTED_PROFILE_ROW'] = 0;

function dataLoadedLocal() {

  initMap(1);
  drawProfileListTable();
  
  // Highlight the first row as the current selection
  $($('#profileListTable').DataTable().row(window['SELECTED_PROFILE_ROW']).node()).addClass('selected');
  
  newProfileLoaded();
  
  itemNotLoading(PLOT1_LOADING);
  itemNotLoading(PLOT2_LOADING);
  itemNotLoading(MAP1_LOADING);
  
  // We drew the table ourselves
  return true;
}

function getInitialLoadingItems() {
	return TABLE_LOADING | PLOT1_LOADING | PLOT2_LOADING | MAP1_LOADING | PROFILE_TABLE_LOADING;
}

// Lay out the overall page structure
function layoutPage() {
  $('#plotPageContent').split({
    orientation: 'horizontal',
    onDragEnd: function(){
      scaleTableSplit()}
    });

  $('#plots').split({
    orientation: 'vertical',
    onDragEnd: function(){
    resizePlots()}
  });

  $('#profilePlots').split({
    orientation: 'vertical',
    onDragEnd: function(){
      resizePlots()}
  });

  $('#profileInfo').split({
    orientation: 'horizontal',
    onDragEnd: function(){
    resizeProfileInfo()}
  });
	  
  $('#profileData').split({
    orientation: 'horizontal',
    onDragEnd: function(){
    resizeProfileInfo()}
  });
}

function resizeAllContent() {
}

function resizePlots() {
	
}

function resizeProfileInfo() {
	
}

function getPlotFormName(index) {
	return '#plot' + index + 'Form';
}

function getMapFormName(index) {
	// There's only one map, so we ignore the index
	return '#profileMapForm';
}

// Handle table/plot split adjustment
function scaleTableSplit() {
  tableSplitProportion = $('#plotPageContent').split().position() / $('#plotPageContent').height();
  resizeAllContent();
}

function drawProfileListTable() {

  let tableColumns = [];
  
  let profileColumnList = JSON.parse($('#profileListForm\\:profileListColumns').val());
  
  profileColumnList.forEach(column => {
	tableColumns.push({'title': column});
  });
	
  new DataTable('#profileListTable', {
	ordering: false,
	searching: false,
	paging: false,
	bInfo: false,
	scrollY: 400,
    columns : tableColumns,
    data: JSON.parse($('#profileListForm\\:profileListData').val()),
	drawCallback: function (settings) {
	  setupProfileTableClickHandlers();
	}
    }
  )
  
  itemNotLoading(PROFILE_TABLE_LOADING);
}

// Initialise the click event handlers for the table
function setupProfileTableClickHandlers() {
  // Remove any existing handlers
  $('#profileListTable').off('click', 'tbody tr');

  // Set click handler
  $('#profileListTable').on('click', 'tbody tr', function() {
    selectProfileClick(this);
  })
}

function selectProfileClick(row) {
  $('#profileListForm\\:selectedProfile').val(row._DT_RowIndex);
  selectProfile();
}

function newProfileLoaded() {
	// Redraw the main QC table
	drawTable();
	
	// Update the profile table row highlight
	$($('#profileListTable').DataTable().row(window['SELECTED_PROFILE_ROW']).node()).removeClass('selected');
	window['SELECTED_PROFILE_ROW'] = $('#profileListForm\\:selectedProfile').val();;
	$($('#profileListTable').DataTable().row(window['SELECTED_PROFILE_ROW']).node()).addClass('selected');
	
	loadPlot1(); // PF RemoteCommand
}

function selectXAxis(index) {
  let xAxis = PF('plot' + index + 'XAxisPicker').input.val(); 
  window['plot' + index + 'XAxisVar'] = xAxis;
  if (xAxis != 0) {
    $(getPlotFormName(index) + '\\:plot' + index + 'XAxis').val(xAxis);
  }
  
  eval('loadPlot' + index + '()');
}

function mapsAllowed() {
  return false;
}

function getStrokeWidth() {
  return 0;
}

// Default y axis formatter does nothing
function formatYAxisLabel(value) {
  return value * -1;
}

// Default y axis value formatter does nothing
function formatYAxisValue(value) {
  return value * -1;
}

// Draw axes at zero
function getAxesAtZero() {
  return true;
}

// Always include zero
function getIncludeZero() {
  return true;
}

function legendFormatter(data) {
  return 'PRES: ' + formatYAxisValue(data.series[2].y) + '&nbsp;' + data.dygraph.user_attrs_.xlabel + ': ' + data.x;
}