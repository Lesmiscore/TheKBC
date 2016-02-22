package com.nao20010128nao.FuckingSeries.TheKBC;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import java.util.zip.ZipInputStream;
import java.io.BufferedInputStream;
import java.util.zip.ZipEntry;
import java.io.IOException;
import android.widget.Toast;
import java.io.OutputStream;
import android.view.SurfaceView;
import android.media.MediaPlayer;
import java.io.File;

public class MainActivity extends Activity {
	SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		pref=PreferenceManager.getDefaultSharedPreferences(this);
     	if(pref.getBoolean("done",false)){
			start();
		}else{
			ext();
		}
    }
	private void start(){
		try {
			SurfaceView sv=(SurfaceView)findViewById(R.id.surfaceView);
			MediaPlayer mp=new MediaPlayer();
			mp.setDataSource(new File(getFilesDir(), "thekbc.mp4").toString());
			mp.setDisplay(sv.getHolder());
			mp.prepare();
			mp.start();
		} catch (IllegalArgumentException e) {
			
		} catch (SecurityException e) {
			
		} catch (IllegalStateException e) {
			
		} catch (IOException e) {
			
		}
	}
	private void ext(){
		final ProgressDialog pd=new ProgressDialog(this);
		pd.setCancelable(false);
		pd.setMessage("0%");
		new AsyncTask<Void,Integer,Boolean>(){
			int size=11704734;
			public Boolean doInBackground(Void... a){
				int tmp=0;
				ZipInputStream zis=null;
				OutputStream os=null;
				try{
					zis=new ZipInputStream(new BufferedInputStream(getAssets().open("thekbc.zip")));
					os=openFileOutput("thekbc.mp4",MODE_PRIVATE);
					ZipEntry ze=zis.getNextEntry();
					if(!ze.getName().equals("thekbc.mp4")){
						return false;
					}
					//publishProgress((int)ze.getSize());
					byte[] buf=new byte[8192];
					int r=0;
					while(true){
						r=zis.read(buf);
						if(r<=0){
							break;
						}
						tmp+=r;
						publishProgress(tmp);
						os.write(buf,0,r);
					}
					return true;
				}catch(Throwable e){
					e.printStackTrace();
					return false;
				}finally{
					if(zis!=null){
						try {
							zis.close();
						} catch (IOException e) {
							
						}
					}
					if(os!=null){
						try {
							os.close();
						} catch (IOException e) {

						}
					}
				}
			}
			public void onPostExecute(Boolean r){
				if(r){
					pd.dismiss();
					pref.edit().putBoolean("done",true).commit();
					start();
				}else{
					Toast.makeText(MainActivity.this,"Error",1).show();
					finish();
				}
			}
			public void onProgressUpdate(Integer... a){
				int value=a[0];
				if(size==-1){
					size=value;
				}else{
					pd.setMessage(((int)(value/size*100))+"%");
				}
			}
		}.execute();
		pd.show();
	}
}
