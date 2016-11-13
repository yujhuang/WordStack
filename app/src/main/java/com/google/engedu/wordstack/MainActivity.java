package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import static android.R.id.undo;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack<LetterTile> placedTiles = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if(word.length()== WORD_LENGTH) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        //word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                    }

                    placedTiles.push(tile);
                    if(placedTiles.size() == WORD_LENGTH*2) {
                        Button undo = (Button) findViewById(R.id.button);
                        undo.setEnabled(false);
                        LinearLayout layout1 = (LinearLayout) findViewById(R.id.word1);
                        LinearLayout layout2 = (LinearLayout) findViewById(R.id.word2);
                        String w1 = "",w2 = "";
                        for(int i = 0; i < WORD_LENGTH;i++) {
                            LetterTile temp1 = (LetterTile) layout1.getChildAt(i);
                            LetterTile temp2 = (LetterTile)layout2.getChildAt(i);
                            w1 += temp1.getLetter();
                            w2 += temp2.getLetter();
                        }
                        TextView messageBox = (TextView) findViewById(R.id.message_box);

                        String m1 = "",m2="";
                        if(words.contains(w1)) {
                            m1 = w1+" is valid, ";
                        }
                        if(words.contains(w2)) {
                            m2 = w2 + " is valid";
                        }
                        messageBox.setText(m1+m2);
                    }
                    return true;
            }
            return false;
        }
    }

    protected boolean onStartGame(View view) {
        LinearLayout layout1 = (LinearLayout) findViewById(R.id.word1);
        LinearLayout layout2 = (LinearLayout) findViewById(R.id.word2);
        layout1.removeAllViews();
        layout2.removeAllViews();
        placedTiles.clear();
        stackedLayout.clear();
        Button undo = (Button) findViewById(R.id.button);
        undo.setEnabled(true);
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");
        int length = words.size();
        int ran1 = random.nextInt(length);
        word1 = words.get(ran1);
        int ran2 =  random.nextInt(length);
        while(ran2 ==ran1) {
            ran2 =  random.nextInt(length);
        }
        word2 = words.get(ran2);
        Log.d("test",word1+" "+word2);
        int done = 0;
        ArrayList<String> scramble = new ArrayList<>();
        int rest1 = WORD_LENGTH,rest2 = WORD_LENGTH;
        int start =  random.nextInt(2);
        int nextstart1 = 0,nextstart2 = 0;
        int swap = 0;
        if(start == 0) {
            int ranIndex = random.nextInt(rest1-1);
            String strpick = word1.substring(0,ranIndex+1);
            scramble.add(strpick);
            rest1 -= (ranIndex+1);
            nextstart1 = ranIndex+1;

            ranIndex = random.nextInt(rest2-1);
            strpick = word2.substring(0,ranIndex+1);
            scramble.add(strpick);
            rest2 -= (ranIndex+1);
            nextstart2 = ranIndex+1;

            swap = 0;
        }else {
            int ranIndex = random.nextInt(rest2-1);
            String strpick = word2.substring(0,ranIndex+1);
            scramble.add(strpick);
            rest2 -= (ranIndex+1);
            nextstart2 = ranIndex+1;

            ranIndex = random.nextInt(rest1-1);
            strpick = word1.substring(0,ranIndex+1);
            scramble.add(strpick);
            rest1 -= (ranIndex+1);
            nextstart1 = ranIndex+1;

            swap = 1;
        }
        while(done == 0) {
            if(swap == 0) {
                int ranIndex = random.nextInt(rest1);
                String strpick = word1.substring(nextstart1,ranIndex+nextstart1+1);
                scramble.add(strpick);
                rest1 -= (ranIndex+1);
                nextstart1 = ranIndex+nextstart1+1;
                if(rest2 != 0) {
                    swap = 1;
                }
            }else {
                int ranIndex = random.nextInt(rest2);
                String strpick = word2.substring(nextstart2,ranIndex+nextstart2+1);
                scramble.add(strpick);
                rest2 -= (ranIndex+1);
                nextstart2 = ranIndex+nextstart2+1;
                if(rest1 != 0) {
                    swap = 0;
                }
            }

            if(rest1 == 0&&rest2 == 0) {
                String temp = "";
                for(int i = 0; i < scramble.size();i++) {
                    temp += scramble.get(i);
                }
                char[] letters = temp.toCharArray();
                messageBox.setText(temp);
                for(int i = letters.length-1; i >= 0;i-- ) {
                    LetterTile letter = new LetterTile(view.getContext(),letters[i]);
                    stackedLayout.push(letter);
                }
                done =1;
            }
        }

        return true;
    }

    protected boolean onUndo(View view) {
        if(!placedTiles.empty()) {
            LetterTile tile = placedTiles.pop();
            tile.moveToViewGroup(stackedLayout);
            return true;
        }
        return false;
    }
}
