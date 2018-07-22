package me.dpeng.clickdots;

import android.annotation.SuppressLint;
import android.graphics.Canvas;

/**
 * Created by Daniel Peng on 7/22/2018.
 */

public class GameThread extends Thread{

    private GameView view;
    private boolean running = false;

    public GameThread(GameView view) {
        this.view = view;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        while(running) {
            Canvas c = null;
            try {
                c = view.getHolder().lockCanvas();
                if(c != null) {
                    synchronized (view.getHolder()) {
                        view.onDraw(c);
                    }
                }
            } finally {
                if(c != null) {
                    view.getHolder().unlockCanvasAndPost(c);
                }
            }
        }
    }
}
