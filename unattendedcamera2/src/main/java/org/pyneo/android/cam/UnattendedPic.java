package org.pyneo.android.cam;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class UnattendedPic {
	private static final String TAG = UnattendedPic.class.getName();
	private static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable("org.pyneo.android", Log.DEBUG); }

	private static class ImageSaver implements Runnable {
		private final Image mImage;
		private final File mFile;
		public ImageSaver(Image image, File file) {
			Log.d(TAG, "ImageSaver");
			mImage = image;
			mFile = file;
		}
		@Override
		public void run() {
			Log.d(TAG, "ImageSaver.run");
			try {
				ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
				byte[] bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				FileOutputStream output = new FileOutputStream(mFile);
				try {
					output.write(bytes);
					Log.d(TAG, "ImageSaver.run mFile=" + mFile);
				}
				finally {
					output.close();
				}
			}
			catch (Exception e) {
				Log.e(TAG, "caught an exception", e);
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
		@Override
		public void onImageAvailable(ImageReader reader) {
			Log.d(TAG, "onImageAvailable");
			mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
		}

	};
	private CameraDevice mCameraDevice;
	private Semaphore mCameraOpenCloseLock = new Semaphore(1);
	private CameraCaptureSession mCaptureSession;
	private CaptureRequest.Builder captureBuilder;
	private CameraCaptureSession.CaptureCallback captureCallback;
	private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(CameraDevice cameraDevice) {
			Log.d(TAG, "onOpened");
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
					@Override
					public void onConfigured(CameraCaptureSession cameraCaptureSession) {
						Log.d(TAG, "onConfigured");
						if (null == mCameraDevice) {
							return;
						}
						mCaptureSession = cameraCaptureSession;
						captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
						captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
						captureCallback = new CameraCaptureSession.CaptureCallback() {
							@Override
							public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
								Log.d(TAG, "onCaptureCompleted");
							}
						};
						Log.d(TAG, "onConfigured: capture!");
						try {
							mCaptureSession.capture(captureBuilder.build(), captureCallback, null);
						} catch (Exception e) {
							Log.e(TAG, "caught an exception", e);
						}
					}
					@Override
					public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
						Log.i(TAG, "onConfigureFailed");
					}
				}, null);
			} catch (Exception e) {
				Log.e(TAG, "caught an exception", e);
			}
		}
		@Override
		public void onDisconnected(CameraDevice cameraDevice) {
			Log.d(TAG, "onDisconnected");
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
		}
		@Override
		public void onError(CameraDevice cameraDevice, int error) {
			Log.d(TAG, "onError error=" + error);
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
		}
	};
	private File mFile;

	public void start(Activity activity) {
		if (mImageReader == null) {
			mBackgroundThread = new HandlerThread("CameraBackground");
			mBackgroundThread.start();
			mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
			mFile = new File("/sdcard", "pic.jpg");
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
						for (Object o: map.getOutputSizes(ImageFormat.JPEG)) {
							if (DEBUG) Log.d(TAG, "doTest o=" + o);
						}
						mImageReader = ImageReader.newInstance(800, 600, ImageFormat.JPEG, 2);
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
	}

	public void capture(Activity activity) {
		Log.d(TAG, "doTest: capture!");
		start(activity);
		try {
			mCaptureSession.capture(captureBuilder.build(), captureCallback, null);
		} catch (Exception e) {
			Log.e(TAG, "caught an exception", e);
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



}
