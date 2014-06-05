package com.mobinsight.demoapp;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobinsight.demoapp.R;

import com.mobinsight.client.Answer;
import com.mobinsight.client.AnswerRange;
import com.mobinsight.client.Mobinsight;
import com.mobinsight.client.Question;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();

    private Mobinsight mobinsight;

    private TextView mSurveyNameText;
    private TextView mQuestionText;
    private ImageView mQuestionImage;
    private Button mAnswerButton;
    private View mProgressView;
    private EditText mAnswerText;
    private Spinner mAnswerChoice;
    private SeekBar mAnswerRange;
    private Question mQuestion;

    private Mobinsight.AuthenticateUserListener mAuthenticateUserListener =
            new Mobinsight.AuthenticateUserListener() {
        @Override
        public void onSuccess() {
            onAuthenticated();
        }
        @Override
        public void onError(int error) {
            showError("Authentication error " + error);
        }
    };

    private Mobinsight.GetNextQuestionListener mGetNextQuestionListener =
            new Mobinsight.GetNextQuestionListener() {
        @Override
        public void onSuccess(Question question) {
            onNextQuestion(question);
        }
        @Override
        public void onError(int error) {
            showError("getNextQuestion error " + error);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurveyNameText = (TextView) findViewById(R.id.survey_name);
        mQuestionText = (TextView) findViewById(R.id.question);
        mQuestionImage = (ImageView) findViewById(R.id.image);
        mAnswerButton = (Button) findViewById(R.id.answer);
        mProgressView = findViewById(R.id.progress);
        mAnswerText = (EditText) findViewById(R.id.answerText);
        mAnswerChoice = (Spinner) findViewById(R.id.answerChoice);
        mAnswerRange = (SeekBar) findViewById(R.id.answerRange);
        mAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnswerButton.setEnabled(false);
                mProgressView.setVisibility(View.VISIBLE);
                Answer answer = null;
                if (mQuestion.getAnswerType() == Question.ANSWER_TYPE_TEXT) {
                    answer = new Answer(mAnswerText.getText().toString());
                } else if (mQuestion.getAnswerType() == Question.ANSWER_TYPE_CHOICE) {
                    answer = new Answer(mAnswerChoice.getSelectedItemPosition());
                } else if (mQuestion.getAnswerType() == Question.ANSWER_TYPE_RANGE) {
                    answer = new Answer((float) mAnswerRange.getProgress());
                }
                mobinsight.answerLastQuestion(answer);
                mobinsight.getNextQuestion(mGetNextQuestionListener);
            }
        });

        mobinsight = Mobinsight.getInstance();
        mobinsight.authenticateUser(null, mAuthenticateUserListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    private void onAuthenticated() {
        mobinsight.getNextQuestion(mGetNextQuestionListener);
    }

    private void onNextQuestion(Question question) {
        mAnswerButton.setEnabled(true);
        mProgressView.setVisibility(View.GONE);
        mAnswerText.setVisibility(View.GONE);
        mAnswerChoice.setVisibility(View.GONE);
        mAnswerRange.setVisibility(View.GONE);
        if (question != null) {
            mQuestion = question;
            mSurveyNameText.setText(question.getSurveyName() +
                    " (" + (question.getIndexInSurvey() + 1) + "/" +
                    question.getSurveyLength() + ")");
            mQuestionText.setText(question.getText());
            if (question.getLocalImage() != null) {
                mQuestionImage.setVisibility(View.VISIBLE);
                mQuestionImage.setImageBitmap(BitmapFactory.decodeFile(question.getLocalImage()));
            } else {
                mQuestionImage.setVisibility(View.INVISIBLE);
            }
            if (question.getAnswerType() == Question.ANSWER_TYPE_TEXT) {
                mAnswerText.setVisibility(View.VISIBLE);
                mAnswerText.setText("");
            } else if (question.getAnswerType() == Question.ANSWER_TYPE_CHOICE) {
                mAnswerChoice.setVisibility(View.VISIBLE);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                        question.getAnswerChoices());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mAnswerChoice.setAdapter(adapter);
            } else if (question.getAnswerType() == Question.ANSWER_TYPE_RANGE) {
                // This is just a sample. In real apps you'd want to use a widget that supports real
                // float range for selection, but SeekBar only has integer support and doesn't allow
                // any steps other than +1.
                AnswerRange range = question.getAnswerRange();
                mAnswerRange.setMax((int) (range.mHigh - range.mLow));
                mAnswerRange.setVisibility(View.VISIBLE);
                mAnswerRange.setProgress(0);
            }
        } else {
            mQuestionText.setText("No more questions.");
            mQuestionImage.setVisibility(View.INVISIBLE);
        }
    }

    private void showError(String message) {
        Log.e(TAG, message);
    }

}
