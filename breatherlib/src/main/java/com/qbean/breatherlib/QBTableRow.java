package com.qbean.breatherlib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TableRow;

public class QBTableRow extends TableRow {

  private int rowIndex;

  public QBTableRow(Context context) {
    super(context);
  }

  public QBTableRow(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public void setRowIndex(int rowIndex) {
    this.rowIndex = rowIndex;
  }
}
