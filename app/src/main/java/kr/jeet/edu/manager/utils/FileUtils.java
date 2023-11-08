package kr.jeet.edu.manager.utils;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.AttachFileData;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.view.DrawableAlwaysCrossFadeFactory;

public class FileUtils {


    public static AttachFileData copyTempFile(Context context, AttachFileData attachFileData) {
        File destParentPath = new File(context.getExternalFilesDir(null).getPath());
        if(!destParentPath.exists()) {
            destParentPath.mkdir();
        }
//        String destFilePath = destParentPath + File.pathSeparator + attachFileData.fileName;
        String destFilePath = destParentPath + "/" +  attachFileData.fileName;

        File destFile = new File(destFilePath);
        InputStream is = null;
        OutputStream os = null;
        try{
            is = context.getContentResolver().openInputStream(attachFileData.uri);
            if(is != null) {
                os = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0){
                    os.write(buf, 0, len);
                }
                attachFileData.tempFilePath = destFilePath;
            }
        }catch(IOException ex) {
            ex.printStackTrace();
        }finally{
            try {
                if(is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return attachFileData;
    }
    public static boolean isExistBoardTempFile(Context context, FileData fileData) {
        String type = fileData.path.replaceAll("/", "");
        File file = new File(context.getExternalFilesDir(type).getPath() + "/" + fileData.tempFileName);
        return file.exists();
    }
    public static FileData copyBoardTempFile(Context context, Uri originalUri, FileData fileData) {
        if(fileData == null) return null;
        String type = fileData.path.replaceAll("/", "");
        File destParentPath = new File(context.getExternalFilesDir(type).getPath());
        if(!destParentPath.exists()) {
            destParentPath.mkdir();
        }
//        String destFilePath = destParentPath + File.pathSeparator + attachFileData.fileName;
        String destFilePath = destParentPath + "/" +  fileData.tempFileName;

        File destFile = new File(destFilePath);
        InputStream is = null;
        OutputStream os = null;
        try{
            is = context.getContentResolver().openInputStream(originalUri);
            if(is != null) {
                os = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0){
                    os.write(buf, 0, len);
                }
//                fileData.tempFileName = destFilePath;
            }
        }catch(IOException ex) {
            ex.printStackTrace();
        }finally{
            try {
                if(is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileData;
    }
    public static AttachFileData initAttachFileFromServer(Context context, FileData data) {
        AttachFileData attachFileData = new AttachFileData();
        String uriString = RetrofitApi.FILE_SUFFIX_URL + data.path + "/" + data.saveName;
        attachFileData.uri = Uri.parse(uriString);
        attachFileData.fileName = data.orgName;
        attachFileData.mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
        attachFileData.seq = data.seq;
        return attachFileData;
    }
    public static AttachFileData initAttachFileDataInstance(Context context, Uri uri) {
        if(uri == null) return null;
        Cursor cursor = null;
        String fileName = "";
        String originalFilePath = "";
        String mimeType = "";
        long fileSize = 0L;
        try{
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (columnIndex != -1) {
                    originalFilePath = cursor.getString(columnIndex);
                }
                mimeType = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FILE_MIME_TYPE));
                fileName = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FILE_DISPLAY_NAME));
                fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.FILE_SIZE));
                LogMgr.e("in cursor fileName = " + fileName);
                return new AttachFileData(uri, mimeType, originalFilePath, fileName, fileSize);
            }
        }catch(Exception ex){

        }finally{
            if(cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
    public static File resizeImageFile(File destinationParent, String originFilePath, int desireWidth, int desireHeight) {
        File originFile = new File(originFilePath);
        int originalWidth = 0;
        int originalHeight = 0;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originFilePath, options);
        originalWidth = options.outWidth;
        originalHeight = options.outHeight;
        float scaleFactor = calculateScaleFactor(originalWidth, originalHeight, desireWidth, desireHeight);
        options.inJustDecodeBounds = false;
        LogMgr.w("samplingSize = " + scaleFactor);
        options.inSampleSize = (int) scaleFactor;

        if(!isRequireResize(originalWidth, originalHeight, desireWidth, desireHeight)) {
            return originFile;
        }else{
            Bitmap samplingBitmap = BitmapFactory.decodeFile(originFilePath, options);
            Bitmap resizedBitmap = getScaledBitmap(samplingBitmap, desireWidth, desireHeight);
            if(resizedBitmap == null) return originFile;
            resizedBitmap = rotateImageIfRequired(originFile, resizedBitmap);
            File resizedFile = new File(destinationParent.getAbsolutePath(), originFile.getName());
            if(resizedFile.exists()) {
                resizedFile.delete();
            }
            FileOutputStream outputStream = null;
            try{
                resizedFile.createNewFile();
                outputStream = new FileOutputStream(resizedFile);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
            }catch(IOException ex) {
                ex.printStackTrace();
            }finally{
                if(outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return resizedFile;
        }
    }

    public static boolean isTargetResize(AttachFileData originAttachFile) {
        if(!originAttachFile.mimeType.startsWith("image")) return false;
        if("heic".equalsIgnoreCase(originAttachFile.fileExt)) return false;
        return true;
    }
    private static boolean isRequireResize(int originalWidth, int originalHeight, int desireWidth, int desireHeight) {
        return calculateScaleFactor(originalWidth, originalHeight, desireWidth, desireHeight) > 1.0f;
    }
    private static float calculateScaleFactor(int originalWidth, int originalHeight, int desireWidth, int desireHeight) {
        LogMgr.w("origin " + originalWidth + " / " + originalHeight);
        LogMgr.w("destination " + desireWidth + " / " + desireHeight);
        float scaleFactor = 1.0f;
        if (originalWidth > desireWidth || originalHeight > desireHeight) {
            float widthScaleFactor = (float) originalWidth / desireWidth;
            float heightScaleFactor = (float) originalHeight / desireHeight;
            scaleFactor = Math.min(widthScaleFactor, heightScaleFactor);
        }
        LogMgr.w("scaleFactor = " + scaleFactor);
        return scaleFactor;
    }
    private static Bitmap getScaledBitmap(Bitmap bitmap, int desireWidth, int desireHeight) {
        if(bitmap == null) {
            return null;
        }
        int w = 0;
        int h = 0;

        if(desireHeight > 0) {
            if (bitmap.getWidth() >= bitmap.getHeight()) {
                if (bitmap.getWidth() > desireHeight) {
                    w = desireHeight;
                    h = (w * bitmap.getHeight()) / bitmap.getWidth();
                } else {
                    w = bitmap.getWidth();
                    h = bitmap.getHeight();
                }
            } else {
                if (bitmap.getHeight() > desireHeight) {
                    h = desireHeight;
                    w = (h * bitmap.getWidth()) / bitmap.getHeight();
                } else {
                    w = bitmap.getWidth();
                    h = bitmap.getHeight();
                }
            }
        } else {
            if (bitmap.getWidth() > desireWidth) {
                w = desireWidth;
                h = (desireWidth * bitmap.getHeight()) / bitmap.getWidth();
            } else {
                w = bitmap.getWidth();
                h = bitmap.getHeight();
            }
        }
        Bitmap scaledBitmap = null;
        try{
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
            if(bitmap != scaledBitmap) {
                bitmap.recycle();
            }
        }catch(OutOfMemoryError e) {
            e.printStackTrace();
        }
        return scaledBitmap;
    }
    private static Bitmap rotateImageIfRequired(File file, Bitmap bitmap) {
        try{
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    private static Bitmap rotateImage(Bitmap bitmap, int degree) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }finally{
            bitmap.recycle();
        }
    }

    public static String getFilePathFromUri(Context context, Uri uri) {
        String result = null;
        if(uri == null) return null;
        String scheme = uri.getScheme();
        LogMgr.w("scheme = " + scheme);
        if(scheme != null) {
            if(scheme == ContentResolver.SCHEME_CONTENT) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        if (columnIndex != -1) {
                            result = cursor.getString(columnIndex);
                        }
                    }
                }catch(IllegalArgumentException e) {

                }finally{
                    if(cursor != null) cursor.close();
                }
                LogMgr.w("result = " + result);
                if(TextUtils.isEmpty(result)) {
                    result = uri.getLastPathSegment();
                }
                LogMgr.w("result2 = " + result);
            }else if(scheme == ContentResolver.SCHEME_FILE) {
                result = uri.getPath();
                LogMgr.w("result = " + result);
                if(TextUtils.isEmpty(result)) {
                    result = uri.getLastPathSegment();
                }
                LogMgr.w("result2 = " + result);
            }
        }
        LogMgr.w("return result = " + result);
        return result;
    }
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                }
            }catch(IllegalArgumentException e){
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
    public static String getFileName(String filePath) {
        return filePath != null ? filePath.substring(filePath.lastIndexOf("/") + 1) : "";
    }

    public static String replaceMultipleSlashes(String input) {
        String regex = "/+";
        Matcher matcher = Pattern.compile(regex).matcher(input);
        String result = matcher.replaceAll("/");
        return result;
    }
    public static String getMimeTypeFromExtension(String extension) {
        try {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if(mimeType == null) mimeType = "";
            return mimeType;
        }catch(Exception ex) {
            return "";
        }
    }
    public static void downloadFile(Context context, String fileUrl, String fileName) {
        Uri uri = Uri.parse(fileUrl);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(fileName);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

    }

    public static void isImageUrlValid(Context context, String imageUrl, ImageValidationListener listener) {
        RequestOptions requestOptions = new RequestOptions()
                .onlyRetrieveFromCache(true)
                .dontAnimate()
                .skipMemoryCache(true);

        Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        // 이미지 로딩에 실패한 경우
                        listener.onImageValidation(false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        // 이미지 로딩에 성공한 경우
                        listener.onImageValidation(true);
                        return false;
                    }
                })
                .submit();
    }

    public interface ImageValidationListener {
        void onImageValidation(boolean isValid);
    }

    public static boolean setImageViewEnabledWithColor(Context context, ImageView imageView, boolean flag, int enableColorRes, int disableColorRes) {
        if(imageView != null) {
            imageView.setEnabled(flag);
            if (flag) {
                // Set the color filter for the enabled state
                imageView.setColorFilter(context.getColor(enableColorRes));
            } else {
                // Set the color filter for the disabled state
                imageView.setColorFilter(context.getColor(disableColorRes));
            }
            return true;
        }else{
            return false;
        }
    }
    public static void loadImage(Context mContext, String url, ImageView view){
        Glide.with(mContext)
                .load(url)
                .thumbnail(0.2f)
                .centerCrop()
//                .error(R.drawable.ic_vector_image_error)
                .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                .into(view);
    }

//    public static void downloadFile(Context context, String fileUrl, String destinationPath, final onDownloadListener callback) {
//        if(RetrofitClient.getInstance() != null) {
//            RetrofitApi retrofitApi = RetrofitClient.getApiInterface();
//            retrofitApi.downloadFile(fileUrl).enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    if(response.isSuccessful()) {
//                        boolean writtenToDisk = writeResponseBodyToDisk(response.body(), destinationPath);
//                        if(writtenToDisk) {
//                            callback.onDownloadSuccess();
//                        }else{
//                            callback.onDownloadFail(context.getString(R.string.error_msg_fail_to_save));
//                        }
//                    }else{
//                        callback.onDownloadFail(context.getString(R.string.error_msg_fail_to_download));
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    callback.onDownloadFail(context.getString(R.string.server_error));
//                }
//            });
//        }
//    }
//    private static boolean writeResponseBodyToDisk(ResponseBody body, String destinationPath) {
//        try {
//            File file = new File(destinationPath);
//            if(!file.exists()) {
//                file.createNewFile();
//            }
//            InputStream inputStream = null;
//            OutputStream outputStream = null;
//
//            try {
//                byte[] fileReader = new byte[4096];
//                long fileSize = body.contentLength();
//                long fileSizeDownloaded = 0;
//
//                inputStream = body.byteStream();
//                outputStream = new FileOutputStream(file);
//
//                while (true) {
//                    int read = inputStream.read(fileReader);
//                    if (read == -1) {
//                        break;
//                    }
//
//                    outputStream.write(fileReader, 0, read);
//                    fileSizeDownloaded += read;
//
//                }
//
//                outputStream.flush();
//                return true;
//            } catch (IOException e) {
//                return false;
//            } finally {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//            }
//        } catch (IOException e) {
//            return false;
//        }
//    }
//    public interface onDownloadListener {
//        void onDownloadSuccess();
//        void onDownloadFail(String error);
//    }


}
