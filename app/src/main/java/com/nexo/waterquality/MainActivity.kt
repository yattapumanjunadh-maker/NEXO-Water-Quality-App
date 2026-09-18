package com.nexo.waterquality

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
 private lateinit var content:FrameLayout
 private lateinit var nav:BottomNavigationView
 private var bitmap:Bitmap?=null
 private var use="Drinking"
 private var parameter="pH"
 private var value="—"
 private var total=1
 private val results=linkedMapOf<String,String>()
 private data class Ref(val label:String,val rgb:Int)
 private val palettes=mapOf(
  "pH" to listOf(Ref("1",Color.rgb(245,35,39)),Ref("2",Color.rgb(247,82,22)),Ref("3",Color.rgb(251,126,16)),Ref("4",Color.rgb(252,177,17)),Ref("5",Color.rgb(153,191,27)),Ref("6",Color.rgb(77,166,61)),Ref("7",Color.rgb(4,119,104)),Ref("8",Color.rgb(71,46,159)),Ref("9",Color.rgb(92,22,151)),Ref("10",Color.rgb(68,10,122)),Ref("11",Color.rgb(43,19,113)),Ref("12",Color.rgb(35,20,111)),Ref("13",Color.rgb(30,19,105)),Ref("14",Color.rgb(14,45,153))),
  "Alkalinity" to listOf(Ref("0",Color.rgb(254,113,2)),Ref("40",Color.rgb(253,208,3)),Ref("80",Color.rgb(173,170,15)),Ref("120",Color.rgb(119,149,18)),Ref("180",Color.rgb(2,119,32)),Ref("250",Color.rgb(2,112,56)),Ref("720",Color.rgb(3,57,169))),
  "Water Hardness" to listOf(Ref("0",Color.rgb(79,149,245)),Ref("50",Color.rgb(61,104,234)),Ref("120",Color.rgb(144,56,190)),Ref("180",Color.rgb(184,29,160)),Ref("250",Color.rgb(202,1,139)),Ref("425",Color.rgb(236,2,123))),
  "Iron" to listOf(Ref("0",Color.rgb(254,240,175)),Ref("0.1",Color.rgb(254,227,180)),Ref("0.3",Color.rgb(251,209,185)),Ref("1.0",Color.rgb(253,181,182)),Ref("5.0",Color.rgb(249,142,170))),
  "Free Chlorine" to listOf(Ref("0",Color.rgb(254,248,2)),Ref("0.1",Color.rgb(232,240,4)),Ref("0.2",Color.rgb(211,232,3)),Ref("0.5",Color.rgb(178,220,24)),Ref("0.8",Color.rgb(126,212,31)),Ref("4.0",Color.rgb(69,186,17))),
  "Nitrate" to listOf(Ref("0",Color.rgb(239,240,244)),Ref("0.5",Color.rgb(254,230,243)),Ref("1",Color.rgb(250,209,227)),Ref("5",Color.rgb(253,176,204)),Ref("10",Color.rgb(253,144,186))),
  "Nitrite" to listOf(Ref("0",Color.rgb(246,235,241)),Ref("0.5",Color.rgb(254,214,231)),Ref("1",Color.rgb(250,190,216)),Ref("5",Color.rgb(253,146,188)),Ref("10",Color.rgb(253,100,166)))
 )
 private val uses=arrayOf("Drinking","Bathing / Recreation","Household","Agriculture / Irrigation","Community / Multiple Uses")
 private val params=palettes.keys.toTypedArray()
 private val camera=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){r->if(r.resultCode==Activity.RESULT_OK){bitmap=r.data?.extras?.get("data") as? Bitmap;showScan(true)}}
 private val camPermission=registerForActivityResult(ActivityResultContracts.RequestPermission()){if(it)openCamera()}
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main);content=findViewById(R.id.content);nav=findViewById(R.id.bottomNav);nav.setOnItemSelectedListener{when(it.itemId){R.id.nav_home->showHome();R.id.nav_scan->showScan(false);R.id.nav_history->showHistory();R.id.nav_map->showMap()};true};showHome()}
 private fun inflate(id:Int):View{content.removeAllViews();return layoutInflater.inflate(id,content,true)}
 private fun records():JSONArray=try{JSONArray(getSharedPreferences("nexo",0).getString("records","[]")?:"[]")}catch(_:Exception){JSONArray()}
 private fun saveRecords(a:JSONArray){getSharedPreferences("nexo",0).edit().putString("records",a.toString()).apply()}
 private fun showHome(){val v=inflate(R.layout.screen_home);val a=records();v.findViewById<TextView>(R.id.sampleCount).text="SAMPLES\n"+a.length();if(a.length()>0)v.findViewById<TextView>(R.id.lastResult).text="LAST RESULT\n"+a.getJSONObject(a.length()-1).optString("result","—");v.findViewById<MaterialButton>(R.id.startTestButton).setOnClickListener{showScan(false)};v.findViewById<MaterialButton>(R.id.languageButton).setOnClickListener{chooseLanguage()}}
 private fun chooseLanguage(){AlertDialog.Builder(this).setTitle("Select Language").setItems(arrayOf("English","தமிழ்","తెలుగు","हिन्दी")){_,i->getSharedPreferences("nexo",0).edit().putString("language",arrayOf("en","ta","te","hi")[i]).apply();showHome()}}.show()}
 private fun showScan(keep:Boolean){val v=inflate(R.layout.screen_scan);val us=v.findViewById<Spinner>(R.id.useSpinner);val ps=v.findViewById<Spinner>(R.id.parameterSpinner);val cs=v.findViewById<Spinner>(R.id.testCountSpinner);us.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,uses);ps.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,params);cs.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("1 test","2 tests","3 tests","4 tests","5 tests"));us.setSelection(uses.indexOf(use).coerceAtLeast(0));ps.setSelection(params.indexOf(parameter).coerceAtLeast(0));cs.setSelection(total-1);val ref=v.findViewById<LinearLayout>(R.id.referenceContainer)
  fun refresh(){use=uses[us.selectedItemPosition];parameter=params[ps.selectedItemPosition];total=cs.selectedItemPosition+1;ref.removeAllViews();populate(ref,parameter);v.findViewById<TextView>(R.id.criterionText).text="Reference levels: "+palettes[parameter]!!.joinToString(", "){it.label}+unit(parameter);v.findViewById<TextView>(R.id.instructions).text=instructions(parameter);v.findViewById<TextView>(R.id.testProgress).text="TEST "+(results.size+1)+" OF "+total+"\nCapture each test one-by-one.";if(keep&&bitmap!=null){v.findViewById<ImageView>(R.id.preview).setImageBitmap(bitmap);val x=analyse(bitmap!!,parameter);value=x.first;v.findViewById<TextView>(R.id.colourReadout).text="Reference match: "+value+"\nComputer vision confidence: "+x.second+"%";v.findViewById<MaterialButton>(R.id.resultButton).isEnabled=x.second>=35}}
  us.onItemSelectedListener=listener{refresh()};ps.onItemSelectedListener=listener{refresh()};cs.onItemSelectedListener=listener{refresh()};v.findViewById<MaterialButton>(R.id.allReferenceButton).setOnClickListener{showAllReferenceCharts()};v.findViewById<MaterialButton>(R.id.cameraButton).setOnClickListener{requestCamera()};v.findViewById<MaterialButton>(R.id.resultButton).setOnClickListener{results[parameter]=value;if(results.size>=total)showResult()else{bitmap=null;showScan(false)}};refresh()}
 private fun listener(a:()->Unit)=object:AdapterView.OnItemSelectedListener{override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){a()};override fun onNothingSelected(p:AdapterView<*>?){}} 
 private fun populate(c:LinearLayout,p:String){palettes[p]!!.forEach{r->val box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.gravity=1;val sw=TextView(this);sw.layoutParams=LinearLayout.LayoutParams(52,52).apply{setMargins(4,4,4,0)};sw.setBackgroundColor(r.rgb);val t=TextView(this);t.text=r.label;t.gravity=17;t.textSize=13f;box.addView(sw);box.addView(t);c.addView(box)}}
 private fun showAllReferenceCharts(){val l=LinearLayout(this);l.orientation=LinearLayout.VERTICAL;l.setPadding(20,20,20,20);val s=ScrollView(this);s.addView(l);val title=TextView(this);title.text="NEXO WATER QUALITY\n7-PARAMETER COLOR REFERENCE";title.textSize=24f;title.setTextColor(Color.rgb(14,79,128));l.addView(title);params.forEach{p->val h=TextView(this);h.text=p+unit(p);h.textSize=19f;h.setPadding(0,16,0,4);l.addView(h);val row=HorizontalScrollView(this);val c=LinearLayout(this);c.orientation=LinearLayout.HORIZONTAL;populate(c,p);row.addView(c);l.addView(row)};AlertDialog.Builder(this).setView(s).setPositiveButton("Close",null).show()}
 private fun unit(p:String)=if(p=="pH")" (pH)" else " (mg/L or ppm)"
 private fun instructions(p:String)=if(p=="pH")"Use the pH strip according to its manufacturer instructions. Dip, wait for the specified reaction time, keep flat and scan the reacted colour area." else "Prepare the selected test according to the manufacturer's instructions. Scan after the specified reaction time and compare with the matching reference chart."
 private fun requestCamera(){if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)openCamera()else camPermission.launch(Manifest.permission.CAMERA)}
 private fun openCamera(){camera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))}
 private fun analyse(b:Bitmap,p:String):Pair<String,Int>{var sr=0;var sg=0;var sb=0;var n=0;val step=(b.width.coerceAtMost(b.height)/80).coerceAtLeast(2);for(y in b.height/4 until b.height*3/4 step step)for(x in b.width/4 until b.width*3/4 step step){val c=b.getPixel(x,y);sr+=Color.red(c);sg+=Color.green(c);sb+=Color.blue(c);n++};if(n==0)return "—" to 0;val r=sr/n;val g=sg/n;val bl=sb/n;var best=Double.MAX_VALUE;var label="—";for(q in palettes[p]!!){val d=sqrt(((r-Color.red(q.rgb))*(r-Color.red(q.rgb))+(g-Color.green(q.rgb))*(g-Color.green(q.rgb))+(bl-Color.blue(q.rgb))*(bl-Color.blue(q.rgb))).toDouble());if(d<best){best=d;label=q.label}};return label to (((1.0-best/442.0).coerceIn(0.0,1.0))*100).toInt()}
 private fun showResult(){val v=inflate(R.layout.screen_result);v.findViewById<TextView>(R.id.resultValue).text="TESTS COMPLETED\n"+results.entries.joinToString("\n"){it.key+" : "+it.value};v.findViewById<TextView>(R.id.status).text="SCREENING STATUS\nResults recorded for the selected tests";v.findViewById<TextView>(R.id.details).text="Water use: "+use+"\nDate & time: "+now()+"\nSample ID: NEXO-"+(records().length()+1);v.findViewById<TextView>(R.id.interpretation).text="INTERPRETATION\nColour values are screening references from the supplied chart. Colours vary by brand, lighting and camera. Confirm concerning results with an appropriate validated test or laboratory.";v.findViewById<MaterialButton>(R.id.saveButton).setOnClickListener{saveAssessment()};v.findViewById<MaterialButton>(R.id.reportButton).setOnClickListener{shareReport()}}
 private fun saveAssessment(){val a=records();val o=JSONObject();o.put("id","NEXO-"+(a.length()+1));o.put("use",use);o.put("result",results.entries.joinToString(", "){it.key+"="+it.value});o.put("time",now());a.put(o);saveRecords(a);Toast.makeText(this,"Sample saved locally.",Toast.LENGTH_LONG).show();results.clear();showHome()}
 private fun shareReport(){startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,"NEXO WATER QUALITY — FIELD SCREENING\nUse: "+use+"\nTests: "+results.entries.joinToString(", "){it.key+" = "+it.value}+"\nTime: "+now()+"\nScreening only; confirm concerning results with a validated test or laboratory.")},"Share NEXO report"))}
 private fun now()=SimpleDateFormat("dd-MM-yyyy HH:mm",Locale.getDefault()).format(Date())
 private fun showHistory(){val v=inflate(R.layout.screen_history);val a=records();v.findViewById<TextView>(R.id.historyCount).text=a.length().toString()+" locally saved sample(s)";val no=v.findViewById<TextView>(R.id.noHistory);val c=v.findViewById<LinearLayout>(R.id.historyContainer);c.removeAllViews();no.visibility=if(a.length()==0)View.VISIBLE else View.GONE;for(i in 0 until a.length()){val o=a.getJSONObject(i);val t=TextView(this);t.text=o.optString("id")+"\n"+o.optString("result")+"\n"+o.optString("use")+" • "+o.optString("time");t.textSize=16f;t.setPadding(18,18,18,18);t.setBackgroundResource(R.drawable.card_bg);c.addView(t)};v.findViewById<MaterialButton>(R.id.clearHistoryButton).setOnClickListener{AlertDialog.Builder(this).setTitle("Clear sample history?").setNegativeButton("Cancel",null).setPositiveButton("Clear"){_,_->saveRecords(JSONArray());showHistory()}.show()}}
 private fun showMap(){val v=inflate(R.layout.screen_map);val w=v.findViewById<WebView>(R.id.mapWebView);w.settings.javaScriptEnabled=true;w.webViewClient=WebViewClient();w.loadDataWithBaseURL("https://unpkg.com/","<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/><script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script></head><body style='margin:0'><div id='map' style='height:410px'></div><script>var map=L.map('map').setView([8.18,77.43],12);L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);</script></body></html>","text/html","UTF-8",null);v.findViewById<TextView>(R.id.mapSummary).text=records().length().toString()+" saved screening sample(s). GPS mapping is shown when coordinates are available."}
}