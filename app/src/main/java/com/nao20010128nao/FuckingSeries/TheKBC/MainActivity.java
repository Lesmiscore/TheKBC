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
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	SharedPreferences pref;
	MediaPlayer mp;
	SurfaceView sv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		pref=PreferenceManager.getDefaultSharedPreferences(this);
		sv=(SurfaceView)findViewById(R.id.surfaceView);
		sv.getHolder().addCallback(new SurfaceHolder.Callback(){
			public void surfaceCreated(SurfaceHolder sh){
				if(mp!=null){
					mp.setDisplay(sh);
				}
				int height=sv.getHeight();
				double width=height/3*4;
				System.out.println(height);
				System.out.println(width);
				RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams((int)width,height);
				sv.setLayoutParams(lp);
			}
			public void surfaceDestroyed(SurfaceHolder sh){

			}
			public void surfaceChanged(SurfaceHolder sh,int a,int b,int c){

			}
		});
		
     	if(pref.getBoolean("done",false)){
			start();
		}else{
			ext();
		}
    }
	private void start(){
		try {
			mp=new MediaPlayer();
			mp.setDataSource(new File(getFilesDir(), "thekbc.mp4").toString());
			mp.setLooping(true);
			if(sv.getHolder().getSurface().isValid()){
				mp.setDisplay(sv.getHolder());
			}
			mp.prepare();
			mp.start();
		} catch (Throwable e) {
			e.printStackTrace();
			ext();
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
					pref.edit().putBoolean("done",true).apply();
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

	@Override
	protected void onDestroy() {
		// TODO: Implement this method
		super.onDestroy();
		if(mp!=null){
			mp.release();
		}
	}

	@Override
	protected void onPause() {
		// TODO: Implement this method
		super.onPause();
		if(mp!=null){
			mp.pause();
		}
	}

	@Override
	protected void onResume() {
		// TODO: Implement this method
		super.onResume();
		if(mp!=null){
			mp.start();
		}
	}
	
}
