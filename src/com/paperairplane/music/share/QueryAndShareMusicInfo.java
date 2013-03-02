package com.paperairplane.music.share;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

class QueryAndShareMusicInfo extends Thread {
	private int means;
	private Long album_id;
	private String artist, title, album;
	private Context context;
	private Handler handler;
	private final String ARTWORK_PATH = Environment
			.getExternalStorageDirectory() + "/music_share/";

	public void run() {
		String[] info = Utilities.getMusicAndArtworkUrl(title, artist, context,
				handler);
		String content;
		content = genContent(info);

		String artworkUrl = null;
		if (info[Consts.ArraySubscript.ARTWORK] != null) {
			artworkUrl = info[Consts.ArraySubscript.ARTWORK].replace("spic", "lpic");
		}
		Bundle bundle = new Bundle();
		bundle.putString("content", content);
		bundle.putString("artworkUrl", artworkUrl);
		switch (means) {
		case Consts.ShareMeans.OTHERS:
			String fileName = null;
			String type = "text/plain";
			try{
			if ((album_id !=  null)&&(Utilities.getLocalArtwork(context, album_id, 120,120) != null)){
				Utilities.saveFile(Utilities.getLocalArtwork(context, album_id, 120,120), title + ".jpg", ARTWORK_PATH);
				fileName = ARTWORK_PATH + title + ".jpg";
				Log.d(Consts.DEBUG_TAG,"��ȡ���ط���ɹ�");
			}else{
				fileName = ARTWORK_PATH
						+ Utilities.getArtwork(artworkUrl, title, ARTWORK_PATH);
			}
			}catch(Exception e){
				Log.e(Consts.DEBUG_TAG, "Error Occured");
				e.printStackTrace();
				fileName = ARTWORK_PATH
						+ Utilities.getArtwork(artworkUrl, title, ARTWORK_PATH);
			}
			Intent intent = new Intent(Intent.ACTION_SEND);
			if (fileName != null) {
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fileName)));
				Log.d(Consts.DEBUG_TAG, "Intent " + fileName);
				type = "image/*";
			}
			intent.setType(type);
			intent.putExtra(Intent.EXTRA_SUBJECT,
					context.getString(R.string.app_name));
			intent.putExtra(Intent.EXTRA_TEXT, content);
			context.startActivity(Intent.createChooser(intent,
					context.getString(R.string.how_to_share)).addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case Consts.ShareMeans.WEIBO:
			Message m = handler.obtainMessage(Consts.Status.SEND_WEIBO, bundle);
			handler.sendMessage(m);
			break;
		}

	}

	private String genContent(String[] info) {
		boolean isSingle = ((info[Consts.ArraySubscript.VERSION] != null) && info[Consts.ArraySubscript.VERSION]
				.equals(context.getString(R.string.single)));
		String content = context.getString(R.string.share_by)
				+ " "
				+ ((artist.equals("")) ? info[Consts.ArraySubscript.ARTIST] : artist)
				+ " "
				+ (isSingle ? context.getString(R.string.music_single)
						: context.getString(R.string.music_artist))
				+ " "
				+ title
				+ " "
				+ (isSingle ? "" : context.getString(R.string.music_album)
						+ " " + ((album.equals("")) ? info[Consts.ArraySubscript.ALBUM] : album)
						+ " ") + context.getString(R.string.before_url)
				+ info[Consts.ArraySubscript.MUSIC] + " ";
		return content;
	}

	public QueryAndShareMusicInfo(String _title, String _artist, String _album,Long _album_id,
			int _means, Context _context, Handler _handler) {
		album_id = _album_id;
		title = _title;
		artist = _artist;
		album = _album;
		means = _means;
		context = _context;
		handler = _handler;
	}

}