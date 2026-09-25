package com.tsk.trofeomirror
import android.app.*;import android.content.*;import android.media.projection.MediaProjectionManager;import android.os.Bundle;import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity:AppCompatActivity(){
 private val REQ=77
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main)
  val ip=findViewById<EditText>(R.id.ip);ip.setText(getPreferences(0).getString("ip","172.19.95.220"))
  findViewById<Button>(R.id.start).setOnClickListener{
   getPreferences(0).edit().putString("ip",ip.text.toString()).apply()
   val m=getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager;startActivityForResult(m.createScreenCaptureIntent(),REQ)}
  findViewById<Button>(R.id.stop).setOnClickListener{stopService(Intent(this,MirrorService::class.java))}
 }
 override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d)
  if(r==REQ&&c==RESULT_OK&&d!=null){val i=Intent(this,MirrorService::class.java);i.putExtra("code",c);i.putExtra("data",d);i.putExtra("ip",findViewById<EditText>(R.id.ip).text.toString());startForegroundService(i);Toast.makeText(this,"เริ่ม Mirror แล้ว เปิดแอปเพลง/วิดีโอได้เลย",Toast.LENGTH_LONG).show()}}
}