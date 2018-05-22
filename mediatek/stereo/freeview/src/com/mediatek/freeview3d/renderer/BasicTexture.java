/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.freeview3d.renderer;

import com.mediatek.util.Log;

/**
 * BasicTexture is a Texture corresponds to a real GL texture.
 * The state of a BasicTexture indicates whether its data is loaded to GL memory.
 * If a BasicTexture is loaded into GL memory, it has a GL texture id.
 */
public abstract class BasicTexture {
    private static final String TAG = Log.Tag("Fv/BasicTexture");

    protected static final int UNSPECIFIED = -1;
    protected static final int STATE_UNLOADED = 0;
    protected static final int STATE_LOADED = 1;
    protected static final int STATE_ERROR = -1;
    // Log a warning if a texture is larger along a dimension
    private static final int MAX_TEXTURE_SIZE = 4096;

    protected int mId = -1;
    protected int mState;
    protected int mTextureWidth;
    protected int mTextureHeight;
    protected int mWidth = UNSPECIFIED;
    protected int mHeight = UNSPECIFIED;
    protected GLES20Canvas mCanvasRef = null;

    protected BasicTexture(GLES20Canvas canvas, int id, int state) {
        setAssociatedCanvas(canvas);
        mId = id;
        mState = state;
    }

    public void prepare(GLES20Canvas canvas) {
    }

    protected BasicTexture() {
        this(null, 0, STATE_UNLOADED);
    }

    protected void setAssociatedCanvas(GLES20Canvas canvas) {
        mCanvasRef = canvas;
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getId() {
        return mId;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getTextureWidth() {
        return mWidth;
    }

    public int getTextureHeight() {
        return mHeight;
    }


    public void draw(GLES20Canvas canvas, int x, int y) {
        canvas.drawTexture(this, x, y, getWidth(), getHeight());
    }

    public void draw(GLES20Canvas canvas, int x, int y, int w, int h) {
        canvas.drawTexture(this, x, y, w, h);
    }

    // onBind is called before GLCanvas binds this texture.
    // It should make sure the data is uploaded to GL memory.
    abstract protected boolean onBind(GLES20Canvas canvas);

    // Returns the GL texture target for this texture (e.g. GL_TEXTURE_2D).
    protected abstract int getTarget();

    public boolean isLoaded() {
        return mState == STATE_LOADED;
    }

    // recycle() is called when the texture will never be used again,
    // so it can free all resources.
    public void recycle() {
        freeResource();
    }

    // yield() is called when the texture will not be used temporarily,
    // so it can free some resources.
    // The default implementation unloads the texture from GL memory, so
    // the subclass should make sure it can reload the texture to GL memory
    // later, or it will have to override this method.
    public void yield() {
        freeResource();
    }

    private void freeResource() {
        if (mCanvasRef != null && mId != -1) {
            mCanvasRef.unloadTexture(this);
            mId = -1; // Don't free it again.
            mCanvasRef.deleteRecycledResources();
        }
        mState = STATE_UNLOADED;
        setAssociatedCanvas(null);
    }

    @Override
    protected void finalize() {
        recycle();
    }
}
