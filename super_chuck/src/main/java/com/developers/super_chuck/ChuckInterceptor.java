package com.developers.super_chuck;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.developers.super_chuck.internal.data.ChuckContentProvider;
import com.developers.super_chuck.internal.data.HttpTransaction;
import com.developers.super_chuck.internal.data.LocalCupboard;
import com.developers.super_chuck.internal.helper.NotificationHelper;
import com.developers.super_chuck.internal.helper.RetentionManager;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;

/**
 * @Author yinzh
 * @Date 2019/3/25 14:22
 * @Description
 */
public class ChuckInterceptor implements Interceptor {

    public enum Period {
        /**
         * Retain data for the last hour.
         */
        ONE_HOUR,
        /**
         * Retain data for the last day.
         */
        ONE_DAY,
        /**
         * Retain data for the last week.
         */
        ONE_WEEK,
        /**
         * Retain data forever.
         */
        FOREVER
    }

    private static final String LOG_TAG = "ChuckInterceptor";
    private static final Period DEFAULT_RETENTION = Period.ONE_WEEK;
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final Context context;
    private final NotificationHelper notificationHelper;
    private RetentionManager retentionManager;
    private boolean showNotification;
    private long maxContentLength = 250000L;

    /**
     * @param context The current Context.
     */
    public ChuckInterceptor(Context context) {
        this.context = context.getApplicationContext();
        notificationHelper = new NotificationHelper(this.context);
        showNotification = true;
        retentionManager = new RetentionManager(this.context, DEFAULT_RETENTION);
    }

    public ChuckInterceptor showNotification(boolean show) {
        showNotification = show;
        return this;
    }

    public ChuckInterceptor maxContentLength(long max) {
        this.maxContentLength = max;
        return this;
    }

    public ChuckInterceptor retainDataFor(Period period) {
        retentionManager = new RetentionManager(context, period);
        return this;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        HttpTransaction httpTransaction = new HttpTransaction();
        httpTransaction.setRequestDate(new Date());
        httpTransaction.setMethod(request.method());
        httpTransaction.setUrl(request.url().toString());
        httpTransaction.setRequestHeaders(request.headers());

        if (hasRequestBody) {
            if (requestBody.contentType() != null) {
                httpTransaction.setRequestContentType(requestBody.contentType().toString());
            }
            if (requestBody.contentLength() != -1) {
                httpTransaction.setRequestContentLength(requestBody.contentLength());
            }
        }

        httpTransaction.setRequestBodyIsPlainText(!bodyHasUnsupportedEncoding(request.headers()));
        if (hasRequestBody && httpTransaction.requestBodyIsPlainText()) {
            BufferedSource source = getNativeSource(new Buffer(), bodyGzipped(request.headers()));
            Buffer buffer = source.buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            if (isPlainText(buffer)) {
                httpTransaction.setRequestBody(readFromBuffer(buffer, charset));
            } else {
                httpTransaction.setResponseBodyIsPlainText(false);
            }
        }

        Uri uri = create(httpTransaction);
        long startTime = System.nanoTime();
        Response response;

        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            httpTransaction.setError(e.toString());
            update(httpTransaction, uri);
            throw e;
        }

        long  tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        ResponseBody responseBody = response.body();

        httpTransaction.setRequestHeaders(response.request().headers());// includes headers added later in the chain
        httpTransaction.setResponseDate(new Date());
        httpTransaction.setTookMs(tookMs);
        httpTransaction.setProtocol(response.protocol().toString());
        httpTransaction.setResponseCode(response.code());
        httpTransaction.setResponseMessage(response.message());

        httpTransaction.setRequestContentLength(requestBody.contentLength());

        if(requestBody.contentType() != null){
            httpTransaction.setResponseContentType(responseBody.contentType().toString());
        }
        httpTransaction.setResponseHeaders(response.headers());

        httpTransaction.setResponseBodyIsPlainText(!bodyHasUnsupportedEncoding(response.headers()));
        if (HttpHeaders.hasBody(response) && httpTransaction.responseBodyIsPlainText()) {
            BufferedSource source = getNativeSource(response);
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    update(httpTransaction, uri);
                    return response;
                }
            }
            if (isPlaintext(buffer)) {
                httpTransaction.setResponseBody(readFromBuffer(buffer.clone(), charset));
            } else {
                httpTransaction.setResponseBodyIsPlainText(false);
            }
            httpTransaction.setResponseContentLength(buffer.size());
        }

        update(httpTransaction, uri);

        return response;
    }


    private boolean bodyHasUnsupportedEncoding(Headers headers) {

        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null &&
                !contentEncoding.equalsIgnoreCase("identity") &&
                !contentEncoding.equalsIgnoreCase("gzip");
    }


    private BufferedSource getNativeSource(BufferedSource input, boolean isGziped) {
        if (isGziped) {
            GzipSource gzipSource = new GzipSource(input);
            return Okio.buffer(gzipSource);
        } else {
            return input;
        }
    }


    private boolean bodyGzipped(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return "gzip".equalsIgnoreCase(contentEncoding);
    }

    private boolean isPlainText(Buffer buffer) {

        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = 0;

                codePoint = prefix.readUtf8CodePoint();

                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            e.printStackTrace();
        }
        return false;
    }


    private String readFromBuffer(Buffer buffer, Charset charset) {

        long bufferSize = buffer.size();
        long maxBytes = Math.min(bufferSize, maxContentLength);
        String body = "";
        try {
            buffer.readString(maxBytes, charset);

        } catch (EOFException e) {
            body += context.getString(R.string.chuck_body_unexpected_eof);
        }

        if (bufferSize > maxContentLength) {
            body += context.getString(R.string.chuck_body_content_truncated);
        }
        return body;
    }


    private Uri create(HttpTransaction httpTransaction) {
        ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(httpTransaction);
        Uri uri = context.getContentResolver().insert(ChuckContentProvider.TRANSACTION_URI, values);
        httpTransaction.setId(Long.valueOf(uri.getLastPathSegment()));
        if (showNotification) {
            notificationHelper.show(httpTransaction);
        }
        retentionManager.doMaintenance();
        return uri;
    }

    private int update(HttpTransaction transaction, Uri uri) {
        ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(transaction);
        int update = context.getContentResolver().update(uri, values, null, null);
        if (showNotification && update > 0) {
            notificationHelper.show(transaction);
        }
        return update;
    }


    private boolean isPlaintext(Buffer buffer){
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }


    private BufferedSource getNativeSource(Response response) throws IOException {
        if (bodyGzipped(response.headers())) {
            BufferedSource source = response.peekBody(maxContentLength).source();
            if (source.buffer().size() < maxContentLength) {
                return getNativeSource(source, true);
            } else {
                Log.w(LOG_TAG, "gzip encoded response was too long");
            }
        }
        return response.body().source();
    }


}
