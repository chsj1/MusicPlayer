package com.hjd.nature;


import com.example.nature.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	

	public void music(View view){
		System.out.println("------------>你淡季了音乐播放器的按钮");
		
		Intent i = new Intent(MainActivity.this,MusicActivity.class);
		startActivity(i);
		
	}
}
