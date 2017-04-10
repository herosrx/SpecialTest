package com.cocoatest.st.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.net.TrafficStats;
import android.util.Log;

/**
 * information of network traffic
 */
public class TrafficInfo {

	private static final String LOG_TAG = "SpecialTest-" + TrafficInfo.class.getSimpleName();

	private String uid;
	private static final int UNSUPPORTED = -1;

	public TrafficInfo(String uid) {
		this.uid = uid;
	}

	/**
	 * 获取上传流量
	 * 
	 * @return sndTraffic
	 */
	public long getSendTraffic() {
		Log.d(LOG_TAG, "get sndtraffic information");
		Log.d(LOG_TAG, "uid===" + uid);
		long sndTraffic = UNSUPPORTED;

		sndTraffic = TrafficStats.getUidTxBytes(Integer.parseInt(uid));
		// 如果得到-1，去读系统文件
		if (sndTraffic != UNSUPPORTED) {
			return sndTraffic;
		} else {
			RandomAccessFile rafSnd = null;
			String sndPath = "/proc/uid_stat/" + uid + "/tcp_snd";

			try {
				rafSnd = new RandomAccessFile(sndPath, "r");
				sndTraffic = Long.parseLong(rafSnd.readLine());
			} catch (FileNotFoundException e) {
				sndTraffic = UNSUPPORTED;
			} catch (NumberFormatException e) {
				Log.d(LOG_TAG, "NumberFormatException: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(LOG_TAG, "IOException: " + e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					if (rafSnd != null)
						rafSnd.close();
				} catch (IOException e) {
					Log.d(LOG_TAG, "close randomAccessFile exception: " + e.getMessage());
				}
			}
			Log.d(LOG_TAG, "sndTraffic===" + sndTraffic);
			if (sndTraffic == UNSUPPORTED) {
				return UNSUPPORTED;
			} else {
				return sndTraffic;
			}
		}
	}

	/**
	 * 获取下载流量
	 * 
	 * @return rcvTraffic
	 */
	public long getRcvTraffic() {
		Log.d(LOG_TAG, "get rcvtraffic information");
		Log.d(LOG_TAG, "uid===" + uid);
		long rcvTraffic = UNSUPPORTED;

		rcvTraffic = TrafficStats.getUidRxBytes(Integer.parseInt(uid));
		// 如果得到-1，去读系统文件
		if (rcvTraffic != UNSUPPORTED) {
			return rcvTraffic;
		} else {
			RandomAccessFile rafRcv = null;
			String rcvPath = "/proc/uid_stat/" + uid + "/tcp_rcv";
			try {
				rafRcv = new RandomAccessFile(rcvPath, "r");
				rcvTraffic = Long.parseLong(rafRcv.readLine());
			} catch (FileNotFoundException e) {
				rcvTraffic = UNSUPPORTED;
			} catch (NumberFormatException e) {
				Log.d(LOG_TAG, "NumberFormatException: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(LOG_TAG, "IOException: " + e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					if (rafRcv != null) {
						rafRcv.close();
					}
				} catch (IOException e) {
					Log.d(LOG_TAG, "close randomAccessFile exception: " + e.getMessage());
				}
			}
			Log.d(LOG_TAG, "rcvTraffic===" + rcvTraffic);
			if (rcvTraffic == UNSUPPORTED) {
				return UNSUPPORTED;
			} else {
				return rcvTraffic;
			}
		}
	}
}
