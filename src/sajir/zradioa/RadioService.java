package sajir.zradioa;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import sajir.zradio.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewDebug.FlagToString;
import android.widget.Toast;

public class RadioService extends Service implements OnPreparedListener, OnErrorListener{

	MediaPlayer mediaPlayer;
	Toast toast;
	CharSequence text;
	int duration = Toast.LENGTH_LONG;
	private final IBinder mBinder = new LocalBinder();
	String ImagePath="",Artist="",Album="",Song="0",OldSong="",artsize="500";
	Boolean playings=false;
	Boolean		quality,notification,threadisrunning=false;
	Drawable D;
	NotificationManager NotMan;
	PendingIntent PI;
	Intent i;
	TagMonitor mon;
	Intent reciv;
	
	@Override
	public void onCreate() {
		Log.d("Prueba", "onCreate");
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnErrorListener(this);
		i = new Intent(this,ZeldaRadioAlphaActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PI = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
		NotMan = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		

	    

	
	}
	
	


	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("Prueba", "onStart");
		
			reciv=intent;

		threadisrunning=true;
		mon = new TagMonitor();
		mon.start();
		RequestInmediateSongData();
		
		
	}

	

	@Override
	public void onDestroy() {
		Log.d("Prueba", "onDestroy");
		if(playings){mediaPlayer.stop();}
		NotMan.cancel(1);
		playings=false;
		threadisrunning=false;
	}



	@Override
	public IBinder onBind(Intent arg0) {
		Log.i("Prueba","Binding requested");
		return mBinder;
	}
	
	public void play() {
		
		try {
			setSource();
			mediaPlayer.prepareAsync();
			
			
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		playings=true;
		RefreshNotification();
	}

	public void pause(){
		if(playings){
		mediaPlayer.stop();
		mediaPlayer.reset();
		}
		playings=false;
		NotMan.cancel(1);
	}
	
	 public class LocalBinder extends Binder {
	        RadioService getService() {
	            // Return this instance of LocalService so clients can call public methods
	            return RadioService.this;
	        }
	    }

	
	 public void GetSongData(){
			
		  
		    try {
		    	Connection con = Jsoup.connect(getString(R.string.site));
		    	con.timeout(5000);
		    	Document doc = con.get();
		    	Elements imagen = doc.select("#nowplaying-img");
		    	Elements autor = doc.select("#nowplaying-artist");
		    	Elements album = doc.select("#nowplaying-album");
		    	Elements song = doc.select("#nowplaying-song");
		    	ImagePath = imagen.attr("src");
		    	
		    	if ((getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {     
		            artsize="175";
		        }
		    	else
		    	{artsize="500";}

		    	
		    	ImagePath = ImagePath.replace("cover81", "cover"+artsize);
		    	Log.i("prueba", ImagePath);
		    	Artist = autor.text();
//		    	Log.i("prueba", Artist);
		    	Album = album.text();
//		    	Log.i("prueba", Album);
		    	Song = song.text();
//		    	Log.i("prueba", Song);
		      }
		      catch(IOException ioe)
		      {
		    	  ImagePath=null;
		    	  Artist=null;
		    	  Album=null;
		    	  Song=null;
		          ioe.printStackTrace();
		       }
		      catch(Exception e)
		      {
		    	  ImagePath=null;
		    	  Artist=null;
		    	  Album=null;
		    	  Song=null;
		         e.printStackTrace();
		       }
		    }
	
	 public class TagMonitor extends Thread{
		 public void run(){
			 while(threadisrunning){
				 
				 while(playings){
			 
				OldSong=Song;
			 Log.i("info", OldSong);
			 GetSongData();
			 Log.i("info", Song);
			 
			 if(!OldSong.contentEquals(Song)){
			 Intent intent = new Intent();
			 intent.setAction("sajir.zradioa.UPDATE");
		       intent.putExtra("AlbumArt", ImagePath);
		       intent.putExtra("Artist", Artist);
		       intent.putExtra("Album", Album);
		       intent.putExtra("Song", Song);
		       sendBroadcast(intent);
		       Log.i("control","broadcast enviado");
		       if(playings){
		       RefreshNotification();
		       
		       }
		       
			 }
			 SystemClock.sleep(5000);
			 
			 }   
				 SystemClock.sleep(11000);
			 }
		 }
	 }
	 
	 public void RefreshNotification(){
		 if(notification){
		 Notification n = new Notification();
		 n.icon=R.drawable.ocarina;
		 n.tickerText="Radio Hyrule";
		 n.number=0;
		 
		 if(Album.contentEquals("")){
		 n.setLatestEventInfo(getApplicationContext(), Song, Artist, PI);}
		 else{
			 n.setLatestEventInfo(getApplicationContext(), Song, Artist+" - "+Album, PI);}
		 NotMan.notify(1,n);
		 }}
	 
	 public boolean IsPlaying(){
		 return playings;
	 }
	 
	 public void RequestInmediateSongData(){
		 GetSongData();
		 Intent intent = new Intent();
		 intent.setAction("sajir.zradioa.UPDATE");
	       intent.putExtra("AlbumArt", ImagePath);
	       intent.putExtra("Artist", Artist);
	       intent.putExtra("Album", Album);
	       intent.putExtra("Song", Song);
	       Log.i("control","broadcast enviado");
	       sendBroadcast(intent);
	 }




	
	 
	 public void onPrepared(MediaPlayer arg0) {
		 if(playings){
		 mediaPlayer.start();}
		
	}

public void setSource(){
	quality=reciv.getExtras().getBoolean("Quality");
	notification=reciv.getExtras().getBoolean("Notification");
	if(quality){
	try {
		mediaPlayer.setDataSource(getString(R.string.url));
		Log.i("info", "Hi quality mode triggered");
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		text = "Connection Issues. Please Check Your Internet Conection";
		toast = Toast.makeText(this, text, duration);
		toast.show();
	}}
	else{
	try {
		mediaPlayer.setDataSource(getString(R.string.urllow));
		Log.i("info", "Low quality mode triggered");
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		text = "Connection Issues. Please Check Your Internet Conection";
		toast = Toast.makeText(this, text, duration);
		toast.show();
	}}
}




public boolean onError(MediaPlayer mp, int what, int extra) {
	if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED){
	text = "CONNECTION ERROR. Please try again later";
	toast = Toast.makeText(this, text, duration);
	toast.show();
	}
	return false;
}

}
