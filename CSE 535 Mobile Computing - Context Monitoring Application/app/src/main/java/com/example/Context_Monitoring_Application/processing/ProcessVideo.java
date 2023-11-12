package com.example.Context_Monitoring_Application.processing;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.Context_Monitoring_Application.R;
import com.example.Context_Monitoring_Application.calculations.CalculateHeartRate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProcessVideo {
    public static Uri URI;
    private static boolean isHeartRateInProcess =false;
    public static Double uploadRecordingAndCalculateHearRate(Context context){
        InputStream inputStream = context.getResources().openRawResource(R.raw.input);
        String fileName = "heart_rate.mp4";
        File location = new File(context.getFilesDir(), fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(location);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        URI = Uri.fromFile(location);

        if (isHeartRateInProcess) {
            Toast.makeText(context, "Heart Rate is being calculated. Please wait...",
                    Toast.LENGTH_SHORT).show();
            return (double) -1;
        } else if (URI != null) {
            isHeartRateInProcess = true;
            return CalculateHeartRate.calculateHeartRate(extractFramesFromRecording(context, URI));
        } else {
            Toast.makeText(context, "Video has not recorded yet.", Toast.LENGTH_SHORT).show();
            return (double) -1;
        }
    }

    private static List<Bitmap> extractFramesFromRecording(Context context, Uri videoUri) {
        List<Bitmap> frames = new ArrayList<>();

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, videoUri);

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);

            int recordingDuration = duration != null ? Integer.parseInt(duration) : 0;
            int i = 10;
            while (i < recordingDuration) {
                Bitmap bitmap = retriever.getFrameAtIndex(i);
                if (bitmap != null) {
                    Log.i("Frames", "Extracting Frames");
                    frames.add(bitmap);
                }
                i += 5;
            }
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("Total frames", "frames: "+frames.size());
        return frames;
    }

}

