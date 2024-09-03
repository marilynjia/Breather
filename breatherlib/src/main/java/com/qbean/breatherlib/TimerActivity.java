package com.qbean.breatherlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class TimerActivity extends Activity {

  private WebView timerWebView;
  private Button prevButton;
  private Button nextButton;

  private int currentUrlIndex;
  private String[] timerUrlArray = {
      "https://www.youtube.com/embed/UP1WpOa82R4?si=Lh_0_CmujPB1Pv2O",
      "https://www.youtube.com/embed/PU9fJokGNWU?si=FIiWU2Og7LHGMysL",
      "https://www.youtube.com/embed/5Ezn49xj0zk?si=ZrQLC6c5y3mz7jHN",
      "https://www.acalanes.k12.ca.us/campolindo/"
  };

  private CountDownTimer countDownTimer;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_timer);

    timerWebView = (WebView) findViewById(R.id.timerWebView);
    timerWebView.getSettings().setJavaScriptEnabled(true);
    timerWebView.setWebViewClient(new TimerWebViewClient());
    timerWebView.loadUrl(timerUrlArray[currentUrlIndex]);

    prevButton = (Button) findViewById(R.id.prevButton);
    nextButton = (Button) findViewById(R.id.nextButton);
    prevButton.setEnabled(false);

    startTimerCountDown();
    enableLongClickToConfigure(this);

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("For " + BreatherActivity.getTimerMinutes() + " minutes, rest your eyes with a calming video. Think about how you feel today.")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
          }
        });
    AlertDialog alertDialog = builder.create();
    alertDialog.setTitle("You've played " + BreatherActivity.getBreatherTime() + " minutes");
    alertDialog.show();
  }

  public void onClickPrev(View view) {
    currentUrlIndex--;
    timerWebView.loadUrl(timerUrlArray[currentUrlIndex]);
    updateButtonState();
  }

  public void onClickNext(View view) {
    currentUrlIndex++;
    timerWebView.loadUrl(timerUrlArray[currentUrlIndex]);
    updateButtonState();
  }

  private void updateButtonState() {
    if(currentUrlIndex > 0) {
      prevButton.setEnabled(true);
    } else {
      prevButton.setEnabled(false);
    }

    if (currentUrlIndex < timerUrlArray.length - 1) {
      nextButton.setEnabled(true);
    } else {
      nextButton.setEnabled(false);
    }
  }

  private void startTimerCountDown() {
    TextView timerCountDown = (TextView) findViewById(R.id.timerCountDown);

    countDownTimer = new CountDownTimer(BreatherActivity.getTimerMinutes() * 60000, 1000) {
      public void onTick(long millisUntilFinished) {
        long secondsUntilFinished = millisUntilFinished / 1000;
        timerCountDown.setText(secondsUntilFinished / 60 + ":" + secondsUntilFinished % 60);
      }

      public void onFinish() {
        BreatherActivity.switchToMainActivity(TimerActivity.this);
        this.cancel();
      }
    }.start();
  }

  private void enableLongClickToConfigure(Activity currentActivity) {
    View contentView = this.findViewById(android.R.id.content);
    contentView.setLongClickable(true);
    contentView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        countDownTimer.cancel();
        BreatherActivity.configureBreather(currentActivity, true);
        return true;
      }
    });
  }

  class TimerWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return true;
    }
  }
}
