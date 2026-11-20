package com.mk.titanium.launcher;

import android.app.Activity;
import android.app.role.RoleManager;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private boolean dark=true;
    private int bg, fg, muted, panel, accent, border;
    private LinearLayout root, home, drawer, apps;
    private TextView clock, date, battery, themeLabel;
    private ThemeSwitch themeSwitch;
    private EditText search;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ArrayList<ResolveInfo> installed=new ArrayList<>();
    private final Runnable tick=new Runnable(){public void run(){updateInfo();handler.postDelayed(this,1000);}};
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        dark=getPreferences(0).getBoolean("dark",true);
        build();
    }
    @Override protected void onResume(){super.onResume();loadApps();updateInfo();handler.removeCallbacks(tick);handler.post(tick);}
    @Override protected void onPause(){handler.removeCallbacks(tick);super.onPause();}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);showHome();}
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private int color(String s){return Color.parseColor(s);}
    private void palette(){
        bg=color(dark?"#070A10":"#E8EEF5"); fg=color(dark?"#F3F7FC":"#142033");
        muted=color(dark?"#92A4B9":"#52657D");panel=color(dark?"#151D28":"#F8FAFD");
        accent=color(dark?"#60DFFF":"#007A9E");border=color(dark?"#293C4D":"#C9D6E4");
    }
    private GradientDrawable surface(int c,int radius,boolean stroke){
        GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(radius));if(stroke)d.setStroke(dp(1),border);return d;
    }
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private TextView text(String s,int size,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(c);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private void add(LinearLayout l,View v,int height){l.addView(v,new LinearLayout.LayoutParams(-1,height<0?height:dp(height)));}
    private void gap(LinearLayout l,int h){add(l,new View(this),h);}
    private TextView button(String s,Runnable action){
        TextView t=text(s,15,fg);t.setGravity(Gravity.CENTER);t.setBackground(surface(panel,18,true));t.setPadding(dp(10),dp(10),dp(10),dp(10));t.setOnClickListener(v->action.run());return t;
    }
    private void build(){
        palette();getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        root=vertical();root.setBackgroundColor(bg);root.setPadding(dp(20),dp(12),dp(20),dp(12));setContentView(root);
        home=vertical();root.addView(home,new LinearLayout.LayoutParams(-1,0,1));
        TextView mk=text("MK",38,accent);mk.setTypeface(Typeface.DEFAULT,Typeface.BOLD_ITALIC);mk.setGravity(Gravity.CENTER);add(home,mk,54);
        TextView title=text("T I T A N I U M   /   V 1",11,muted);title.setGravity(Gravity.CENTER);add(home,title,20);
        gap(home,14);
        clock=text("--:--",64,fg);clock.setTypeface(Typeface.create("sans-serif-light",0));clock.setGravity(Gravity.CENTER);add(home,clock,84);
        date=text("",13,muted);date.setGravity(Gravity.CENTER);add(home,date,26);gap(home,18);
        LinearLayout tiles=row();add(home,tiles,82);
        battery=text("BATERIA  --%",17,accent);battery.setGravity(Gravity.CENTER);battery.setBackground(surface(panel,20,true));tiles.addView(battery,new LinearLayout.LayoutParams(0,-1,1));
        View spacing=new View(this);tiles.addView(spacing,new LinearLayout.LayoutParams(dp(10),1));
        TextView settings=button("Ajustes",()->startActivity(new Intent(Settings.ACTION_SETTINGS)));tiles.addView(settings,new LinearLayout.LayoutParams(0,-1,1));
        gap(home,12);
        LinearLayout theme=row();theme.setPadding(dp(14),0,dp(12),0);theme.setBackground(surface(panel,20,true));add(home,theme,78);
        themeLabel=text(dark?"Dark Mode":"Light Mode",17,fg);theme.addView(themeLabel,new LinearLayout.LayoutParams(0,-1,1));
        themeSwitch=new ThemeSwitch();theme.addView(themeSwitch,new LinearLayout.LayoutParams(dp(150),dp(66)));
        theme.setOnClickListener(v->themeSwitch.toggle());gap(home,12);
        TextView favorites=text("ACESSO RÁPIDO",11,muted);add(home,favorites,24);
        LinearLayout quick=row();add(home,quick,64);
        addQuick(quick,"Telefone",new Intent(Intent.ACTION_DIAL));addQuick(quick,"Câmera",new Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));addQuick(quick,"Ajustes",new Intent(Settings.ACTION_SETTINGS));
        home.addView(new View(this),new LinearLayout.LayoutParams(1,0,1));
        add(home,button("↑   Todos os aplicativos",()->showDrawer()),54);gap(home,8);
        add(home,button("Definir MK como tela inicial",()->requestHome()),44);
        drawer=vertical();drawer.setVisibility(View.GONE);root.addView(drawer,new LinearLayout.LayoutParams(-1,0,1));
        add(drawer,button("‹  Tela inicial",()->showHome()),48);gap(drawer,10);
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(fg);search.setHintTextColor(muted);search.setHint("Buscar aplicativos");search.setPadding(dp(15),0,dp(15),0);search.setBackground(surface(panel,16,true));add(drawer,search,54);gap(drawer,10);
        ScrollView scroll=new ScrollView(this);apps=vertical();scroll.addView(apps);drawer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){renderApps();}public void afterTextChanged(Editable e){}});
        updateInfo();renderApps();
    }
    private void addQuick(LinearLayout l,String name,Intent intent){TextView t=button(name,()->launch(intent));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);p.setMargins(dp(3),0,dp(3),0);l.addView(t,p);}
    private void launch(Intent intent){try{startActivity(intent);}catch(Exception e){Toast.makeText(this,"Aplicativo indisponível",Toast.LENGTH_SHORT).show();}}
    private void requestHome(){try{if(Build.VERSION.SDK_INT>=29){RoleManager rm=getSystemService(RoleManager.class);if(rm.isRoleAvailable(RoleManager.ROLE_HOME)&&!rm.isRoleHeld(RoleManager.ROLE_HOME)){startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME),8);return;}}startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(Exception e){launch(new Intent(Settings.ACTION_SETTINGS));}}
    private void loadApps(){Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(Intent.CATEGORY_LAUNCHER);installed.clear();installed.addAll(getPackageManager().queryIntentActivities(i,0));installed.sort((a,b)->a.loadLabel(getPackageManager()).toString().compareToIgnoreCase(b.loadLabel(getPackageManager()).toString()));renderApps();}
    private void renderApps(){if(apps==null)return;apps.removeAllViews();String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);PackageManager pm=getPackageManager();
        for(ResolveInfo r:installed){String label=r.loadLabel(pm).toString();if(r.activityInfo.packageName.equals(getPackageName())||!label.toLowerCase(Locale.ROOT).contains(q))continue;
            LinearLayout item=row();item.setPadding(dp(12),0,dp(10),0);item.setBackground(surface(panel,16,true));ImageView icon=new ImageView(this);icon.setImageDrawable(r.loadIcon(pm));item.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(42)));TextView t=text(label,16,fg);t.setPadding(dp(12),0,0,0);item.addView(t,new LinearLayout.LayoutParams(0,-1,1));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(66));p.bottomMargin=dp(8);apps.addView(item,p);
            item.setOnClickListener(v->{Intent in=new Intent(Intent.ACTION_MAIN);in.addCategory(Intent.CATEGORY_LAUNCHER);in.setComponent(new ComponentName(r.activityInfo.packageName,r.activityInfo.name));in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);launch(in);});
        }
    }
    private void showDrawer(){loadApps();home.setVisibility(View.GONE);drawer.setVisibility(View.VISIBLE);search.clearFocus();}
    private void showHome(){if(home!=null){home.setVisibility(View.VISIBLE);drawer.setVisibility(View.GONE);((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(),0);}}
    @Override public void onBackPressed(){showHome();}
    private void updateInfo(){if(clock==null)return;Date now=new Date();clock.setText(new SimpleDateFormat("HH:mm",new Locale("pt","BR")).format(now));date.setText(new SimpleDateFormat("EEEE, dd MMMM",new Locale("pt","BR")).format(now).toUpperCase(new Locale("pt","BR")));BatteryManager b=(BatteryManager)getSystemService(BATTERY_SERVICE);int n=b.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);battery.setText(n>=0&&n<=100?"BATERIA  "+n+"%":"BATERIA  --");}
    private class ThemeSwitch extends View{
        final Paint p=new Paint(3);float progress=0;boolean running=false;
        ThemeSwitch(){super(MainActivity.this);setContentDescription("Alternar tema");setFocusable(true);setOnClickListener(v->toggle());}
        void toggle(){if(running)return;running=true;android.animation.ValueAnimator a=android.animation.ValueAnimator.ofFloat(0,1);a.setDuration(950);a.setInterpolator(new LinearInterpolator());a.addUpdateListener(v->{progress=(Float)v.getAnimatedValue();invalidate();});a.addListener(new android.animation.AnimatorListenerAdapter(){@Override public void onAnimationEnd(android.animation.Animator v){dark=!dark;getPreferences(0).edit().putBoolean("dark",dark).apply();build();}});a.start();}
        void circle(Canvas c,float x,float y,float r,int col){p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);canvas.save();canvas.scale(getWidth()/150f,getHeight()/66f);
            p.setColor(color("#090E15"));canvas.drawRoundRect(3,5,147,61,19,19,p);
            p.setColor(color("#243649"));canvas.drawOval(112,47,141,57,p);p.setColor(Color.BLACK);canvas.drawOval(115,49,138,55,p);
            circle(canvas,31,30,20,color("#182B38"));circle(canvas,31,30,13,dark?color("#60DFFF"):color("#F6BD55"));
            if(dark)circle(canvas,37,25,12,color("#182B38"));else{p.setColor(color("#F6BD55"));p.setStrokeWidth(2);for(int i=0;i<8;i++){double t=i*Math.PI/4;canvas.drawLine(31+(float)Math.cos(t)*16,30+(float)Math.sin(t)*16,31+(float)Math.cos(t)*19,30+(float)Math.sin(t)*19,p);}}
            if(running){float t=progress;float x=t<.5f?126-95*(t*2):31+95*((t-.5f)*2);float y=42-7*(float)Math.abs(Math.sin(t*Math.PI*8));float fade=Math.min(1,Math.min(t*10,(1-t)*10));int col=Color.argb((int)(255*fade),112,224,255);circle(canvas,x,y-15,5,col);p.setColor(col);p.setStrokeWidth(3);p.setStrokeCap(Paint.Cap.ROUND);canvas.drawLine(x,y-9,x,y+1,p);float step=5*(float)Math.sin(t*55);canvas.drawLine(x,y+1,x+step,y+9,p);canvas.drawLine(x,y+1,x-step,y+9,p);canvas.drawLine(x,y-7,x+7,y-3,p);}
            canvas.restore();
        }
    }
}
