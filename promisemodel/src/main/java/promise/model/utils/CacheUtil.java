/*
 *
 *  * Copyright 2017, Peter Vincent
 *  * Licensed under the Apache License, Version 2.0, Promise.
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package promise.model.utils;

import android.content.Context;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;

import promise.commons.Promise;
import promise.commons.data.log.LogUtil;
import promise.commons.file.Dir;

public class CacheUtil {
  private String TAG = LogUtil.makeTag(CacheUtil.class);
  private String promise = LogUtil.makeTag(Promise.class);
  // The cache directory should look something like this
  private File cacheDirectory;

  private CacheUtil(Context context) {
    if (Dir.isWritable()) {
      cacheDirectory = context.getCacheDir();
      Dir.make(cacheDirectory.getAbsolutePath());
    }
  }

  public static CacheUtil instance() {
    return new CacheUtil(Promise.instance().context());
  }

  private static boolean tooOld(long time) {
    long now = new Date().getTime();
    long diff = now - time;
    return diff > 900000;
  }

  private String convertToCacheName(String url) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(url.getBytes());
      byte[] b = digest.digest();
      BigInteger bi = new BigInteger(b);
      return promise + bi.toString(16) + ".cache";
    } catch (Exception e) {
      LogUtil.d("ERROR", e.toString());
      return null;
    }
  }

  public byte[] read(String url) {
    try {
      String file = cacheDirectory + "/" + convertToCacheName(url);
      File f = new File(file);
      if (!f.exists() || f.length() < 1) return null;
      // Delete the cached file if it is too old
      if (f.exists() && tooOld(f.lastModified())) f.delete();
      byte data[] = new byte[(int) f.length()];
      DataInputStream fis = new DataInputStream(
          new FileInputStream(f));
      fis.readFully(data);
      fis.close();
      return data;
    } catch (Exception e) {
      return null;
    }
  }

  public void write(String url, String data) {
    try {
      String file = cacheDirectory + "/" + convertToCacheName(url);
      PrintWriter pw = new PrintWriter(new FileWriter(file));
      pw.print(data);
      pw.close();
    } catch (Exception e) {
      LogUtil.e(TAG, e);
    }
  }
}