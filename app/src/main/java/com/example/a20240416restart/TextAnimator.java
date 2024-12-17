package com.example.a20240416restart;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public class TextAnimator {
    private Handler handler;
    private int index;
    private long delay;
    private TextView textView;
    private String text;

    public TextAnimator(TextView textView, String text, long delay) {
        this.handler = new Handler(Looper.getMainLooper());
        this.index = 0;
        this.delay = delay;
        this.textView = textView;
        this.text = text;
    }

    public void animateText() {
        handler.postDelayed(characterAdder, delay);
    }

    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            textView.setText(textView.getText().toString() + text.charAt(index));
            index++;
            if (index < text.length()) {
                handler.postDelayed(characterAdder, delay);
            }
        }
    };
}

