package org.pyneo.cam;

import android.content.res.Configuration;
import android.util.Size;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class UnattendedPic {
	private static final String TAG = "org.pyneo";
	private static final boolean DEBUG = true;
	private static class ImageSaver implements Runnable {
		private final Image mImage;
		private final File mFile;
		public ImageSaver(Image image, File file) {
			if (DEBUG) Log.d(TAG, "ImageSaver");
			mImage = image;
			mFile = file;
		}
		@Override public void run() {
			if (DEBUG) Log.d(TAG, "ImageSaver.run");
			try {
				ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
				byte[] bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				FileOutputStream output = new FileOutputStream(mFile);
				try {
					output.write(bytes);
					if (DEBUG) Log.d(TAG, "ImageSaver.run mFile=" + mFile);
				}
				finally {
					output.close();
				}
			}
			catch (Exception e) {
				Log.e(TAG, "caught an exception=" + e, e);
			}
			finally {
				mImage.close();
			}
		}
	}
	private HandlerThread mBackgroundThread;
	private Handler mBackgroundHandler;
	private ImageReader mImageReader;
	private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
		@Override public void onImageAvailable(ImageReader reader) {
			if (DEBUG) Log.d(TAG, "onImageAvailable");
			mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage(), mFile));
		}
	};
	private CameraDevice mCameraDevice;
	private Semaphore mCameraOpenCloseLock = new Semaphore(1);
	private CameraCaptureSession mCaptureSession;
	private CaptureRequest.Builder captureBuilder;
	private CameraCaptureSession.CaptureCallback captureCallback;
	private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
		@Override public void onOpened(CameraDevice cameraDevice) {
			if (DEBUG) Log.d(TAG, "onOpened");
			mCameraOpenCloseLock.release();
			mCameraDevice = cameraDevice;
			try {
				captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
				Surface surface = mImageReader.getSurface();
				captureBuilder.addTarget(surface);
				captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
				captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
				captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
				mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
					@Override public void onConfigured(CameraCaptureSession cameraCaptureSession) {
						if (DEBUG) Log.d(TAG, "onConfigured");
						if (null == mCameraDevice) {
							return;
						}
						mCaptureSession = cameraCaptureSession;
						captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
						captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
						captureCallback = new CameraCaptureSession.CaptureCallback() {
							@Override public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
								if (DEBUG) Log.d(TAG, "onCaptureCompleted");
								captured(mFile);
							}
						};
						if (DEBUG) Log.d(TAG, "onConfigured: capture!");
						try {
							mCaptureSession.capture(captureBuilder.build(), captureCallback, null);
						} catch (Exception e) {
							Log.e(TAG, "caught an exception", e);
						}
					}
					@Override public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
						Log.i(TAG, "onConfigureFailed");
					}
				}, null);
			} catch (Exception e) {
				Log.e(TAG, "caught an exception", e);
			}
		}
		@Override public void onDisconnected(CameraDevice cameraDevice) {
			if (DEBUG) Log.d(TAG, "onDisconnected");
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
		}
		@Override public void onError(CameraDevice cameraDevice, int error) {
			if (DEBUG) Log.d(TAG, "onError error=" + error);
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
		}
	};
	private File mFile;

	public void capture(Activity activity) {
		if (DEBUG) Log.d(TAG, "capture!");
		boolean isLS = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if (DEBUG) Log.d(TAG, "capture isLS=" + isLS);
		boolean isR = activity.getWindowManager().getDefaultDisplay().getRotation() != Surface.ROTATION_0;
		if (DEBUG) Log.d(TAG, "capture isR=" + isR);
		if (mImageReader == null) {
			mBackgroundThread = new HandlerThread("CameraBackground");
			mBackgroundThread.start();
			mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
			mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(), "pic.jpg");
			try {
				CameraManager manager = (CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);
				for (String cameraId: manager.getCameraIdList()) {
					if (DEBUG) Log.d(TAG, "doTest cameraId=" + cameraId);
					CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
					if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
						for (Key<?> key: characteristics.getKeys()) {
							if (DEBUG) Log.d(TAG, "doTest key=" + key.getName() + ", value=" + characteristics.get(key));
						}
						if (DEBUG) Log.d(TAG, "doTest characteristics=" + characteristics);
						StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
						if (DEBUG) Log.d(TAG, "doTest map=" + map);
						int width = 960;//800;
						int height = 720;//600;
						for (Size o: map.getOutputSizes(ImageFormat.JPEG)) {
							if (DEBUG) Log.d(TAG, "doTest o=" + o);
							if (700 < o.getWidth() && o.getWidth() < 1000) {
								width = o.getWidth();
								height = o.getHeight();
							}
						}
						if (DEBUG) Log.d(TAG, "doTest width=" + width + ", height=" + height);
						mImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2);
						mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
						manager.openCamera(cameraId, mStateCallback, mBackgroundHandler);
						break; // go on in onOpened, it's async
					}
				}
			}
			catch (CameraAccessException e) {
				Log.e(TAG, "caught exception", e);
			}
		}
		else {
			try {
				mCaptureSession.capture(captureBuilder.build(), captureCallback, null);
			} catch (Exception e) {
				Log.e(TAG, "caught an exception", e);
			}
		}
	}

	public void stop() {
		try {
			mCameraOpenCloseLock.acquire();
			if (null != mCaptureSession) {
				mCaptureSession.close();
				if (DEBUG) Log.d(TAG, "onPause: mCaptureSession.close");
				mCaptureSession = null;
			}
			if (null != mCameraDevice) {
				mCameraDevice.close();
				if (DEBUG) Log.d(TAG, "onPause: mCameraDevice.close");
				mCameraDevice = null;
			}
			if (null != mImageReader) {
				mImageReader.close();
				if (DEBUG) Log.d(TAG, "onPause: mImageReader.close");
				mImageReader = null;
			}
		} catch (InterruptedException e) {
			Log.e(TAG, "caught an exception", e);
		} finally {
			mCameraOpenCloseLock.release();
		}
		try {
			mBackgroundThread.join();
			if (DEBUG) Log.d(TAG, "onPause: mBackgroundThread.join");
			mBackgroundThread = null;
			mBackgroundHandler = null;
		} catch (InterruptedException e) {
			Log.e(TAG, "caught an exception", e);
		}
	}

	public void captured(File file) {
		if (DEBUG) Log.d(TAG, "captured file=" + file);
	}
}
