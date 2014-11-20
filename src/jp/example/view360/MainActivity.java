/* 
 * MainActivity.java
 * 
 * Copyright (C) 2014 M.Nakamura
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit
 *  http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */
package jp.example.view360;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private static String Tag = "MainActivity";
	private static int MATRIX_SIZE = 16;
	private static int DIMENSION = 3;
	private static int VIEW_ANGLE = 60;
	private static int WIDTH_ANGLE = 540;
	private SensorManager mSensorManager = null;
	private float mMagneticValues[] = null;
	private float mAccelerometerValues[] = null;
	private Bitmap mBitmapImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(Tag, "onCreate");
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);

			// SensorManagerのインスタンスを取得する
			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			mBitmapImage = BitmapFactory.decodeResource(getResources(),
					R.drawable.ebisubasi45);
		} catch (Exception e) {
			Log.e(Tag, e.getMessage());
		}
	}

	// * onResume */
	@Override
	protected void onResume() {
		Log.i(Tag, "onResume");
		try {
			super.onResume();

			// 磁気センサー
			List<Sensor> sensorsMF = mSensorManager
					.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

			// センサマネージャへリスナーを登録
			if (sensorsMF.size() > 0) {
				Sensor sensor = sensorsMF.get(0);
				mSensorManager.registerListener(onSensorEventListener, sensor,
						SensorManager.SENSOR_DELAY_NORMAL);
			}

			// 加速度センサー
			List<Sensor> sensorsAcc = mSensorManager
					.getSensorList(Sensor.TYPE_ACCELEROMETER);

			// センサマネージャへリスナーを登録
			if (sensorsAcc.size() > 0) {
				Sensor sensor = sensorsAcc.get(0);
				mSensorManager.registerListener(onSensorEventListener, sensor,
						SensorManager.SENSOR_DELAY_NORMAL);
			}
		} catch (Exception e) {
			Log.e(Tag, e.getMessage());
		}
	}

	// * onPause */
	@Override
	protected void onPause() {
		Log.i(Tag, "onPause");
		try {
			super.onPause();

			// 磁気センサー
			List<Sensor> sensorsMF = mSensorManager
					.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

			// センサマネージャへリスナーを登録
			if (sensorsMF.size() > 0) {
				Sensor sensor = sensorsMF.get(0);
				mSensorManager
						.unregisterListener(onSensorEventListener, sensor);
			}

			// 加速度センサー
			List<Sensor> sensorsAcc = mSensorManager
					.getSensorList(Sensor.TYPE_ACCELEROMETER);

			// センサマネージャへリスナーを登録
			if (sensorsAcc.size() > 0) {
				Sensor sensor = sensorsAcc.get(0);
				mSensorManager
						.unregisterListener(onSensorEventListener, sensor);
			}
		} catch (Exception e) {
			Log.e(Tag, e.getMessage());
		}
	}

	private final SensorEventListener onSensorEventListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			Log.i(Tag, "onAccuracyChanged");
			// センサーの精度が変更されると呼ばれる
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// Log.i(Tag, "onSensorChanged");
			try {
				// センサーの値が変化すると呼ばれる
				switch (event.sensor.getType()) {
				case Sensor.TYPE_MAGNETIC_FIELD: // 地磁気センサ
					mMagneticValues = event.values.clone();
					break;
				case Sensor.TYPE_ACCELEROMETER: // 加速度センサ
					mAccelerometerValues = event.values.clone();
					break;
				}

				if (mMagneticValues != null && mAccelerometerValues != null) {
					float[] rotationMatrix = new float[MATRIX_SIZE];
					float[] inclinationMatrix = new float[MATRIX_SIZE];
					float[] remapedMatrix = new float[MATRIX_SIZE];

					float[] orientationValues = new float[DIMENSION];

					// 加速度センサと地磁気センサから回転行列を取得
					SensorManager.getRotationMatrix(rotationMatrix,
							inclinationMatrix, mAccelerometerValues,
							mMagneticValues);
					SensorManager.remapCoordinateSystem(rotationMatrix,
							SensorManager.AXIS_X, SensorManager.AXIS_Z,
							remapedMatrix);
					SensorManager.getOrientation(remapedMatrix,
							orientationValues);
					// 求まった方位角．ラジアンなので度に変換する
					float orientDegree = (float) Math
							.toDegrees(orientationValues[0]);
					Log.d(Tag, "orientDegree=" + orientDegree);
					viewChange(orientDegree);
				}
			} catch (Exception e) {
				Log.e(Tag, e.getMessage());
			}
		}

		private void viewChange(float orientDegree) {
			Log.i(Tag, "viewChange");
			try {
				float width = (float) mBitmapImage.getWidth() / WIDTH_ANGLE;
				int left = (int) ((180 + orientDegree - VIEW_ANGLE) * width);
				int right = (int) ((180 + orientDegree + VIEW_ANGLE) * width);
				Bitmap newBitmapImage = Bitmap.createBitmap(mBitmapImage, left,
						0, right - left, mBitmapImage.getHeight());
				ImageView imageView = (ImageView) findViewById(R.id.imageView1);
				imageView.setImageBitmap(newBitmapImage);
			} catch (Exception e) {
				Log.e(Tag, e.getMessage());
			}
		}
	};
}
