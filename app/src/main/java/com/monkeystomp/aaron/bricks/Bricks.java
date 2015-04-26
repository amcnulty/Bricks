package com.monkeystomp.aaron.bricks;

import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class Bricks extends ActionBarActivity {

    private BricksView mBricksView;

    private boolean homeWasPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("__________onCreate", "on Create");
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_bricks);

        // Instantiate BricksView and create a object variable for this class
        mBricksView = (BricksView) findViewById(R.id.bricks);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bricks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.reset_game:
                mBricksView.resetGame();
                mBricksView.level.resetLevel();
                return true;
            case R.id.stop_music:
                mBricksView.level.muteMusic();
                return true;
            case R.id.play_music:
                mBricksView.level.unMuteMusic();
                return true;
            case R.id.next_track:
                Log.v(null, "Change Tracks Not Working!");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This is called when the user presses the back button, home button, or a phonecall is received
     */
    @Override
    public void onPause() {
        mBricksView.setRunning(false);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // If a phone call is received and surface created isn't called,
        // this will make sure that the running variable is set to true.
        mBricksView.setRunning(true);
        if (homeWasPressed && mBricksView.gameState != mBricksView.GAME_PAUSED) {
            mBricksView.resumeMusic();
        }
        Log.v("________onResume", "Program is resuming");
    }

    @Override
    public void onStop() {
        if (mBricksView.gameState != mBricksView.GAME_READY) mBricksView.gameState = mBricksView.GAME_PAUSED;
        super.onStop();
        Log.v("__________onStop", "Program is stopped");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBricksView.stopMusic();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        homeWasPressed = true;
    }

}
