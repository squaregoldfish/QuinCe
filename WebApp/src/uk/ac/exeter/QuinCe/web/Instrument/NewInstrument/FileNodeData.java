package uk.ac.exeter.QuinCe.web.Instrument.NewInstrument;

import uk.ac.exeter.QuinCe.data.Instrument.FileDefinition;

public class FileNodeData extends AssignmentsTreeNodeData {

  private final FileDefinition file;

  protected FileNodeData(FileDefinition file) {
    this.file = file;
  }

  protected String getFileDescription() {
    return file.getFileDescription();
  }

  @Override
  public String getLabel() {
    return file.getFileDescription();
  }

  @Override
  public String toString() {
    return file.getFileDescription();
  }
}
