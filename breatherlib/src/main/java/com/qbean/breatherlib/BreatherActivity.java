package com.qbean.breatherlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class BreatherActivity extends Activity {
  private static Activity _mainActivity;
  private static Activity _fromActivity;
  private static boolean _reconfigure;
  private static boolean _isConfigured;
  private static String _breatherType;

  // Config
  private static SharedPreferences breatherConfig;
  private static SharedPreferences.Editor breatherConfigEditor;
  private static final String WAS_CONFIGURED = "WAS_CONFIGURED";
  private static final String BREATHER_TIME = "BREATHER_TIME";
  private static final float BREATHER_TIME_DEFAULT = 0.1f;
  private static final String BREATHER_TYPE = "BREATHER_TYPE";
  private static final String BREATHER_TYPE_DEFAULT = "Step Counter";
  private static final String TOTAL_STEPS = "TOTAL_STEPS";
  private static final int TOTAL_STEPS_DEFAULT = 10;
  private static final String TIMER_MINUTES = "TIMER_MINUTES";
  private static final int TIMER_MINUTES_DEFAULT = 10;
  private static String PASSCODE = "1111";

  private static Handler mainActivityTimeoutHandler = new Handler();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_breather);

    breatherConfig = getSharedPreferences("BreatherConfig", Context.MODE_PRIVATE);
    breatherConfigEditor = breatherConfig.edit();

    _isConfigured = wasConfigured();
    _breatherType = getBreatherType();

    if (_isConfigured && !_reconfigure) {
      switchToMainActivity(this);
    } else {
      checkToloadConfigForm("Please type in your passcode.");
    }

    findViewById(R.id.breather_layout).setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return false;
      }
    });
  }

  private void checkToloadConfigForm(String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    //builder.setMessage("Please type in your passcode.");
    builder.setMessage(message);

    final EditText passcodeInput = new EditText(this);
    passcodeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
    passcodeInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
    builder.setView(passcodeInput);

    builder.setPositiveButton("OK",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface arg0, int arg1) {
            if (passcodeInput.getText().toString().equals(PASSCODE)) {
              loadConfigForm();
            } else {
              //builder.setMessage("Wrong passcode. Try again.");
              checkToloadConfigForm("Wrong passcode. Try again.");
            }
          }
        });

    builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switchActivity(BreatherActivity.this, _fromActivity.getClass());
        dialog.dismiss();
      }
    });

    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }

  private void loadConfigForm() {
    EditText breatherTimeInput = (EditText) findViewById(R.id.breatherTimeInput);
    breatherTimeInput.setText(String.valueOf(getBreatherTime()));
    breatherTimeInput.addTextChangedListener(new AfterTextChangedListener(breatherTimeInput, "Breather Time") {
      @Override
      public void saveTextChanged(String inputString) {
        setBreatherTime(Float.parseFloat(inputString));
      }
    });

    RadioGroup breatherTypeChoice = (RadioGroup) findViewById(R.id.breatherTypeChoices);
    String breatherType = getBreatherType();

    for (int i = 0; i < breatherTypeChoice.getChildCount(); i++) {
      RadioButton rb = (RadioButton) breatherTypeChoice.getChildAt(i);
      if (rb.getText().equals(breatherType)) {
        breatherTypeChoice.check(rb.getId());
        break;
      }
    }

    breatherTypeChoice.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton checkButton = (RadioButton) findViewById(checkedId);
        _breatherType = checkButton.getText().toString();
        setBreatherType(_breatherType);
      }
    });

    EditText totalStepsInput = (EditText) findViewById(R.id.totalStepsInput);
    totalStepsInput.setText(String.valueOf(getTotalSteps()));
    totalStepsInput.addTextChangedListener(new AfterTextChangedListener(totalStepsInput, "Total Steps") {
      @Override
      public void saveTextChanged(String inputString) {
        setTotalSteps(Integer.parseInt(inputString));
      }
    });

    EditText timerMinutesInput = (EditText) findViewById(R.id.timerMinutesInput);
    timerMinutesInput.setText(String.valueOf(getTimerMinutes()));
    timerMinutesInput.addTextChangedListener(new AfterTextChangedListener(timerMinutesInput, "Timer Minutes") {
      @Override
      public void saveTextChanged(String inputString) {
        setTimerMinutes(Integer.parseInt(inputString));
      }
    });
  }

  // Save config
  public void onClickSave(View view) {
    if (getErrorMessage().length() > 0) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("Please fix the error")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.dismiss();
            }
          });
      AlertDialog alertDialog = builder.create();
      alertDialog.setTitle("Error Found");
      alertDialog.show();
    } else {
      _isConfigured = true;
      setConfigured(true);
      switchToMainActivity(this);
    }
  }

  public static void switchToMainActivity(Activity fromActivity) {
    Runnable mainActivityRunnable = new Runnable() {
      public void run() {
        switchActivity(_mainActivity, getBreatherClass(_breatherType));
      }
    };

    float breatherTime = getBreatherTime();
    long breatherTimeMillis = (long)(float)(breatherTime * 60000); // TEST
    mainActivityTimeoutHandler.postAtTime(mainActivityRunnable,
        System.currentTimeMillis() + breatherTimeMillis);
    mainActivityTimeoutHandler.postDelayed(mainActivityRunnable, breatherTimeMillis);

    switchActivity(fromActivity, _mainActivity.getClass());
  }

  public static boolean isConfigured() {
    return _isConfigured;
  }

  public static void setMainActivity(Activity mainActivity) {
    _mainActivity = mainActivity;
  }

  public static void configureBreather(Activity fromActivity, boolean reconfigure) {
    _fromActivity = fromActivity;
    mainActivityTimeoutHandler.removeCallbacksAndMessages(null);
    BreatherActivity._reconfigure = reconfigure;
    switchActivity(fromActivity, BreatherActivity.class);
  }

  private static void switchActivity(Activity fromActivity, Class toActivityClass) {
    Intent intent = new Intent(fromActivity, toActivityClass);
    fromActivity.startActivity(intent);
  }

  private boolean wasConfigured() {
    return breatherConfig.getBoolean(WAS_CONFIGURED, false);
  }

  private void setConfigured(boolean isConfigured) {
    breatherConfigEditor.putBoolean(WAS_CONFIGURED, isConfigured);
    breatherConfigEditor.apply();
  }

  public static float getBreatherTime() {
    return breatherConfig.getFloat(BREATHER_TIME, BREATHER_TIME_DEFAULT);
  }

  private void setBreatherTime(float breatherTime) {
    breatherConfigEditor.putFloat(BREATHER_TIME, breatherTime);
    breatherConfigEditor.apply();
  }

  private String getBreatherType() {
    return breatherConfig.getString(BREATHER_TYPE, BREATHER_TYPE_DEFAULT);
  }

  private void setBreatherType(String activityType) {
    breatherConfigEditor.putString(BREATHER_TYPE, activityType);
    breatherConfigEditor.apply();
  }

  private static Class getBreatherClass(String breatherType) {
    if (breatherType.equals("Step Counter"))
      return StepCounterActivity.class;
    if (breatherType.equals("Countdown"))
      return TimerActivity.class;
    if (breatherType.equals("Trivia"))
      return TestActivity.class;
    return null;
  }

  public static int getTotalSteps() {
    return breatherConfig.getInt(TOTAL_STEPS, TOTAL_STEPS_DEFAULT);
  }

  private void setTotalSteps(int totalSteps) {
    breatherConfigEditor.putInt(TOTAL_STEPS, totalSteps);
    breatherConfigEditor.apply();
  }

  public static int getTimerMinutes() {
    return breatherConfig.getInt(TIMER_MINUTES, TIMER_MINUTES_DEFAULT);
  }

  private void setTimerMinutes(int timerMinutes) {
    breatherConfigEditor.putInt(TIMER_MINUTES, timerMinutes);
    breatherConfigEditor.apply();
  }

  private String getErrorMessage() {
    TextView errorMessageTextView = (TextView) findViewById(R.id.errorMessage);
    return errorMessageTextView.getText().toString();
  }

  private void setErrorMessage(String errorMessage) {
    TextView errorMessageTextView = (TextView) findViewById(R.id.errorMessage);
    errorMessageTextView.setText(errorMessage);
  }

  private abstract class AfterTextChangedListener implements TextWatcher {
    private EditText _textInput;
    private String _inputName;

    public AfterTextChangedListener(EditText textInput, String inputName) {
      _textInput = textInput;
      _inputName = inputName;
    }

    public abstract void saveTextChanged(String inputString);

    @Override
    public void afterTextChanged(Editable s) {
      String inputString = _textInput.getText().toString();
      String errorMessage = "The " + _inputName + " can't be empty.";

      if (inputString.length() == 0) {
        setErrorMessage(errorMessage);
        return;
      }

      if(getErrorMessage().equals(errorMessage))
        setErrorMessage("");
      saveTextChanged(inputString);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start,
        int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start,
        int before, int count) {}
  }
}
