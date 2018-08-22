package com.github.wire;

public interface Middleware {
    void run(Context ctx, Next next);
}
