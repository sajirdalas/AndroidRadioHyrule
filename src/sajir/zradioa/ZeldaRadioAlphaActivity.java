package sajir.zradioa;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;

import sajir.zradio.R;
import sajir.zradioa.RadioService.LocalBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ZeldaRadioAlphaActivity extends Activity implements
		OnClickListener {


	ImageButton b,s;
	TextView tsong, talbum, tartist,playm;
	ImageView albumart;
	Boolean Playing = false;
	RadioService Radio;
	boolean mBound = false;
	String ImagePath, Artist, Album, Song;
	Drawable D;
	SharedPreferences data;
	boolean quality,inst,notion;
	Intent RadioInt;
	Toast toast;
	CharSequence text;
	MyReceiver r;
	int duration = Toast.LENGTH_LONG;
	SharedPreferences.Editor editor;
	ArrayList songs = new ArrayList(); 
	int numstosongs = 0;
	MediaPlayer cn;
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		data = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = data.edit();
		quality=data.getBoolean("quapref", true);
		notion=data.getBoolean("notipref", true);
		numstosongs=data.getInt("numberofsongs", 0);
		for(int i=0; i<numstosongs; i++){
            songs.add(data.getString(Integer.toString(i), ""));
       }
		numstosongs=songs.size();
		
		
		
		
		b = (ImageButton) findViewById(R.id.playpausebutton);
		s = (ImageButton) findViewById(R.id.songsave);
		tsong = (TextView) findViewById(R.id.song);
		talbum = (TextView) findViewById(R.id.album);
		tartist = (TextView) findViewById(R.id.artist);
		albumart = (ImageView) findViewById(R.id.albumart);
		playm = (TextView) findViewById(R.id.tv1);
		

		b.setOnClickListener(this);
		RadioInt=new Intent(this, RadioService.class);
		RadioInt.putExtra("Quality", quality);
		RadioInt.putExtra("Notification", notion);
		startService(RadioInt);
		bindService(RadioInt, mConnection,
				Context.BIND_AUTO_CREATE);
		
		IntentFilter filter = new IntentFilter("sajir.zradioa.UPDATE");
		r = new MyReceiver();
		registerReceiver(r, filter);
		if(!isNetworkAvailable()){
			text = "Connection Issues. Please Check Your Internet Conection";
			toast = Toast.makeText(this, text, duration);
			toast.show();
			finish();
		}
		

		
		s.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				songs.add(Song+" - "+Artist);
				numstosongs = songs.size();
				text = "Song title saved to favorites";
				toast = Toast.makeText(getApplicationContext(), text, duration);
				toast.show();
				cn = MediaPlayer.create(getApplicationContext(), R.raw.chest);
				cn.setOnCompletionListener(new OnCompletionListener(){
	
					
					public void onCompletion(MediaPlayer mp) {
						cn.release();
					}});
				cn.start();
				
			}
				
		});

		
		if (Playing) {

			playm.setText("Pause");
			
		} else {
			

			playm.setText("Play");

		}
		
	}

	public void onClick(View arg0) {
		
		if (Playing) {

			b.setImageResource(R.drawable.r1);
			Radio.pause();
			Playing = false;
			playm.setText("Play");
			
		} else {
			
			b.setImageResource(R.drawable.r2);
			Playing = true;
			text = "Buffering... Please wait";
			toast = Toast.makeText(this, text, toast.LENGTH_LONG);
			toast.show();
			Radio.play();
			playm.setText("Pause");

		}
	}

	

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			Radio = binder.getService();
			mBound = true;

		}

		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;

		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(Playing){Radio.pause();}
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		stopService(new Intent(this, RadioService.class));
		unregisterReceiver(r);
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.i("Control","Broadcast recivido");
			ImagePath = arg1.getStringExtra("AlbumArt");
			Artist = arg1.getStringExtra("Artist");
			Album = arg1.getStringExtra("Album");
			Song = arg1.getStringExtra("Song");
			if(ImagePath==null){
				text = "Connection Issues. Please Check Your Internet Conection";
				toast = Toast.makeText(this, text, duration);
				toast.show();
				finish();
			}else{
			UpdateTag();}
		}

	}
	
	public void UpdateTag(){
		
		tsong.setText(Html.fromHtml("<b>Song: </b>"+"<i>"+Song+"</i>"));
		talbum.setText(Html.fromHtml("<b>Album: </b>"+"<i>"+Album+"</i>"));
		tartist.setText(Html.fromHtml("<b>Artist: </b>"+"<i>"+Artist+"</i>"));
		SetImage();
		
		
		
	}
	
	public void LightUpdateTag(){
		tsong.setText(Html.fromHtml("<b>Song: </b>"+"<i>"+Song+"</i>"));
		talbum.setText(Html.fromHtml("<b>Album: </b>"+"<i>"+Album+"</i>"));
		tartist.setText(Html.fromHtml("<b>Artist: </b>"+"<i>"+Artist+"</i>"));
	}
	
	public void SetImage(){
		 try {
				URL url = new URL(getString(R.string.site)+ImagePath);

			    Object content = url.getContent();
			    InputStream is = (InputStream)content;
				D = Drawable.createFromStream(is, "src");
				albumart.setImageDrawable(D);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				
			} catch (IOException e) {
				e.printStackTrace();
				
			}
	 }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.main);
		InitializeViews();
		LightUpdateTag();
		albumart.setImageDrawable(D);
	}

	public void InitializeViews(){
		b = (ImageButton) findViewById(R.id.playpausebutton);
		tsong = (TextView) findViewById(R.id.song);
		talbum = (TextView) findViewById(R.id.album);
		tartist = (TextView) findViewById(R.id.artist);
		albumart = (ImageView) findViewById(R.id.albumart);

		b.setOnClickListener(this);
		if(!Playing){b.setImageResource(R.drawable.r1);}
		else{b.setImageResource(R.drawable.r2);}
		
		s = (ImageButton) findViewById(R.id.songsave);
		s.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				songs.add(Song+" - "+Artist);
				numstosongs = songs.size();
				text = "Song title saved to favorites";
				toast = Toast.makeText(getApplicationContext(), text, duration);
				toast.show();
				cn = MediaPlayer.create(getApplicationContext(), R.raw.chest);
				cn.setOnCompletionListener(new OnCompletionListener(){
	
					
					public void onCompletion(MediaPlayer mp) {
						cn.release();
					}});
				cn.start();
				
			}
	
	});
	}

	@Override
	protected void onResume() {
		super.onResume();
			//Radio.RequestInmediateSongData();
			albumart.setImageDrawable(D);
		
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater blow = getMenuInflater();
		blow.inflate(R.menu.defmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.quality:
		if(quality){
			quality=false;
			
		}else{
			quality=true;
			
		}
		editor.putBoolean("quapref", quality);
		editor.commit();
		b.setImageResource(R.drawable.r1);
		Radio.pause();
		Playing = false;
		stopService(RadioInt);
		unbindService(mConnection);
		mBound = false;
		RadioInt.putExtra("Quality", quality);
		startService(RadioInt);
		bindService(RadioInt, mConnection,
				Context.BIND_AUTO_CREATE);
		break;
		case R.id.notification:
			Log.i("Notice","notion button triggered");
			if(notion){
				notion=false;
			}else{
				notion=true;
			}
			editor.putBoolean("notipref", notion);
			editor.commit();
			b.setImageResource(R.drawable.r1);
			Radio.pause();
			Playing = false;
			stopService(new Intent(this, RadioService.class));
			unbindService(mConnection);
			mBound = false;
			RadioInt.putExtra("Notification", notion);
			startService(RadioInt);
			bindService(RadioInt, mConnection,
					Context.BIND_AUTO_CREATE);
		break;
		case R.id.saved:
			Object[] temparr = songs.toArray();
			String[] temparr2 = Arrays.asList(temparr).toArray(new String[temparr.length]);
			numstosongs=songs.size();
			editor.putInt("numberofsongs", numstosongs);
			
			for(int i=0; i<numstosongs; i++){
	            editor.putString(Integer.toString(i), temparr2[i]);
	       }
			editor.commit();
			
			Intent intent = new Intent(getApplicationContext(),songlist.class);
			intent.putExtra("lista", temparr2);
			startActivity(intent);
		}
		return true;
		
	}

	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		super.onPrepareOptionsMenu(menu);
		if(quality){
		menu.findItem(R.id.quality).setTitle("Change to low quality");}
		else{
			menu.findItem(R.id.quality).setTitle("Change to hi quality");
		}
		if(notion){
			menu.findItem(R.id.notification).setTitle("Toogle notification Off");
		}else{
			menu.findItem(R.id.notification).setTitle("Toogle notification On");
		}
		return true;
	}

	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}
	
	


	
	
}