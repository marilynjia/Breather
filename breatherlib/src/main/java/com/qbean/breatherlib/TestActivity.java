package com.qbean.breatherlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Random;

public class TestActivity extends Activity {

  private final int QUESTION_TOTAL = 6;
  private final int OPERAND_MAX = 20;
  private final int OPERAND_MIN = 1;

  private int[] firstOperandList = new int[QUESTION_TOTAL];
  private int[] secondOperandList = new int[QUESTION_TOTAL];
  private String[] operatorList = new String[QUESTION_TOTAL];
  private int[] resultList = new int[QUESTION_TOTAL];
  private String[] questionList = new String[QUESTION_TOTAL];
  private boolean[] markList = new boolean[QUESTION_TOTAL];

  private TableLayout testList;
  private TextView remainingQuestionCountTextView;
  private int remainingQuestionCount;
  private ColorStateList oldColors;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);

    remainingQuestionCountTextView = (TextView) findViewById(R.id.remainingQuestionCount);
    remainingQuestionCountTextView.setText(String.valueOf(QUESTION_TOTAL));
    remainingQuestionCount = QUESTION_TOTAL;
    testList = (TableLayout) findViewById(R.id.testList);

    generateTestData();

    addTableRows();

    testList.setLongClickable(true);
    testList.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        BreatherActivity.configureBreather(TestActivity.this, true);
        return true;
      }
    });

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("Let's take a breather. Answer " + QUESTION_TOTAL + " quick math questions to re-focus your mind.")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
          }
        });
    AlertDialog alertDialog = builder.create();
    alertDialog.setTitle("Your mind has been running for " + BreatherActivity.getBreatherTime() + " minutes.");
    alertDialog.show();
  }

  private void generateTestData() {
    Random r = new Random(System.currentTimeMillis());

    int operand1 = 0;
    int operand2 = 0;
    String operator = "";

    for (int i = 0; i < QUESTION_TOTAL; i++) {
      operand1 = r.nextInt(OPERAND_MAX) + OPERAND_MIN;
      operand2 = r.nextInt(OPERAND_MAX) + OPERAND_MIN;
      operator = r.nextInt(2) == 0 ? "+" : "-";

      firstOperandList[i] = operand1;
      secondOperandList[i] = operand2;
      operatorList[i] = operator;
      questionList[i] =
          String.valueOf(operand1) + " " + operator + " " + String.valueOf(operand2) + " = ";
      resultList[i] = operator == "+" ? operand1 + operand2 : operand1 - operand2;
    }
  }

  private void addTableRows() {
    for (int i = 0; i < QUESTION_TOTAL; i++) {
      QBTableRow row = new QBTableRow(this);
      TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
      row.setPadding(80, 10, 20, 10);
      row.setRowIndex(i);
      row.setLayoutParams(lp);

      TextView questionText = new TextView(this);
      questionText.setText(questionList[i]);
      questionText.setTextAppearance(android.R.style.TextAppearance_Medium);
      row.addView(questionText);

      EditText resultInput = new EditText(this);
      resultInput.setTextAppearance(android.R.style.TextAppearance_Medium);
      resultInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
      resultInput.setGravity(Gravity.CENTER_HORIZONTAL);
      resultInput.setWidth((int) (300 + 0.5f));
      oldColors = resultInput.getTextColors();
      resultInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
          EditText editText = (EditText) v;
          if (!hasFocus) {

            QBTableRow row = (QBTableRow) v.getParent();
            int rowIndex = row.getRowIndex();
            String answer = editText.getText().toString();
            boolean isCountChanged = false;

            if (answer.length() < 1) { // The answer is an empty string
              if (markList[rowIndex]) {
                markList[rowIndex] = false;
                remainingQuestionCount++;
                isCountChanged = true;
              }
            } else {
              int answerNumber = Integer.parseInt(answer);
              if (answerNumber == resultList[rowIndex] && !markList[rowIndex]) {
                markList[rowIndex] = true;
                remainingQuestionCount--;
                isCountChanged = true;
              }
              if (answerNumber != resultList[rowIndex] && markList[rowIndex]) {
                markList[rowIndex] = false;
                remainingQuestionCount++;
                isCountChanged = true;
              }

              if (answerNumber != resultList[rowIndex]) {
                editText.setTextColor(Color.RED);
              } else {
                editText.setTextColor(oldColors);
              }
            }

            if (isCountChanged) {
              remainingQuestionCountTextView.setText(String.valueOf(remainingQuestionCount));
              if (remainingQuestionCount == 0) {
                BreatherActivity.switchToMainActivity(TestActivity.this);
              }
            }
          }
        }
      });

      row.addView(resultInput);
      testList.addView(row);
    }
  }
}
