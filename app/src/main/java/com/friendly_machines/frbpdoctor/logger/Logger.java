package com.friendly_machines.frbpdoctor.logger;

import android.widget.TextView;

public class Logger {
    private static TextView logTextView;

    public static void setLogTextView(TextView textView) {
        logTextView = textView;
    }

    public static void log(String message) {
        if (logTextView != null) {
            logTextView.append(message + "\n");
        }
    }
}
