package com.example.sibhali.facedet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Sibhali on 12/19/2016.
 */
public class FlushedInputStream extends FilterInputStream {
    protected FlushedInputStream(InputStream in) {
        super(in);
    }

    @Override
    public long skip(long n) throws IOException {
        long totalBytesSkipped = 0L;
        while(totalBytesSkipped<n){
            long bytesSkipped = in.skip(n - totalBytesSkipped);
            if (bytesSkipped == 0L){
                int b = read();
                if (b<0) {
                    break;
                }else {
                    bytesSkipped = 1;
                }
            }
            totalBytesSkipped += bytesSkipped;
        }
        return totalBytesSkipped;
    }
}
