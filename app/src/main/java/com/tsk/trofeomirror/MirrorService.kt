package com.tsk.trofeomirror
import android.app.*;import android.content.*;import android.graphics.*;import android.hardware.display.DisplayManager;import android.media.*;import android.media.projection.*;import android.os.*;import androidx.core.app.NotificationCompat
import okhttp3.*;import java.io.ByteArrayOutputStream;import java.util.concurrent.Executors
class MirrorService:Service(){
 private var mp:MediaProjection?=null;private var reader:ImageReader?=null;private var vd:android.hardware.display.VirtualDisplay?=null
 private val http=OkHttpClient.Builder().callTimeout(java.time.Duration.ofMillis(900)).build();private val exec=Executors.newSingleThreadExecutor();private var ip=""
 override fun onBind(i:Intent?)=null
 override fun onStartCommand(i:Intent?,f:Int,id:Int):Int{
  ip=i?.getStringExtra("ip")?:"172.19.95.220"; val ch="trofeo";(getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(NotificationChannel(ch,"TROFEO Mirror",NotificationManager.IMPORTANCE_LOW))
  startForeground(1,NotificationCompat.Builder(this,ch).setContentTitle("TROFEO Mirror").setContentText("กำลังส่งภาพไป TROFEO").setSmallIcon(android.R.drawable.ic_media_play).build())
  val code=i!!.getIntExtra("code",Activity.RESULT_CANCELED);val data=if(Build.VERSION.SDK_INT>=33)i.getParcelableExtra("data",Intent::class.java) else @Suppress("DEPRECATION") i.getParcelableExtra("data")
  val mgr=getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager;mp=mgr.getMediaProjection(code,data!!)
  reader=ImageReader.newInstance(1280,480,PixelFormat.RGBA_8888,2)
  vd=mp!!.createVirtualDisplay("Trofeo",1280,480,240,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader!!.surface,null,null)
  reader!!.setOnImageAvailableListener({r->val image=r.acquireLatestImage()?:return@setOnImageAvailableListener;try{
   val p=image.planes[0];val buf=p.buffer;val ps=p.pixelStride;val rs=p.rowStride;val pad=rs-ps*image.width
   val raw=Bitmap.createBitmap(image.width+pad/ps,image.height,Bitmap.Config.ARGB_8888);raw.copyPixelsFromBuffer(buf)
   val bm=Bitmap.createBitmap(raw,0,0,image.width,image.height);raw.recycle()
   exec.execute{val out=ByteArrayOutputStream();bm.compress(Bitmap.CompressFormat.JPEG,40,out);bm.recycle()
    val req=Request.Builder().url("http://$ip:8765/frame").post(out.toByteArray().toRequestBody("image/jpeg".toMediaType())).build()
    try{http.newCall(req).execute().close()}catch(_:Exception){}}
  }finally{image.close()}},Handler(Looper.getMainLooper()))
  return START_NOT_STICKY
 }
 override fun onDestroy(){reader?.close();vd?.release();mp?.stop();exec.shutdownNow();super.onDestroy()}
}