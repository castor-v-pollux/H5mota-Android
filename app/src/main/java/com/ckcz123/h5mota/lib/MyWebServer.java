package com.ckcz123.h5mota.lib;

import android.util.Log;

import com.ckcz123.h5mota.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.SimpleWebServer;

/**
 * Created by oc on 2018/4/24.
 */

public class MyWebServer extends SimpleWebServer{

    public MyWebServer(String host, int port, File wwwroot, boolean quiet) {
        super(host, port, wwwroot, quiet);
    }

    public boolean shouldRedirect(String path) {
        // 只有"存档同步"才会上传服务器
        return path.equals("/games/sync.php");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String path = session.getUri();
        if (session.getMethod() == Method.POST && shouldRedirect(path)) {

            try {
                session.parseBody(new HashMap<String, String>());
            }
            catch (ResponseException | IOException e) {
                Log.e("Parse Body", "error", e);
            }

            Map<String, List<String>> map = session.getParameters();

            Map<String, String> keyValue = new HashMap<>();
            for (Map.Entry<String, List<String>> entry: map.entrySet()) {
                keyValue.put(entry.getKey(), entry.getValue().get(0));
            }

            try {
                HttpRequest request = HttpRequest.post(MainActivity.DOMAIN+path)
                        .acceptJson()
                        .form(keyValue);

                int code = request.code();
                String body = request.body(), message = request.message();
                request.disconnect();

                if (code==200) {
                    return newFixedLengthResponse(Response.Status.OK, "application/json", body);
                }
                else {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", message);
                }
            }
            catch (Exception ignore) {}

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "");
        }
        else {
            return super.serve(session);
        }
    }

}
