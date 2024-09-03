package com.qbean.breatherlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class StepCounterActivity extends Activity implements SensorEventListener {

  private TextView totalStepsText;
  private SensorManager sensorManager;
  private Sensor stepSensor;
  private int startTotalSteps;
  private int totalSteps;

  @Override
  public final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stepcounter);

    totalSteps = BreatherActivity.getTotalSteps();
    totalStepsText = (TextView) findViewById(R.id.totalSteps);
    totalStepsText.setText(String.valueOf(totalSteps));

    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    startTotalSteps = 0;

    enableLongClickToConfigure(this);

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("Let's take a breather. Walk " + BreatherActivity.getTotalSteps() + " steps to get back on track!")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
          }
        });
    AlertDialog alertDialog = builder.create();
    alertDialog.setTitle("Your mind has been running for " + BreatherActivity.getBreatherTime() + " minutes.");
    alertDialog.show();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (stepSensor == null) {
      Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();
    } else {
      sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
      Log.i("My Log", stepSensor.toString());
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
  }

  @Override
  public final void onSensorChanged(SensorEvent event) {

    if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
      int sensorTotalSteps = (int) event.values[0];

      if (startTotalSteps == 0) {
        startTotalSteps = sensorTotalSteps;
      }

      int walkedSteps = sensorTotalSteps - startTotalSteps;
      int remainingSteps = totalSteps - walkedSteps;

      if (remainingSteps < 0)
        remainingSteps = 0;

      totalStepsText.setText(String.valueOf(remainingSteps));

      if (remainingSteps == 0)
        BreatherActivity.switchToMainActivity(this);
    }
  }

  @Override
  public final void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  private void enableLongClickToConfigure(Activity currentActivity) {
    View contentView = this.findViewById(android.R.id.content);
    contentView.setLongClickable(true);
    contentView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        BreatherActivity.configureBreather(currentActivity, true);
        return true;
      }
    });
  }
}
