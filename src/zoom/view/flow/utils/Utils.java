package zoom.view.flow.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

public class Utils {

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
			is.close();
			os.close();
			is = null;
			os = null;
		} catch (Exception ex) {
		}
	}

	public static Bitmap decodeFile(File f, int max_size, int width) {
		Bitmap b = null;
		Config quality = Config.ARGB_8888;
		int IMAGE_MAX_SIZE = 1024;
		if (max_size > 0)
			IMAGE_MAX_SIZE = max_size;
		try {
			if (width < 1200) {
				IMAGE_MAX_SIZE = 512;
				quality = Config.ARGB_4444;
			}
			if (android.os.Build.VERSION.SDK_INT < 9) {
				IMAGE_MAX_SIZE = 256;
				quality = Config.ARGB_4444;
			}

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			FileInputStream fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			fis.close();

			int scale = 1;
			if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
				scale = (int) Math.pow(
						2,
						(int) Math.round(Math.log(IMAGE_MAX_SIZE
								/ (double) Math.max(o.outHeight, o.outWidth))
								/ Math.log(0.5)));
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inPreferredConfig = quality;
			o2.inDither = false;
			o2.inPurgeable = true;
			o2.inInputShareable = true;
			o2.inTempStorage = new byte[32 * 1024];
			o2.inSampleSize = scale;
			fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, o2);
			fis.close();
		} catch (IOException e) {
		}

		return b;
	}

}