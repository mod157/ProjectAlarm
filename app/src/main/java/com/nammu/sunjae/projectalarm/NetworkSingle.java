package com.nammu.sunjae.projectalarm;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by SunJae on 2017-01-21.
 */

public class NetworkSingle {
    private static NetworkSingle networkSingle;
    private RequestQueue req ;
    private ImageLoader imageLoader;
    private LruCache<String, Bitmap> cache = new LruCache<>(20);

    NetworkSingle(){}
    private NetworkSingle(Context context){
        req = Volley.newRequestQueue(context);
        imageLoader = new ImageLoader(req, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });

    }

    public static NetworkSingle getInstace(Context context){
        if(networkSingle == null)
            networkSingle = new NetworkSingle(context);

        return networkSingle;
    }

    public RequestQueue getReq(){
        return req;
    }

    public ImageLoader getImageLoader(){
        return imageLoader;
    }
}
