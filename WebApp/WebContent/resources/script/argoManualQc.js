function dataLoadedLocal() {

	initMap(1);
	
    itemNotLoading(PLOT1_LOADING);
	itemNotLoading(PLOT2_LOADING);
	itemNotLoading(MAP1_LOADING);
}

function getInitialLoadingItems() {
	return TABLE_LOADING | PLOT1_LOADING | PLOT2_LOADING | MAP1_LOADING;
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
