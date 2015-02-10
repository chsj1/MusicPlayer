package com.hjd.nature;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.nature.R;
import com.hjd.nature.MusicLoader.MusicInfo;
import com.hjd.nature.NatureService.NatureBinder;

public class MusicActivity extends Activity implements OnClickListener{

	public static final String TAG = "com.example.nature.MAIN_ACTIVITY";
	
	private ListView lvSongs;	
	private SeekBar pbDuration;
	private TextView tvCurrentMusic;
	private List<MusicInfo> musicList;
	private int currentMusic; // The music that is playing.
	private int currentPosition; //The position of the music is playing.
	private int currentMax;
		
	private Button btnStartStop;
	private Button btnNext;
	private Button btnDetail;
	private Button btnPre;
	
	
	private ProgressReceiver progressReceiver;	
	private NatureBinder natureBinder;	
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			natureBinder = (NatureBinder) service;			
		}
	};
	
	
	/**
	 * 为这个Activity绑定服务
	 */
	private void connectToNatureService(){		
		Intent intent = new Intent(MusicActivity.this, NatureService.class);				
		bindService(intent, serviceConnection, BIND_AUTO_CREATE);				
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "OnCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //保持屏幕常亮..
		
		MusicLoader musicLoader = MusicLoader.instance(getContentResolver());//利用单例模式创建一个MusicLoader实例		
		musicList = musicLoader.getMusicList();//获取系统中的音乐信息
		connectToNatureService();
		initComponents();		
	}
	
	public void onResume(){
		Log.v(TAG, "OnResume register Progress Receiver");
		super.onResume();										
		registerReceiver();
		if(natureBinder != null){
			if(natureBinder.isPlaying()){
				btnStartStop.setBackgroundResource(R.drawable.pause);
			}else{
				btnStartStop.setBackgroundResource(R.drawable.play);
			}
			natureBinder.notifyActivity();
		}
	}
	
	public void onPause(){
		Log.v(TAG, "OnPause unregister Progress Receiver");
		super.onPause();
		unregisterReceiver(progressReceiver);
	}
	
	public void onStop(){
		Log.v(TAG, "OnStop");
		super.onStop();				
	}
	
	public void onDestroy(){
		Log.v(TAG, "OnDestroy");
		super.onDestroy();
		if(natureBinder != null){
			unbindService(serviceConnection);
		}
	}
		
	/**
	 * 初始化音乐播放界面
	 */
	private void initComponents(){		
		pbDuration = (SeekBar) findViewById(R.id.pbDuration);	
		pbDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					natureBinder.changeProgress(progress);
				}
			}
		});
		
		tvCurrentMusic = (TextView) findViewById(R.id.tvCurrentMusic);				
		
		btnStartStop = (Button)findViewById(R.id.btnStartStop);
		btnStartStop.setOnClickListener(this);
		
		btnNext = (Button)findViewById(R.id.btnNext);
		btnNext.setOnClickListener(this);
		
		btnPre = (Button)findViewById(R.id.btnPre);
		btnPre.setOnClickListener(this);
		
		
		btnDetail = (Button) findViewById(R.id.btnDetail);
		btnDetail.setOnClickListener(this);
		
		MusicAdapter adapter = new MusicAdapter();
		lvSongs = (ListView) findViewById(R.id.lvSongs);		
		lvSongs.setAdapter(adapter);
		lvSongs.setOnItemClickListener(new OnItemClickListener() {//给每一个item添加点击事件
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				currentMusic = position;
				natureBinder.startPlay(currentMusic,0);
				if(natureBinder.isPlaying()){					
					btnStartStop.setBackgroundResource(R.drawable.pause);		
				}
			}
		});
	}
	
	private void registerReceiver(){
		progressReceiver = new ProgressReceiver();	
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NatureService.ACTION_UPDATE_PROGRESS);
		intentFilter.addAction(NatureService.ACTION_UPDATE_DURATION);
		intentFilter.addAction(NatureService.ACTION_UPDATE_CURRENT_MUSIC);
		registerReceiver(progressReceiver, intentFilter);
	}
	
	class MusicAdapter extends BaseAdapter{

		@Override 
		public int getCount() {
			return musicList.size();
		}

		@Override
		public Object getItem(int position) {
			return musicList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return musicList.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder; 
			if(convertView == null){
				convertView = LayoutInflater.from(MusicActivity.this).inflate(R.layout.music_item, null);
				ImageView pImageView = (ImageView) convertView.findViewById(R.id.albumPhoto);
				TextView pTitle = (TextView) convertView.findViewById(R.id.title);
				TextView pDuration = (TextView) convertView.findViewById(R.id.duration);
				TextView pArtist = (TextView) convertView.findViewById(R.id.artist);
				viewHolder = new ViewHolder(pImageView, pTitle, pDuration, pArtist);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			viewHolder.imageView.setImageResource(R.drawable.audio);
			viewHolder.title.setText(musicList.get(position).getTitle());
			viewHolder.duration.setText(FormatHelper.formatDuration(musicList.get(position).getDuration()));
			viewHolder.artist.setText(musicList.get(position).getArtist());
			
			return convertView;
		}		
	}
	
	class ViewHolder{
		public ViewHolder(ImageView pImageView, TextView pTitle, TextView pDuration, TextView pArtist){
			imageView = pImageView;
			title = pTitle;
			duration = pDuration;
			artist = pArtist;
		}
		
		ImageView imageView;
		TextView title;
		TextView duration;
		TextView artist;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {		
		switch (v.getId()) {		
		case R.id.btnStartStop:		
			play(currentMusic,R.id.btnStartStop);
			break;		
		case R.id.btnNext:
			natureBinder.toNext();
			break;		
		case R.id.btnDetail:						
			Intent intent = new Intent(MusicActivity.this,DetailActivity.class);
			intent.putExtra(DetailActivity.MUSIC_LENGTH, currentMax);
			intent.putExtra(DetailActivity.CURRENT_MUSIC, currentMusic);
			intent.putExtra(DetailActivity.CURRENT_POSITION, currentPosition);			
			startActivity(intent);			
			break;
		case R.id.btnPre:
			natureBinder.toPrevious();
			break;
		}		
	}
	
	private void play(int position, int resId){		
		if(natureBinder.isPlaying()){
			natureBinder.stopPlay();
			btnStartStop.setBackgroundResource(R.drawable.play);
		}else{
			natureBinder.startPlay(position,currentPosition);
			btnStartStop.setBackgroundResource(R.drawable.pause);
		}
	}

	

	class ProgressReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(NatureService.ACTION_UPDATE_PROGRESS.equals(action)){
				int progress = intent.getIntExtra(NatureService.ACTION_UPDATE_PROGRESS, 0);
				if(progress > 0){
					currentPosition = progress; // Remember the current position
					pbDuration.setProgress(progress / 1000);
				}
			}else if(NatureService.ACTION_UPDATE_CURRENT_MUSIC.equals(action)){
				//Retrive the current music and get the title to show on top of the screen.
				currentMusic = intent.getIntExtra(NatureService.ACTION_UPDATE_CURRENT_MUSIC, 0);				
				tvCurrentMusic.setText(musicList.get(currentMusic).getTitle());
			}else if(NatureService.ACTION_UPDATE_DURATION.equals(action)){
				//Receive the duration and show under the progress bar
				//Why do this ? because from the ContentResolver, the duration is zero.
				currentMax = intent.getIntExtra(NatureService.ACTION_UPDATE_DURATION, 0);				
				int max = currentMax / 1000;
				Log.v(TAG, "[Main ProgressReciver] Receive duration : " + max);
				pbDuration.setMax(currentMax / 1000);						
			}
		}
		
	}
	
	public void testVoice(){
		int a ;
		int b;
		int c;
		int d;
		int e;
		
		
	}
}


