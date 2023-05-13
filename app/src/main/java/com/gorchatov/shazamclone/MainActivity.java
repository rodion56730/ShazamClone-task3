package com.gorchatov.shazamclone;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.ArrayMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public Button shazamBtn;
    public CountDownTimer timer;
    private MusicDao musicDao;
    private TextView nameSongTV;


    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

//========================================================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int BufferElements2Rec = 4096; // want to play 2048 (2K) since 2 bytes we use only 1024
        int BytesPerElement = 2; // 2 bytes in 16bit format
        if (ActivityCompat.checkSelfPermission(this, RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RequestPermissions();
            }
        }

        setContentView(R.layout.activity_main);
        shazamBtn = findViewById(R.id.shazam_btn);
        nameSongTV = findViewById(R.id.name_songTV);
        timer = new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
                System.out.println("Осталось: "
                        + millisUntilFinished / 1000);
            }

            public void onFinish() {
                isRecording = false;
                stopRecording();//запись в файл
            }
        };
        shazamBtn.setOnClickListener(v -> {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);
            isRecording = true;
            recorder.startRecording();
            recordingThread = new Thread(() -> {
                try {
                    writeAudioDataToFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, "AudioRecorder Thread");

            recordingThread.start();
            timer.cancel();
            timer.start();
        });
        musicDao = App.getInstance().getDatabase().musicDao();
        System.out.println(musicDao.getAll().size()+"==================");
        //============================
        //первая запись в БД
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if(CheckPermissions()) {
//                Toast.makeText(this,"Here",Toast.LENGTH_LONG).show();
//                InputStream in;
//                try {
//                    in = Files.newInputStream(new File(Environment.getExternalStorageDirectory() + "/Download/example2.wav").toPath());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                try {
//                    toByteArray(in);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                System.out.println(musicDao.getAll().size());
//                System.out.println(temp.length);
//            }
//        }
        //=================================
    }


    int BufferElements2Rec = 4096;
    final int TOTAL_SIZE = 1764000;
    byte[][] allBytes = new byte[TOTAL_SIZE / BufferElements2Rec][];

    private void writeAudioDataToFile() throws IOException {
        // Write the output audio in byte
        byte[] bData = new byte[BufferElements2Rec];
        int t = 0;
        while (isRecording && t < 430) {
            // gets the voice output from microphone to byte format
            recorder.read(bData, 0, 4096);
            allBytes[t] = bData.clone();
            t++;
        }
        RecognizeMusic(allBytes);
    }


    private void stopRecording() {
        // stops the recording activity
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread.interrupt();
            recordingThread = null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (permissionToRecord && permissionToStore) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_AUDIO);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, READ_MEDIA_AUDIO}, 1);
    }

    //================================================================
    //смысловая часть приложения


    public final int CHUNK_SIZE = 4096;

    // Find out in which range
    public int getIndex(int freq) {
        int i = 0;
        while (band[i] < freq)
            i++;
        return i;
    }

    public static final int[] band = {40, 80, 120, 180, 300};
    public long[][] partsHash;
    public double[][] highScores;
    public double[] recordedHash;


    private void RecognizeMusic(byte[][] allBytes) {

        int sampledPartSize = allBytes.length;

        Complex[][] result = new Complex[sampledPartSize][];
        Complex[] mComplex = new Complex[CHUNK_SIZE];
        for (int j = 0; j < sampledPartSize; j++) {
            for (int i = 0; i < CHUNK_SIZE; i++) {
                mComplex[i] = new Complex(allBytes[j][i], 0);
            }
            result[j] = Complex.fft(mComplex);
        }

        highScores = new double[result.length][5];
        partsHash = new long[result.length][5];
        recordedHash = new double[result.length];

        for (int i = 0; i < highScores.length; i++) {
            for (int j = 0; j < 5; j++) {
                highScores[i][j] = 0;
            }
        }
        for (int t = 0; t < result.length; t++) {
            for (int fr = 40; fr <= 300; fr++) {
                double magnitude = Math.log(result[t][fr].abs() + 1);
                int index = getIndex(fr);
                if (magnitude > highScores[t][index]) {
                    highScores[t][index] = magnitude;
                    partsHash[t][index] = fr;
                }
            }
            recordedHash[t] = hash(partsHash[t][0], partsHash[t][1], partsHash[t][2], partsHash[t][3]);
        }

        Map<String, Integer> matches = new ArrayMap<>();
        List<Music> dBase = musicDao.getAll();
        for (Music music : dBase) {
            for (double hash : recordedHash) {
                if (hash == music.getHash()) {
                    matches.put(music.getName(), (matches.get(music.getName()) == null ? 0 : matches.get(music.getName())) + 1);
                    //System.out.println(music.getHash());
                }
            }
        }
        matches.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(max -> nameSongTV.setText(max.getKey()));
        System.out.println(matches);
        System.out.println(dBase.size());
        //        try (FileWriter writer = new FileWriter(Environment.getExternalStorageDirectory() + "/Download/recordBytes.txt", false)) {
//            writer.write(Arrays.deepToString(allBytes));
//            writer.flush();
//        } catch (IOException ex) {
//            System.out.println(ex.getMessage());
//        }

//
//        try (FileWriter writer = new FileWriter(Environment.getExternalStorageDirectory() + "/Download/recordHash.txt", false)) {
//            writer.write(Arrays.toString(recordedHash));
//            writer.flush();
//        } catch (IOException ex) {
//            System.out.println(ex.getMessage());
//        }
    }


//    InputStream in = Files.newInputStream(new File(fileName).toPath()); <-- надо вызвать в нужном месте
//    toByteArray(in);

    public byte[][] temp;

    public void toByteArray(InputStream in) throws IOException {
        byte[] buffer = new byte[4096];
        int t = 0;
        temp = new byte[4353][];
        // считываем байты из входного потока и сохраняем их в буфере
        while ((in.read(buffer)) != -1) {
            temp[t] = buffer.clone();
            t++;
        }
        System.out.println(temp.length);
        RecognizeMusic(temp);
        for (int i = 0; i < recordedHash.length; i++) {
            musicDao.insert(new Music("example", (long) recordedHash[i], i));
        }
    }


    private static final int FUZ_FACTOR = 3;

//    private long hash(double[] hash) {
//        long finalHash = 0;
//        long temp = 10000000000000000L;
//        for (int i = 0; i < 5; i++) {
//            finalHash += ((hash[i] - (hash[i] % FUZ_FACTOR)) * temp);
//            temp /= 100;
//        }
//        return finalHash;
//    }

    private long hash(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }
}