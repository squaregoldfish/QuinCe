function acceptAutoQc() {
  $('#selectionForm\\:selectedColumn').val(getColumn(selectedColumn).id);
  $('#selectionForm\\:selectedRows').val(selectedRows);
  submitAutoQC();
}

function qcFlagsAccepted() {
  errorCheck();
  
  clearSelection();
  // redrawPlot(1);
  // redrawPlot(2);

  // Reload table data
  jsDataTable.ajax.reload(null, false);
}